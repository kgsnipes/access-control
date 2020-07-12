package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.AccessControlUser;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.exception.UserGroupNotFoundException;
import com.accesscontrol.exception.UserNotFoundException;
import com.accesscontrol.models.*;
import com.accesscontrol.repository.*;
import com.accesscontrol.services.ChangeLogService;
import com.accesscontrol.services.PasswordEncryptionService;
import com.accesscontrol.services.UserService;
import com.accesscontrol.util.AccessControlUtil;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;



public class DefaultUserService implements UserService {


    private static final Logger log= LogManager.getLogger(DefaultUserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private ValidatorFactory validatorFactory;

    @Autowired
    private PasswordEncryptionService passwordEncryptionService;

    @Autowired
    private ChangeLogService changeLogService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private User2UserGroupRelationRepository user2UserGroupRelationRepository;

    @Autowired
    private UserGroup2UserGroupRelationRepository userGroup2UserGroupRelationRepository;

    @Autowired
    private AccessPermission2UserGroupRelationRepository accessPermission2UserGroupRelationRepository;

    @Autowired
    private AccessPermissionRepository accessPermissionRepository;


    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;


    @Autowired
    private UserDataImportService userDataImportService;

    @Autowired
    private UserGroupDataImportService userGroupDataImportService;

    @Autowired
    private UserGroup2UserGroupRelationDataImportService userGroup2UserGroupRelationDataImportService;

    @Autowired
    private  User2UserGroupRelationDataImportService user2UserGroupRelationDataImportService;

    @Autowired
    private AccessPermissionDataImportService accessPermissionDataImportService;

    @Autowired
    private AccessPermission2UserGroupRelationDataImportService accessPermission2UserGroupRelationDataImportService;

    @Autowired
    private ChangeLogRepository changeLogRepository;

    private static final String USERID_NOT_NULL_MESSAGE="UserId cannot be null or empty";
    private static final String NO_SUCH_USER_AVAILABLE_MESSAGES="No such user available";
    private static final String SEARCH_TERM_NOT_EMPTY="search term cannot be empty or page number cannot be null or less than 1";
    private static final String USERID_EMPTY_MESSAGE="user id is empty or pagenumber is invalid";
    private static final String ERROR_FETCHING_USERGROUPS_MESSAGE="Error while fetching user groups";
    private static final String PERMISSION_OBJ_CANNOT_BE_NULL_MESSAGE="permission object or user group cannot be null";
    private static final String ERROR_IN_EXPORT="Error processing export for {}";

    @Transactional
    @Override
    public User createUser(User user, AccessControlContext ctx) {
        User savedUser=null;
        if(Objects.isNull(user))
        {
            log.error("The user object is null");
            throw new IllegalArgumentException("User object cannot be null");
        }

        Validator validator=validatorFactory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if(CollectionUtils.isNotEmpty(violations))
        {
            String errorMsg= violations.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            log.error("Error were found in the input {}",errorMsg);
            throw new AccessControlException(errorMsg);
        }
        else
        {
            User existingUser=userRepository.findByUserId(user.getUserId());
            if(Objects.nonNull(existingUser))
            {
                log.error("User ID already existing {}",user.getUserId());
                throw new AccessControlException("User ID already existing "+user.getUserId());
            }
            encryptPasswordIfNotEncrypted(user);
            savedUser=userRepository.save(user);
            log.debug("User Created with ID :{}",savedUser.getId());
            changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,user,savedUser,ctx);
        }
        return savedUser;
    }

    private void encryptPasswordIfNotEncrypted(User user)
    {
        if(!BooleanUtils.isTrue(passwordEncryptionService.isPasswordEncrypted(user.getPassword())))
        {
            user.setPassword(passwordEncryptionService.encryptPassword(user.getPassword()));
        }
    }

    @Transactional
    @Override
    public User saveUser(User user, AccessControlContext ctx) {
        User savedUser=null;
        if(Objects.isNull(user) || Objects.isNull(user.getUserId()) || Objects.isNull(user.getId()))
        {
            log.error("User object cannot be null.");
            throw new IllegalArgumentException("User object cannot be null.");
        }

        Validator validator=validatorFactory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if(CollectionUtils.isNotEmpty(violations))
        {
            String errorMsg= violations.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            log.error("Validation errors found {}",errorMsg);
            throw new AccessControlException(errorMsg);
        }
        else
        {
            User retrievedUser=getUserById(user.getUserId());

            if(Objects.nonNull(retrievedUser))
            {
                encryptPasswordIfNotEncrypted(user);
                retrievedUser.setFirstName(user.getFirstName());
                retrievedUser.setLastName(user.getLastName());
                retrievedUser.setEnabled(user.getEnabled());
                retrievedUser.setPassword(user.getPassword());
                savedUser=userRepository.save(retrievedUser);
                log.debug("Update user with ID {}",savedUser.getId());
                changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.UPDATE,user,savedUser,ctx);

            }

        }
        return savedUser;
    }

    @Transactional
    @Override
    public void disableUser(String userId, AccessControlContext ctx) {

        if(StringUtils.isEmpty(userId))
        {
            log.error(USERID_NOT_NULL_MESSAGE);
            throw new IllegalArgumentException(USERID_NOT_NULL_MESSAGE);
        }

        User user=userRepository.findByUserId(userId);
        if(Objects.isNull(user))
        {
            log.error("User not found with id : {}",userId);
            throw new UserNotFoundException(NO_SUCH_USER_AVAILABLE_MESSAGES);
        }
        else
        {
            user.setEnabled(false);
            User savedUser=userRepository.save(user);
            changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.UPDATE,user,savedUser,ctx);
            log.debug("disabled the user with Id :{}",userId);
        }

    }

    @Transactional
    @Override
    public void enableUser(String userId, AccessControlContext ctx) {
        if(StringUtils.isEmpty(userId))
        {
            log.error(USERID_NOT_NULL_MESSAGE);
            throw new IllegalArgumentException(USERID_NOT_NULL_MESSAGE);
        }

        User user=userRepository.findByUserId(userId);
        if(Objects.isNull(user))
        {
            log.error(NO_SUCH_USER_AVAILABLE_MESSAGES);
            throw new UserNotFoundException(NO_SUCH_USER_AVAILABLE_MESSAGES);
        }
        else
        {
            user.setEnabled(true);
            User savedUser=userRepository.save(user);
            changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.UPDATE,user,savedUser,ctx);
            log.debug("Enabled the user with ID: {}",userId);
        }
    }

    @Transactional
    @Override
    public void deleteUser(String userId, AccessControlContext ctx) {

        if(StringUtils.isEmpty(userId))
        {
            log.error(USERID_NOT_NULL_MESSAGE);
            throw new IllegalArgumentException(USERID_NOT_NULL_MESSAGE);
        }

        User user=userRepository.findByUserId(userId);
        if(Objects.isNull(user))
        {
            log.error(NO_SUCH_USER_AVAILABLE_MESSAGES);
            throw new UserNotFoundException(NO_SUCH_USER_AVAILABLE_MESSAGES);
        }
        else
        {
            changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.DELETE,user,null,ctx);
            userRepository.delete(user);
            log.debug("Deleted the user with ID: {}",userId);
        }

    }


    @Override
    public User getUserById(String userId) {

        if(StringUtils.isEmpty(userId))
        {
            log.error(USERID_NOT_NULL_MESSAGE);
            throw new IllegalArgumentException(USERID_NOT_NULL_MESSAGE);
        }

        User user=userRepository.findByUserId(userId);
        if(Objects.isNull(user))
        {
            log.error(NO_SUCH_USER_AVAILABLE_MESSAGES);
            throw new UserNotFoundException(NO_SUCH_USER_AVAILABLE_MESSAGES);
        }
        else
        {
            return user;
        }
    }

    @Override
    public PageResult<User> findUsers(String searchTerm, Integer pageNumber) {

        if(StringUtils.isEmpty(searchTerm) || (Objects.isNull(pageNumber) || pageNumber<-1))
        {
            log.error(SEARCH_TERM_NOT_EMPTY);
            throw new IllegalArgumentException(SEARCH_TERM_NOT_EMPTY);
        }

        Page<User> userList=userRepository.findUsers(searchTerm, AccessControlUtil.getPageParameter(userRepository,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)));

        if(Objects.nonNull(userList))
        {
            return new PageResult<User>(Collections.unmodifiableList(userList.getContent()),pageNumber,userList.getSize(), (int) userList.getTotalElements());
        }
        return new PageResult(Collections.emptyList(),pageNumber,0, 0);
    }

    @Transactional
    @Override
    public UserGroup createUserGroup(UserGroup userGroup, AccessControlContext ctx) {

        UserGroup savedGroup=null;
        if(Objects.isNull(userGroup))
        {
            log.error("User group object cannot be null");
            throw new IllegalArgumentException("User group object cannot be null");
        }

        Validator validator=validatorFactory.getValidator();
        Set<ConstraintViolation<UserGroup>> violations = validator.validate(userGroup);
        if(CollectionUtils.isNotEmpty(violations))
        {
            String errorMsg= violations.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            log.error("Validation failure : {}",errorMsg);
            throw new AccessControlException(errorMsg);
        }
        else
        {
            UserGroup existingGroup=userGroupRepository.findByCode(userGroup.getCode());
            if(Objects.nonNull(existingGroup))
            {
                log.error("User group with code already existing {}",userGroup.getCode());
                throw new AccessControlException("User group with code already existing "+userGroup.getCode());
            }

            savedGroup=userGroupRepository.save(userGroup);
            changeLogService.logChange(userGroup.getId(),userGroup.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,userGroup,savedGroup,ctx);
        }
        return savedGroup;
    }

    @Transactional
    @Override
    public UserGroup saveUserGroup(UserGroup userGroup, AccessControlContext ctx) {

        UserGroup savedUserGroup=null;
        if(Objects.isNull(userGroup) || Objects.isNull(userGroup.getCode()) || Objects.isNull(userGroup.getId()))
        {
            throw new IllegalArgumentException("User group object cannot be null or code or id is null or empty");
        }

        Validator validator=validatorFactory.getValidator();
        Set<ConstraintViolation<UserGroup>> violations = validator.validate(userGroup);
        if(CollectionUtils.isNotEmpty(violations))
        {
            String errorMsg= violations.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            throw new AccessControlException(errorMsg);
        }
        else
        {
            UserGroup retrievedUserGroup=getUserGroupByCode(userGroup.getCode());

            if(Objects.nonNull(retrievedUserGroup))
            {

                retrievedUserGroup.setCode(userGroup.getCode());
                retrievedUserGroup.setName(userGroup.getName());

                savedUserGroup=userGroupRepository.save(retrievedUserGroup);
                changeLogService.logChange(savedUserGroup.getId(),savedUserGroup.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.UPDATE,userGroup,savedUserGroup,ctx);

            }
            else
            {
                return createUserGroup(userGroup,ctx);
            }

        }
        return savedUserGroup;


    }

    @Transactional
    @Override
    public void disableUserGroup(String userGroupCode, AccessControlContext ctx) {

        if(StringUtils.isEmpty(userGroupCode))
        {
            throw new IllegalArgumentException(USERID_NOT_NULL_MESSAGE);
        }

        UserGroup userGroup=userGroupRepository.findByCode(userGroupCode);
        if(Objects.isNull(userGroup))
        {
            throw new UserGroupNotFoundException(NO_SUCH_USER_AVAILABLE_MESSAGES);
        }
        else
        {
            userGroup.setEnabled(false);
            UserGroup savedUserGroup=userGroupRepository.save(userGroup);
            changeLogService.logChange(userGroup.getId(),userGroup.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.UPDATE,userGroup,savedUserGroup,ctx);

        }

    }

    @Transactional
    @Override
    public void enableUserGroup(String userGroupCode, AccessControlContext ctx) {

        if(StringUtils.isEmpty(userGroupCode))
        {
            throw new IllegalArgumentException(USERID_NOT_NULL_MESSAGE);
        }

        UserGroup userGroup=userGroupRepository.findByCode(userGroupCode);
        if(Objects.isNull(userGroup))
        {
            throw new UserGroupNotFoundException(NO_SUCH_USER_AVAILABLE_MESSAGES);
        }
        else
        {
            userGroup.setEnabled(true);
            UserGroup savedUserGroup=userGroupRepository.save(userGroup);
            changeLogService.logChange(userGroup.getId(),userGroup.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.UPDATE,userGroup,savedUserGroup,ctx);

        }

    }

    @Transactional
    @Override
    public void deleteUserGroup(String userGroupCode, AccessControlContext ctx) {

        if(StringUtils.isEmpty(userGroupCode))
        {
            throw new IllegalArgumentException("user group code cannot be null or empty");
        }

        UserGroup userGroup=userGroupRepository.findByCode(userGroupCode);
        if(Objects.isNull(userGroup))
        {
            throw new UserGroupNotFoundException(NO_SUCH_USER_AVAILABLE_MESSAGES);
        }
        else
        {
            changeLogService.logChange(userGroup.getId(),userGroup.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.DELETE,userGroup,null,ctx);
            userGroupRepository.delete(userGroup);

        }

    }

    @Override
    public UserGroup getUserGroupByCode(String code) {
        if(StringUtils.isEmpty(code))
        {
            throw new IllegalArgumentException("code cannot be null or empty");
        }

        UserGroup ug=userGroupRepository.findByCode(code);
        if(Objects.isNull(ug))
        {
            throw new UserGroupNotFoundException("No such usergroup available");
        }
        else
        {
            return ug;
        }
    }

    @Override
    public PageResult<UserGroup> findUserGroups(String searchTerm, Integer pageNumber) {

        if(StringUtils.isEmpty(searchTerm) || (Objects.isNull(pageNumber) || pageNumber<-1))
        {
            throw new IllegalArgumentException(SEARCH_TERM_NOT_EMPTY);
        }

        Page<UserGroup> userGroupList=userGroupRepository.findUserGroups(searchTerm,AccessControlUtil.getPageParameter(userGroupRepository,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)));

        if(Objects.nonNull(userGroupList))
        {
            return new PageResult(Collections.unmodifiableList(userGroupList.getContent()),pageNumber,userGroupList.getSize(), (int) userGroupList.getTotalElements());
        }
        return new PageResult(Collections.emptyList(),pageNumber,0, 0);
    }


    @Transactional
    @Override
    public void addUserToUserGroup(String userId, String userGroupCode,AccessControlContext ctx) {
        if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(userGroupCode) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("userId or userGroupcode cannot be empty or context is empty");
        }
        addUserToUserGroup(this.getUserById(userId),this.getUserGroupByCode(userGroupCode),ctx);
    }

    @Transactional
    @Override
    public void addUserToUserGroup(User user, UserGroup userGroup,AccessControlContext ctx) {
        if(Objects.isNull(user) || Objects.isNull(userGroup) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("user or usergroup cannot be empty or context is empty");
        }

        User2UserGroupRelation relation=user2UserGroupRelationRepository.findByUserIdAndUserGroupCode(user.getUserId(),userGroup.getCode());
        if(Objects.isNull(relation))
        {
            User2UserGroupRelation relationToSave=new User2UserGroupRelation();
            relationToSave.setUserId(user.getUserId());
            relationToSave.setUserGroupCode(userGroup.getCode());
            User2UserGroupRelation relationToSaved=user2UserGroupRelationRepository.save(relationToSave);
            changeLogService.logChange(relationToSaved.getId(),relationToSave.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,relationToSave,relationToSaved,ctx);
        }

    }

    @Transactional
    @Override
    public void removeUserFromUserGroup(String userId, String userGroupCode,AccessControlContext ctx) {
        if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(userGroupCode) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("userId or userGroupcode cannot be empty or context is null");
        }
        removeUserFromUserGroup(this.getUserById(userId),this.getUserGroupByCode(userGroupCode),ctx);
    }

    @Transactional
    @Override
    public void removeUserFromUserGroup(User user, UserGroup userGroup,AccessControlContext ctx) {
        if(Objects.isNull(user) || Objects.isNull(userGroup) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("user or usergroup cannot be empty or context is null");
        }

        User2UserGroupRelation relation=user2UserGroupRelationRepository.findByUserIdAndUserGroupCode(user.getUserId(),userGroup.getCode());
        if(Objects.nonNull(relation))
        {
            changeLogService.logChange(relation.getId(),relation.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.DELETE,relation,null,ctx);
            user2UserGroupRelationRepository.delete(relation);
        }

    }

    @Transactional
    @Override
    public void addUserGroupToUserGroup(String childUserGroupCode, String parentUserGroupCode, AccessControlContext ctx) {
        if(StringUtils.isEmpty(childUserGroupCode) || StringUtils.isEmpty(parentUserGroupCode) || Objects.isNull(ctx) || StringUtils.trimToEmpty(childUserGroupCode).equals(StringUtils.trimToEmpty(parentUserGroupCode)))
        {
            throw new IllegalArgumentException("usergroup cannot be empty or context is null or both child and parent are the same");
        }
        addUserGroupToUserGroup(getUserGroupByCode(childUserGroupCode),getUserGroupByCode(parentUserGroupCode),ctx);
    }

    @Transactional
    @Override
    public void addUserGroupToUserGroup(UserGroup childUserGroup, UserGroup parentUserGroup, AccessControlContext ctx) {

        if(Objects.isNull(childUserGroup) || Objects.isNull(parentUserGroup) || Objects.isNull(ctx) || StringUtils.trimToEmpty(childUserGroup.getCode()).equals(StringUtils.trimToEmpty(parentUserGroup.getCode())))
        {
            throw new IllegalArgumentException("user or usergroup cannot be empty or context is null or both child and parent are the same");
        }
        UserGroup2UserGroupRelation relation=userGroup2UserGroupRelationRepository.findByChildUserGroupCodeAndParentUserGroupCode(childUserGroup.getCode(),parentUserGroup.getCode());
        if(Objects.isNull(relation))
        {
            UserGroup2UserGroupRelation relationToSave=new UserGroup2UserGroupRelation(childUserGroup.getCode(),parentUserGroup.getCode());
            UserGroup2UserGroupRelation saved=userGroup2UserGroupRelationRepository.save(relationToSave);
            changeLogService.logChange(saved.getId(),saved.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,relationToSave,saved,ctx);
        }

    }

    @Transactional
    @Override
    public void removeUserGroupFromUserGroup(String childUserGroupCode, String parentUserGroupCode, AccessControlContext ctx) {

        if(StringUtils.isEmpty(childUserGroupCode) || StringUtils.isEmpty(parentUserGroupCode) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("usergroup cannot be empty or context is null");
        }
        removeUserGroupFromUserGroup(getUserGroupByCode(childUserGroupCode),getUserGroupByCode(parentUserGroupCode),ctx);

    }

    @Transactional
    @Override
    public void removeUserGroupFromUserGroup(UserGroup childUserGroup, UserGroup parentUserGroup, AccessControlContext ctx) {

        if(Objects.isNull(childUserGroup) || Objects.isNull(parentUserGroup) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("childusergroup or parentusergroup cannot be empty or context is null");
        }

        UserGroup2UserGroupRelation relation=userGroup2UserGroupRelationRepository.findByChildUserGroupCodeAndParentUserGroupCode(childUserGroup.getCode(),parentUserGroup.getCode());
        if(Objects.nonNull(relation))
        {
            changeLogService.logChange(relation.getId(),relation.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.DELETE,relation,null,ctx);
            userGroup2UserGroupRelationRepository.delete(relation);
        }
    }

    @Override
    public PageResult<UserGroup> getAllUserGroupsForUser(String userId, Integer pageNumber) {
        if(StringUtils.isEmpty(userId) || Objects.isNull(pageNumber) || pageNumber<-1)
        {
            throw new IllegalArgumentException(USERID_EMPTY_MESSAGE);
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> allGroups=new HashSet();
        User user=getUserById(userId);
        if(Objects.nonNull(user))
        {
            PageResult<UserGroup> immediateUserGroups=getParentUserGroupsForUser(userId,-1);
            if(Objects.nonNull(immediateUserGroups) && CollectionUtils.isNotEmpty(immediateUserGroups.getResults()))
            {
                allGroups.addAll(immediateUserGroups.getResults());
                immediateUserGroups.getResults().stream().forEach(group->getParentGroupsForUserGroup(group.getCode(),allGroups));
            }
        }

        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(allGroups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
           log.error(ERROR_FETCHING_USERGROUPS_MESSAGE,e);
        }
        return result;
    }

    @Override
    public PageResult<UserGroup> getParentUserGroupsForUser(String userId, Integer pageNumber) {
        if(StringUtils.isEmpty(userId) || Objects.isNull(pageNumber) || pageNumber<-1)
        {
            throw new IllegalArgumentException(USERID_EMPTY_MESSAGE);
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> groups=new HashSet();
        User user=getUserById(userId);
        if(Objects.nonNull(user)){
            List<User2UserGroupRelation> user2UserGroupRelation=user2UserGroupRelationRepository.findByUserId(userId);
            if(CollectionUtils.isNotEmpty(user2UserGroupRelation))
            {
                user2UserGroupRelation.stream().forEach(rel -> groups.add(getUserGroupByCode(rel.getUserGroupCode())));
            }
        }
        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(groups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
            log.error(ERROR_FETCHING_USERGROUPS_MESSAGE,e);
        }

        return result;
    }

    @Override
    public PageResult<UserGroup> getAllUserGroupsForUserGroup(String userGroupCode, Integer pageNumber) {
        if(StringUtils.isEmpty(userGroupCode) || Objects.isNull(pageNumber) || pageNumber<-1)
        {
            throw new IllegalArgumentException(USERID_EMPTY_MESSAGE);
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> groups=new HashSet();
        UserGroup userGroup=getUserGroupByCode(userGroupCode);
        if(Objects.nonNull(userGroup))
        {
            List<UserGroup2UserGroupRelation> immediateGroups=userGroup2UserGroupRelationRepository.findByChildUserGroupCode(userGroupCode);
            if(CollectionUtils.isNotEmpty(immediateGroups)){
                immediateGroups.stream().forEach(ug->getParentGroupsForUserGroup(ug.getParentUserGroupCode(),groups));
            }

        }

        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(groups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
            log.error(ERROR_FETCHING_USERGROUPS_MESSAGE,e);
        }
        return result;
    }

    @Override
    public PageResult<UserGroup> getParentUserGroupsForUserGroup(String userGroupCode, Integer pageNumber) {
        if(StringUtils.isEmpty(userGroupCode) || Objects.isNull(pageNumber) || pageNumber<-1)
        {
            throw new IllegalArgumentException(USERID_EMPTY_MESSAGE);
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> groups=new HashSet();
        UserGroup userGroup=getUserGroupByCode(userGroupCode);
        if(Objects.nonNull(userGroup))
        {
            List<UserGroup2UserGroupRelation> relations =userGroup2UserGroupRelationRepository.findByChildUserGroupCode(userGroupCode);
            if(CollectionUtils.isNotEmpty(relations))
            {
                relations.stream().forEach(r->groups.add(getUserGroupByCode(r.getParentUserGroupCode())));
            }
        }
        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(groups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
            log.error(ERROR_FETCHING_USERGROUPS_MESSAGE,e);
        }
        return result;
    }

    @Override
    public PageResult<UserGroup> getAllChildUserGroupsForUserGroup(String userGroupCode, Integer pageNumber) {
        if(StringUtils.isEmpty(userGroupCode) || Objects.isNull(pageNumber) || pageNumber<-1)
        {
            throw new IllegalArgumentException(USERID_EMPTY_MESSAGE);
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> groups=new HashSet();
        UserGroup userGroup=getUserGroupByCode(userGroupCode);
        if(Objects.nonNull(userGroup))
        {
            List<UserGroup2UserGroupRelation> relations =userGroup2UserGroupRelationRepository.findByParentUserGroupCode(userGroupCode);
            if(CollectionUtils.isNotEmpty(relations))
            {
                relations.stream().forEach(r->getChildGroupsForUserGroup(r.getChildUserGroupCode(),groups));
            }
        }
        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(groups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
            log.error(ERROR_FETCHING_USERGROUPS_MESSAGE,e);
        }
        return result;
    }

    @Override
    public PageResult<UserGroup> getChildUserGroupsForUserGroup(String userGroupCode, Integer pageNumber) {
        if(StringUtils.isEmpty(userGroupCode) || Objects.isNull(pageNumber) || pageNumber<-1)
        {
            throw new IllegalArgumentException(USERID_EMPTY_MESSAGE);
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> groups=new HashSet();
        UserGroup userGroup=getUserGroupByCode(userGroupCode);
        if(Objects.nonNull(userGroup))
        {
            List<UserGroup2UserGroupRelation> relations =userGroup2UserGroupRelationRepository.findByParentUserGroupCode(userGroupCode);
            if(CollectionUtils.isNotEmpty(relations))
            {
                relations.stream().forEach(r->groups.add(getUserGroupByCode(r.getChildUserGroupCode())));
            }
        }
        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(groups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
            log.error(ERROR_FETCHING_USERGROUPS_MESSAGE,e);
        }
        return result;
    }

    @Transactional
    @Override
    public AccessPermission createPermission(AccessPermission permission, UserGroup userGroup, AccessControlContext ctx) {
        AccessPermission savedPermission=null;
        if(Objects.isNull(permission) || Objects.isNull(userGroup))
        {
            throw new IllegalArgumentException(PERMISSION_OBJ_CANNOT_BE_NULL_MESSAGE);
        }

        Validator validator=validatorFactory.getValidator();
        Set<ConstraintViolation<AccessPermission>> violations = validator.validate(permission);
        Set<ConstraintViolation<UserGroup>> violationsForUserGroup = validator.validate(userGroup);
        if(CollectionUtils.isNotEmpty(violations))
        {
            String errorMsg= violations.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            throw new AccessControlException(errorMsg);
        }
        else if(CollectionUtils.isNotEmpty(violationsForUserGroup))
        {
            String errorMsg= violationsForUserGroup.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            throw new AccessControlException(errorMsg);
        }
        else
        {
            AccessPermission existingPermission=accessPermissionRepository.findByResourceAndPermission(permission.getResource(),permission.getPermission());

            if(Objects.isNull(existingPermission))
            {
                savedPermission=accessPermissionRepository.save(permission);
                AccessPermission2UserGroupRelation relation=new AccessPermission2UserGroupRelation();
                relation.setAccessPermissionId(savedPermission.getId());
                relation.setUserGroupCode(userGroup.getCode());
                relation.setEnabled(true);
                AccessPermission2UserGroupRelation savedRelation=accessPermission2UserGroupRelationRepository.save(relation);
                changeLogService.logChange(savedPermission.getId(),savedPermission.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,permission,savedPermission,ctx);
                changeLogService.logChange(savedRelation.getId(),savedRelation.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,relation,savedRelation,ctx);
            }
            else
            {
                AccessPermission2UserGroupRelation existingRelation=accessPermission2UserGroupRelationRepository.findByUserGroupCodeAndAccessPermissionId(userGroup.getCode(),existingPermission.getId());
                if(Objects.isNull(existingRelation))
                {
                    AccessPermission2UserGroupRelation relation=new AccessPermission2UserGroupRelation();
                    relation.setAccessPermissionId(existingPermission.getId());
                    relation.setUserGroupCode(userGroup.getCode());
                    relation.setEnabled(true);
                    AccessPermission2UserGroupRelation savedRelation=accessPermission2UserGroupRelationRepository.save(relation);
                    changeLogService.logChange(savedRelation.getId(),savedRelation.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,relation,savedRelation,ctx);
                }
                savedPermission=existingPermission;
            }


        }
        return savedPermission;
    }

    @Transactional
    @Override
    public AccessPermission createPermission(AccessPermission permission, AccessControlContext ctx) {
        AccessPermission savedPermission=null;
        if(Objects.isNull(permission))
        {
            throw new IllegalArgumentException(PERMISSION_OBJ_CANNOT_BE_NULL_MESSAGE);
        }

        Validator validator=validatorFactory.getValidator();
        Set<ConstraintViolation<AccessPermission>> violations = validator.validate(permission);
        if(CollectionUtils.isNotEmpty(violations))
        {
            String errorMsg= violations.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            throw new AccessControlException(errorMsg);
        }
        else
        {
            AccessPermission existingPermission=accessPermissionRepository.findByResourceAndPermission(permission.getResource(),permission.getPermission());

            if(Objects.isNull(existingPermission))
            {
                savedPermission=accessPermissionRepository.save(permission);
                changeLogService.logChange(savedPermission.getId(),savedPermission.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,permission,savedPermission,ctx);
            }
            else
            {
                savedPermission=existingPermission;
            }

        }
        return savedPermission;
    }

    @Transactional
    @Override
    public void enablePermission(AccessPermission permission, UserGroup userGroup, AccessControlContext ctx) {

        if(Objects.isNull(permission) || Objects.isNull(userGroup))
        {
            throw new IllegalArgumentException(PERMISSION_OBJ_CANNOT_BE_NULL_MESSAGE);
        }

        Validator validator=validatorFactory.getValidator();
        Set<ConstraintViolation<AccessPermission>> violations = validator.validate(permission);
        Set<ConstraintViolation<UserGroup>> violationsForUserGroup = validator.validate(userGroup);
        if(CollectionUtils.isNotEmpty(violations))
        {
            String errorMsg= violations.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            throw new AccessControlException(errorMsg);
        }
        else if(CollectionUtils.isNotEmpty(violationsForUserGroup))
        {
            String errorMsg= violationsForUserGroup.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            throw new AccessControlException(errorMsg);
        }
        else
        {
            AccessPermission2UserGroupRelation existingRelation=accessPermission2UserGroupRelationRepository.findByUserGroupCodeAndAccessPermissionId(userGroup.getCode(),permission.getId());
            if(Objects.nonNull(existingRelation))
            {
                existingRelation.setEnabled(true);
                AccessPermission2UserGroupRelation savedRelation=accessPermission2UserGroupRelationRepository.save(existingRelation);
                changeLogService.logChange(savedRelation.getId(),savedRelation.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.UPDATE,existingRelation,savedRelation,ctx);
            }
            else
            {
                AccessPermission2UserGroupRelation savedRelation=accessPermission2UserGroupRelationRepository.save(new AccessPermission2UserGroupRelation(permission.getId(),userGroup.getCode(),true));
                changeLogService.logChange(savedRelation.getId(),savedRelation.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,null,savedRelation,ctx);
            }
        }


    }

    @Transactional
    @Override
    public void disablePermission(AccessPermission permission, UserGroup userGroup, AccessControlContext ctx) {
        if(Objects.isNull(permission) || Objects.isNull(userGroup))
        {
            throw new IllegalArgumentException(PERMISSION_OBJ_CANNOT_BE_NULL_MESSAGE);
        }

        Validator validator=validatorFactory.getValidator();
        Set<ConstraintViolation<AccessPermission>> violations = validator.validate(permission);
        Set<ConstraintViolation<UserGroup>> violationsForUserGroup = validator.validate(userGroup);
        if(CollectionUtils.isNotEmpty(violations))
        {
            String errorMsg= violations.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            throw new AccessControlException(errorMsg);
        }
        else if(CollectionUtils.isNotEmpty(violationsForUserGroup))
        {
            String errorMsg= violationsForUserGroup.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            throw new AccessControlException(errorMsg);
        }
        else
        {
            AccessPermission2UserGroupRelation existingRelation=accessPermission2UserGroupRelationRepository.findByUserGroupCodeAndAccessPermissionId(userGroup.getCode(),permission.getId());
            if(Objects.nonNull(existingRelation))
            {
                existingRelation.setEnabled(false);
                AccessPermission2UserGroupRelation savedRelation=accessPermission2UserGroupRelationRepository.save(existingRelation);
                changeLogService.logChange(savedRelation.getId(),savedRelation.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,existingRelation,savedRelation,ctx);
            }
        }
    }


    @Override
    public PageResult<AccessPermission> getPermissionsForUserGroup(String userGroupCode, Boolean onlyEnabled,Integer pageNumber) {
        if(StringUtils.isEmpty(userGroupCode) || Objects.isNull(pageNumber) || pageNumber<-1)
        {
            throw new IllegalArgumentException(USERID_EMPTY_MESSAGE);
        }
        PageResult<AccessPermission> result=new PageResult<>();
        HashSet<AccessPermission> permissions=new HashSet();
        UserGroup userGroup=getUserGroupByCode(userGroupCode);
        Page<AccessPermission> per=null;
        if(Objects.nonNull(userGroup))
        {
            if(BooleanUtils.isTrue(onlyEnabled))
            {
                per=accessPermissionRepository.findPermissionByUserGroupCode(userGroupCode,true,AccessControlUtil.getPageParameter(accessPermissionRepository,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)));
            }
            else
            {
                per=accessPermissionRepository.findPermissionByUserGroupCode(userGroupCode,AccessControlUtil.getPageParameter(accessPermissionRepository,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)));
            }

            if(CollectionUtils.isNotEmpty(per.getContent()))
            {
                permissions.addAll(per.getContent());
                result.setPageSize(per.getSize());
                result.setPageNumber(pageNumber);
                result.setTotalResults((int)per.getTotalElements());
                result.setResults(Collections.unmodifiableCollection(permissions));
            }
            else
            {
                result.setResults(Collections.emptyList());
                result.setPageSize(per.getSize());
                result.setPageNumber(pageNumber);
                result.setTotalResults((int)per.getTotalElements());
            }

        }
        return result;
    }

    @Override
    public PageResult<AccessPermission> getPermissionsByResourceAndUserGroup(String resource, String userGroupCode, Boolean onlyEnabled,Integer pageNumber) {
        if(StringUtils.isEmpty(userGroupCode) || StringUtils.isEmpty(resource) || Objects.isNull(pageNumber) || pageNumber<-1)
        {
            throw new IllegalArgumentException("user id is empty or resource is empty or pagenumber is invalid");
        }
        PageResult<AccessPermission> result=new PageResult<>();
        HashSet<AccessPermission> permissions=new HashSet();
        UserGroup userGroup=getUserGroupByCode(userGroupCode);
        Page<AccessPermission> per=null;
        if(Objects.nonNull(userGroup))
        {
            if(BooleanUtils.isTrue(onlyEnabled))
            {
                per=accessPermissionRepository.findPermissionByUserGroupCodeAndResource(userGroupCode,resource,true,AccessControlUtil.getPageParameter(accessPermissionRepository,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)));
            }
            else
            {
                per=accessPermissionRepository.findPermissionByUserGroupCodeAndResource(userGroupCode,resource,AccessControlUtil.getPageParameter(accessPermissionRepository,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)));
            }

            if(CollectionUtils.isNotEmpty(per.getContent()))
            {
                permissions.addAll(per.getContent());
                result.setPageSize(per.getSize());
                result.setPageNumber(pageNumber);
                result.setTotalResults((int)per.getTotalElements());
                result.setResults(Collections.unmodifiableCollection(permissions));
            }
            else
            {
                result.setResults(Collections.emptyList());
                result.setPageSize(per.getSize());
                result.setPageNumber(pageNumber);
                result.setTotalResults((int)per.getTotalElements());
            }

        }
        return result;
    }

    @Override
    public UserDetailsService getUserDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String s) {
                User user=null;
                try
                {
                    user=getUserById(s);
                }
                catch (UserNotFoundException e)
                {
                    throw new UsernameNotFoundException("User not found");
                }
                if(Objects.nonNull(user))
                {
                    Collection<SimpleGrantedAuthority> simpleGrantedAuthorities=new ArrayList<>();
                    getAllUserGroupsForUser(user.getUserId(),-1).getResults().stream().forEach(grp->
                            simpleGrantedAuthorities.add(new SimpleGrantedAuthority(grp.getCode())));
                    return new AccessControlUser(user.getUserId(),user.getPassword(),user.getEnabled(),Collections.unmodifiableCollection(simpleGrantedAuthorities));
                }
                return null;
            }
        };
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                return passwordEncryptionService.encryptPassword((String) charSequence);
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return passwordEncryptionService.encryptPassword((String) charSequence).equals(s);
            }
        };
    }


    private void getParentGroupsForUserGroup(String userGroupCode,Collection<UserGroup> groups)
    {
        List<UserGroup2UserGroupRelation> relations =userGroup2UserGroupRelationRepository.findByChildUserGroupCode(userGroupCode);
        groups.add(getUserGroupByCode(userGroupCode));
        if(CollectionUtils.isNotEmpty(relations))
        {

            relations.stream().forEach(r->getParentGroupsForUserGroup(r.getParentUserGroupCode(),groups));
        }

    }

    private void getChildGroupsForUserGroup(String userGroupCode,Collection<UserGroup> groups)
    {
        List<UserGroup2UserGroupRelation> relations =userGroup2UserGroupRelationRepository.findByParentUserGroupCode(userGroupCode);
        if(CollectionUtils.isNotEmpty(relations))
        {
            groups.add(getUserGroupByCode(userGroupCode));
            relations.stream().forEach(r->getChildGroupsForUserGroup(r.getChildUserGroupCode(),groups));
        }
        else
        {
            groups.add(getUserGroupByCode(userGroupCode));
        }

    }



    @Override
    public PageResult<User> importUsers(List<User> users,AccessControlContext ctx) {

        return userDataImportService.process(users,ctx);
    }

    @Override
    public PageResult<UserGroup> importUserGroups(List<UserGroup> userGroups, AccessControlContext ctx) {
        return userGroupDataImportService.process(userGroups,ctx);
    }


    @Override
    public PageResult<UserGroup2UserGroupRelation> importUserGroupRelations(List<UserGroup2UserGroupRelation> relations, AccessControlContext ctx) {

        return userGroup2UserGroupRelationDataImportService.process(relations,ctx);
    }

    @Override
    public PageResult<User2UserGroupRelation> importUser2UserGroupRelations(List<User2UserGroupRelation> relations, AccessControlContext ctx) {
        return user2UserGroupRelationDataImportService.process(relations,ctx);
    }

    @Override
    public PageResult<AccessPermission> importAccessPermissions(List<AccessPermission> permissions, AccessControlContext ctx) {
        return accessPermissionDataImportService.process(permissions,ctx);
    }

    @Override
    public PageResult<AccessPermission2UserGroupRelation> importAccessPermissions2UserGroupRelations(List<AccessPermission2UserGroupRelation> relations, AccessControlContext ctx) {
        return accessPermission2UserGroupRelationDataImportService.process(relations,ctx);
    }

    @Override
    public PageResult<User> importUsers(Reader reader, AccessControlContext ctx) {
        return userDataImportService.process(reader,ctx);
    }

    @Override
    public PageResult<UserGroup> importUserGroups(Reader reader, AccessControlContext ctx) {
        return userGroupDataImportService.process(reader,ctx);
    }

    @Override
    public PageResult<UserGroup2UserGroupRelation> importUserGroupRelations(Reader reader, AccessControlContext ctx) {
        return userGroup2UserGroupRelationDataImportService.process(reader,ctx);
    }

    @Override
    public PageResult<User2UserGroupRelation> importUser2UserGroupRelations(Reader reader, AccessControlContext ctx) {
        return user2UserGroupRelationDataImportService.process(reader,ctx);
    }

    @Override
    public PageResult<AccessPermission> importAccessPermissions(Reader reader, AccessControlContext ctx) {
        return accessPermissionDataImportService.process(reader,ctx);
    }

    @Override
    public PageResult<AccessPermission2UserGroupRelation> importAccessPermissions2UserGroupRelations(Reader reader, AccessControlContext ctx) {
        return accessPermission2UserGroupRelationDataImportService.process(reader,ctx);
    }

    @Override
    public Boolean isUserAuthorizedForResourceAndPermission(String userId, String resource, String permission) {
        boolean isAuthorized=false;
        if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(resource) || StringUtils.isEmpty(permission))
        {
            throw new IllegalArgumentException("userid or resource or permission cannot be null or empty");
        }
        User user=getUserById(userId);
        AccessPermission permission1=accessPermissionRepository.findByResourceAndPermission(resource,permission);
        PageResult<UserGroup> groups=getAllUserGroupsForUser(user.getUserId(),-1);
        if(Objects.nonNull(permission1) && CollectionUtils.isNotEmpty(groups.getResults()))
        {

            List<AccessPermission> permissions=accessPermissionRepository.findPermissionInUserGroupsByResourceAndPermission(permission,resource,groups.getResults().stream().map(group->group.getCode()).collect(Collectors.toList()));
            isAuthorized=CollectionUtils.isNotEmpty(permissions);
        }
        else
        {
            throw new AccessControlException("No Such user or permission found");
        }
        return isAuthorized;
    }

    @Override
    public Boolean isUserGroupAuthorizedForResourceAndPermission(String userGroupCode, String resource, String permission) {
        boolean isAuthorized=false;
        if(StringUtils.isEmpty(userGroupCode) || StringUtils.isEmpty(resource) || StringUtils.isEmpty(permission))
        {
            throw new IllegalArgumentException("usergroupcode or resource or permission cannot be null or empty");
        }
        UserGroup group=getUserGroupByCode(userGroupCode);
        AccessPermission permission1=accessPermissionRepository.findByResourceAndPermission(resource,permission);
        PageResult<UserGroup> groups=getAllUserGroupsForUserGroup(group.getCode(),-1);
        if(Objects.nonNull(permission1) && Objects.nonNull(groups.getResults()))
        {
            List<UserGroup> groupsForQuery=new ArrayList<>(groups.getResults());
            groupsForQuery.add(group);
            List<AccessPermission> permissions=accessPermissionRepository.findPermissionInUserGroupsByResourceAndPermission(permission,resource,groupsForQuery.stream().map(g->g.getCode()).collect(Collectors.toList()));
            isAuthorized=CollectionUtils.isNotEmpty(permissions);
        }
        else
        {
            throw new AccessControlException("No Such usergroup or permission found");
        }
        return isAuthorized;
    }

    @Override
    public void exportData(Writer writer, Class dataModelClass,Integer pageNumber,Integer limit) {
        if(Objects.isNull(writer) || Objects.isNull(dataModelClass) || Objects.isNull(pageNumber) || Objects.isNull(limit) || limit<-1 || pageNumber<1 )
        {
            throw new IllegalArgumentException("Invalid input for export data.");
        }
        String className=dataModelClass.getSimpleName();
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        try{
            switch (className) {
                case "User":
                    beanToCsv.write(userRepository.findAll(PageRequest.of(pageNumber - 1, getLimitByRepositoryAndPage(userRepository,limit))).getContent());
                    break;
                case "UserGroup":
                    beanToCsv.write(userGroupRepository.findAll(PageRequest.of(pageNumber - 1, getLimitByRepositoryAndPage(userGroupRepository,limit))).getContent());
                    break;
                case "User2UserGroupRelation":
                    beanToCsv.write(user2UserGroupRelationRepository.findAll(PageRequest.of(pageNumber - 1, getLimitByRepositoryAndPage(user2UserGroupRelationRepository,limit))).getContent());
                    break;
                case "UserGroup2UserGroupRelation":
                    beanToCsv.write(userGroup2UserGroupRelationRepository.findAll(PageRequest.of(pageNumber - 1, getLimitByRepositoryAndPage(userGroup2UserGroupRelationRepository,limit))).getContent());
                    break;
                case "AccessPermission":
                    beanToCsv.write(accessPermissionRepository.findAll(PageRequest.of(pageNumber - 1, getLimitByRepositoryAndPage(accessPermissionRepository,limit))).getContent());
                    break;
                case "AccessPermission2UserGroupRelation":
                    beanToCsv.write(accessPermission2UserGroupRelationRepository.findAll(PageRequest.of(pageNumber - 1, getLimitByRepositoryAndPage(accessPermission2UserGroupRelationRepository,limit))).getContent());
                    break;
                case "ChangeLog":
                    beanToCsv.write(changeLogRepository.findAll(PageRequest.of(pageNumber - 1, getLimitByRepositoryAndPage(changeLogRepository,limit))).getContent());
                    break;
                    default:
                        log.info("other types not supported");

            }
        }
        catch (Exception e)
        {
            log.error(ERROR_IN_EXPORT,dataModelClass.getSimpleName(),e);
            throw new AccessControlException(ERROR_IN_EXPORT+dataModelClass.getSimpleName(),e);
        }
        finally {
            handleWriterClose(writer,dataModelClass);
        }
    }

    private void handleWriterClose(Writer writer, Class dataModelClass) {
        if(Objects.nonNull(writer))
        {
            try {
                writer.close();
            } catch (IOException e) {
                log.error(ERROR_IN_EXPORT,dataModelClass.getSimpleName(),e);
            }
        }
    }


    private int getLimitByRepositoryAndPage(CrudRepository repository, int limit)
    {
        return (limit < 0) ? (int) repository.count() : limit;
    }
}
