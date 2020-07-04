package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.exception.UserGroupNotFoundException;
import com.accesscontrol.exception.UserNotFoundException;
import com.accesscontrol.models.User;
import com.accesscontrol.models.User2UserGroupRelation;
import com.accesscontrol.models.UserGroup;
import com.accesscontrol.models.UserGroup2UserGroupRelation;
import com.accesscontrol.repository.User2UserGroupRelationRepository;
import com.accesscontrol.repository.UserGroup2UserGroupRelationRepository;
import com.accesscontrol.repository.UserGroupRepository;
import com.accesscontrol.repository.UserRepository;
import com.accesscontrol.services.ChangeLogService;
import com.accesscontrol.services.PasswordEncryptionService;
import com.accesscontrol.services.UserService;
import com.accesscontrol.util.AccessControlUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class DefaultUserService implements UserService {

    private static Logger log= LogManager.getLogger(DefaultUserService.class);

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
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;

    @Transactional
    @Override
    public User createUser(User user, AccessControlContext ctx) {
        User savedUser=null;
        if(Objects.isNull(user))
        {
            throw new IllegalArgumentException("User object cannot be null");
        }

        Validator validator=validatorFactory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if(CollectionUtils.isNotEmpty(violations))
        {
            String errorMsg= violations.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
            throw new AccessControlException(errorMsg);
        }
        else
        {
            User existingUser=userRepository.findByUserId(user.getUserId());
            if(Objects.nonNull(existingUser))
            {
                log.error("User ID already existing "+user.getUserId());
                throw new AccessControlException("User ID already existing "+user.getUserId());
            }
            encryptPasswordIfNotEncrypted(user);
            savedUser=userRepository.save(user);
            changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,user,savedUser,ctx);
        }
        return savedUser;
    }

    private void encryptPasswordIfNotEncrypted(User user)
    {
        if(!passwordEncryptionService.isPasswordEncrypted(user.getPassword()))
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
            throw new IllegalArgumentException("User object cannot be null");
        }

        Validator validator=validatorFactory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if(CollectionUtils.isNotEmpty(violations))
        {
            String errorMsg= violations.stream().map(violation->violation.getMessage()).collect(Collectors.joining(","));
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
                changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.UPDATE,user,savedUser,ctx);

            }
            else
            {
                return createUser(user,ctx);
            }

        }
        return savedUser;
    }

    @Transactional
    @Override
    public void disableUser(String userId, AccessControlContext ctx) {

        if(StringUtils.isEmpty(userId))
        {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        User user=userRepository.findByUserId(userId);
        if(Objects.isNull(user))
        {
            throw new UserNotFoundException("No such user available");
        }
        else
        {
            user.setEnabled(false);
            User savedUser=userRepository.save(user);
            changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.UPDATE,user,savedUser,ctx);

        }

    }

    @Transactional
    @Override
    public void enableUser(String userId, AccessControlContext ctx) {
        if(StringUtils.isEmpty(userId))
        {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        User user=userRepository.findByUserId(userId);
        if(Objects.isNull(user))
        {
            throw new UserNotFoundException("No such user available");
        }
        else
        {
            user.setEnabled(true);
            User savedUser=userRepository.save(user);
            changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.UPDATE,user,savedUser,ctx);

        }
    }

    @Transactional
    @Override
    public void deleteUser(String userId, AccessControlContext ctx) {

        if(StringUtils.isEmpty(userId))
        {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        User user=userRepository.findByUserId(userId);
        if(Objects.isNull(user))
        {
            throw new UserNotFoundException("No such user available");
        }
        else
        {
            changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.DELETE,user,null,ctx);
            userRepository.delete(user);

        }

    }


    @Override
    public User getUserById(String userId) {

        if(StringUtils.isEmpty(userId))
        {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        User user=userRepository.findByUserId(userId);
        if(Objects.isNull(user))
        {
            throw new UserNotFoundException("No such user available");
        }
        else
        {
            return user;
        }
    }

    @Override
    public PageResult<User> findUsers(String searchTerm, Integer pageNumber) {

        if(StringUtils.isEmpty(searchTerm) || Objects.isNull(pageNumber))
        {
            throw new IllegalArgumentException("search term cannot be empty or page number cannot be null or less than 1");
        }

        Page<User> userList=userRepository.findUsers(searchTerm, AccessControlUtil.getPageParameter(userRepository,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)));

        if(Objects.nonNull(userList))
        {
            return new PageResult<User>(Collections.unmodifiableList(userList.getContent()),pageNumber,userList.getSize(), (int) userList.getTotalElements());
        }
        return new PageResult<User>(Collections.EMPTY_LIST,pageNumber,0, 0);
    }

    @Transactional
    @Override
    public UserGroup createUserGroup(UserGroup userGroup, AccessControlContext ctx) {

        UserGroup savedGroup=null;
        if(Objects.isNull(userGroup))
        {
            throw new IllegalArgumentException("User group object cannot be null");
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
            UserGroup existingGroup=userGroupRepository.findByCode(userGroup.getCode());
            if(Objects.nonNull(existingGroup))
            {
                log.error("User group with code already existing "+userGroup.getCode());
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
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        UserGroup userGroup=userGroupRepository.findByCode(userGroupCode);
        if(Objects.isNull(userGroup))
        {
            throw new UserGroupNotFoundException("No such user available");
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
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        UserGroup userGroup=userGroupRepository.findByCode(userGroupCode);
        if(Objects.isNull(userGroup))
        {
            throw new UserGroupNotFoundException("No such user available");
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
            throw new UserGroupNotFoundException("No such user available");
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

        if(StringUtils.isEmpty(searchTerm) || Objects.isNull(pageNumber))
        {
            throw new IllegalArgumentException("search term cannot be empty or page number cannot be null or less than 1");
        }

        Page<UserGroup> userGroupList=userGroupRepository.findUserGroups(searchTerm,AccessControlUtil.getPageParameter(userGroupRepository,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)));

        if(Objects.nonNull(userGroupList))
        {
            return new PageResult<UserGroup>(Collections.unmodifiableList(userGroupList.getContent()),pageNumber,userGroupList.getSize(), (int) userGroupList.getTotalElements());
        }
        return new PageResult<UserGroup>(Collections.EMPTY_LIST,pageNumber,0, 0);
    }

    @Override
    public PageResult<User> importUsers(List<User> users,Boolean updateIfExists, AccessControlContext ctx) {
        if(CollectionUtils.isEmpty(users) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("list of users cannot be empty or context cannot be null");
        }
        PageResult<User> result=new PageResult<>();
        result.setErrors(new ArrayList<>());
        result.setResults(new ArrayList<>());
        users.stream().forEach(u->{
            try {

                User existingUser=null;
                try
                {
                    existingUser=this.getUserById(u.getUserId());

                }
                catch (UserNotFoundException ex)
                {

                }
                if(Objects.nonNull(existingUser) && updateIfExists)
                {
                    result.getResults().add(saveUser(u, ctx));
                }
                else
                {
                    result.getResults().add(createUser(u, ctx));
                }

                result.getErrors().add(null);
            }
            catch (Exception e)
            {
                log.error("Exception in creating user");
                result.getResults().add(u);
                result.getErrors().add(e);
            }
        });
        return result;
    }

    @Override
    public PageResult<UserGroup> importUserGroups(List<UserGroup> userGroups,Boolean updateIfExists, AccessControlContext ctx) {
        if(CollectionUtils.isEmpty(userGroups) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("list of user groups cannot be empty or context cannot be null");
        }
        PageResult<UserGroup> result=new PageResult<>();
        result.setErrors(new ArrayList<>());
        result.setResults(new ArrayList<>());
        userGroups.stream().forEach(u->{
            try {
                UserGroup existingUserGroup=null;
                try
                {
                    existingUserGroup=this.getUserGroupByCode(u.getCode());

                }
                catch (UserGroupNotFoundException ex)
                {

                }
                if(Objects.nonNull(existingUserGroup) && updateIfExists)
                {
                    result.getResults().add(saveUserGroup(u, ctx));
                }
                else
                {
                    result.getResults().add(createUserGroup(u, ctx));
                }


                result.getErrors().add(null);
            }
            catch (Exception e)
            {
                result.getResults().add(u);
                result.getErrors().add(e);
            }
        });
        return result;
    }

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

    @Override
    public void addUserGroupToUserGroup(String childUserGroupCode, String parentUserGroupCode, AccessControlContext ctx) {
        if(StringUtils.isEmpty(childUserGroupCode) || StringUtils.isEmpty(parentUserGroupCode) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("usergroup cannot be empty or context is null");
        }
        addUserGroupToUserGroup(getUserGroupByCode(childUserGroupCode),getUserGroupByCode(parentUserGroupCode),ctx);
    }

    @Transactional
    @Override
    public void addUserGroupToUserGroup(UserGroup childUserGroup, UserGroup parentUserGroup, AccessControlContext ctx) {

        if(Objects.isNull(childUserGroup) || Objects.isNull(parentUserGroup) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("user or usergroup cannot be empty or context is null");
        }
        UserGroup2UserGroupRelation relation=userGroup2UserGroupRelationRepository.findByChildUserGroupCodeAndParentUserGroupCode(childUserGroup.getCode(),parentUserGroup.getCode());
        if(Objects.isNull(relation))
        {
            UserGroup2UserGroupRelation relationToSave=new UserGroup2UserGroupRelation(childUserGroup.getCode(),parentUserGroup.getCode());
            UserGroup2UserGroupRelation saved=userGroup2UserGroupRelationRepository.save(relationToSave);
            changeLogService.logChange(saved.getId(),saved.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.CREATE,relationToSave,saved,ctx);
        }

    }

    @Override
    public void removeUserGroupFromUserGroup(String childUserGroupCode, String parentUserGroupCode, AccessControlContext ctx) {

        if(StringUtils.isEmpty(childUserGroupCode) || StringUtils.isEmpty(parentUserGroupCode) || Objects.isNull(ctx))
        {
            throw new IllegalArgumentException("usergroup cannot be empty or context is null");
        }
        removeUserGroupFromUserGroup(getUserGroupByCode(childUserGroupCode),getUserGroupByCode(parentUserGroupCode),ctx);

    }

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
        if(StringUtils.isEmpty(userId) || Objects.isNull(pageNumber) || pageNumber<1)
        {
            throw new IllegalArgumentException("user id is empty or pagenumber is invalid");
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> allGroups=new HashSet<UserGroup>();
        User user=getUserById(userId);
        if(Objects.nonNull(user))
        {
            PageResult<UserGroup> immediateUserGroups=getParentUserGroupsForUser(userId,-1);
            if(Objects.nonNull(immediateUserGroups) && CollectionUtils.isNotEmpty(immediateUserGroups.getResults()))
            {
                allGroups.addAll(immediateUserGroups.getResults());
                immediateUserGroups.getResults().stream().forEach(group->{
                   getParentGroupsForUserGroup(group.getCode(),allGroups);
                });
            }
        }

        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(allGroups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
           log.error("Error while fetching user groups",e);
        }
        return result;
    }

    @Override
    public PageResult<UserGroup> getParentUserGroupsForUser(String userId, Integer pageNumber) {
        if(StringUtils.isEmpty(userId) || Objects.isNull(pageNumber) || pageNumber<1)
        {
            throw new IllegalArgumentException("user id is empty or pagenumber is invalid");
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> groups=new HashSet<UserGroup>();
        User user=getUserById(userId);
        if(Objects.nonNull(user)){
            Page<User2UserGroupRelation> user2UserGroupRelation=user2UserGroupRelationRepository.findByUserId("userId");
            if(CollectionUtils.isNotEmpty(user2UserGroupRelation.getContent()))
            {
                user2UserGroupRelation.getContent().stream().forEach(rel -> groups.add(getUserGroupByCode(rel.getUserGroupCode())));
            }
        }
        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(groups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("Error while fetching user groups",e);
        }

        return result;
    }

    @Override
    public PageResult<UserGroup> getAllUserGroupsForUserGroup(String userGroupCode, Integer pageNumber) {
        if(StringUtils.isEmpty(userGroupCode) || Objects.isNull(pageNumber))
        {
            throw new IllegalArgumentException("user id is empty or pagenumber is invalid");
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> groups=new HashSet<UserGroup>();
        UserGroup userGroup=getUserGroupByCode(userGroupCode);
        if(Objects.nonNull(userGroup))
        {
            Page<UserGroup2UserGroupRelation> immediateGroups=userGroup2UserGroupRelationRepository.findByChildUserGroupCode(userGroupCode);
            if(CollectionUtils.isNotEmpty(immediateGroups.getContent())){
                immediateGroups.getContent().stream().forEach(ug->{
                    getParentGroupsForUserGroup(ug.getParentUserGroupId(),groups);
                });

            }

        }

        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(groups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("Error while fetching user groups",e);
        }
        return result;
    }

    @Override
    public PageResult<UserGroup> getParentUserGroupsForUserGroup(String userGroupCode, Integer pageNumber) {
        if(StringUtils.isEmpty(userGroupCode) || Objects.isNull(pageNumber))
        {
            throw new IllegalArgumentException("user id is empty or pagenumber is invalid");
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> groups=new HashSet<UserGroup>();
        UserGroup userGroup=getUserGroupByCode(userGroupCode);
        if(Objects.nonNull(userGroup))
        {
            Page<UserGroup2UserGroupRelation> relations =userGroup2UserGroupRelationRepository.findByChildUserGroupCode(userGroupCode);
            if(CollectionUtils.isNotEmpty(relations.getContent()))
            {
                relations.getContent().stream().forEach(r->{
                    groups.add(getUserGroupByCode(r.getParentUserGroupId()));
                });
            }
        }
        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(groups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("Error while fetching user groups",e);
        }
        return result;
    }

    @Override
    public PageResult<UserGroup> getAllChildUserGroupsForUserGroup(String userGroupCode, Integer pageNumber) {
        if(StringUtils.isEmpty(userGroupCode) || Objects.isNull(pageNumber))
        {
            throw new IllegalArgumentException("user id is empty or pagenumber is invalid");
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> groups=new HashSet<UserGroup>();
        UserGroup userGroup=getUserGroupByCode(userGroupCode);
        if(Objects.nonNull(userGroup))
        {
            Page<UserGroup2UserGroupRelation> relations =userGroup2UserGroupRelationRepository.findByParentUserGroupCode(userGroupCode);
            if(CollectionUtils.isNotEmpty(relations.getContent()))
            {
                relations.getContent().stream().forEach(r->{
                    getChildGroupsForUserGroup(r.getChildUserGroupId(),groups);
                });
            }
        }
        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(groups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("Error while fetching user groups",e);
        }
        return result;
    }

    @Override
    public PageResult<UserGroup> getChildUserGroupsForUserGroup(String userGroupCode, Integer pageNumber) {
        if(StringUtils.isEmpty(userGroupCode) || Objects.isNull(pageNumber))
        {
            throw new IllegalArgumentException("user id is empty or pagenumber is invalid");
        }
        PageResult<UserGroup> result=new PageResult<>();
        HashSet<UserGroup> groups=new HashSet<UserGroup>();
        UserGroup userGroup=getUserGroupByCode(userGroupCode);
        if(Objects.nonNull(userGroup))
        {
            Page<UserGroup2UserGroupRelation> relations =userGroup2UserGroupRelationRepository.findByParentUserGroupCode(userGroupCode);
            if(CollectionUtils.isNotEmpty(relations.getContent()))
            {
                relations.getContent().stream().forEach(r->{
                    groups.add(getUserGroupByCode(r.getChildUserGroupId()));
                });
            }
        }
        try {
            result.setResults(Collections.unmodifiableCollection(new ArrayList<>(AccessControlUtil.getPagedResult(groups,pageNumber,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)))));
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("Error while fetching user groups",e);
        }
        return result;
    }


    private void getParentGroupsForUserGroup(String userGroupCode,Collection<UserGroup> groups)
    {
        Page<UserGroup2UserGroupRelation> relations =userGroup2UserGroupRelationRepository.findByChildUserGroupCode(userGroupCode);
        if(CollectionUtils.isNotEmpty(relations.getContent()))
        {
            groups.add(getUserGroupByCode(userGroupCode));
            relations.getContent().stream().forEach(r->{
                getParentGroupsForUserGroup(r.getParentUserGroupId(),groups);
            });
        }
        else
        {
            groups.add(getUserGroupByCode(userGroupCode));
        }

    }

    private void getChildGroupsForUserGroup(String userGroupCode,Collection<UserGroup> groups)
    {
        Page<UserGroup2UserGroupRelation> relations =userGroup2UserGroupRelationRepository.findByParentUserGroupCode(userGroupCode);
        if(CollectionUtils.isNotEmpty(relations.getContent()))
        {
            groups.add(getUserGroupByCode(userGroupCode));
            relations.getContent().stream().forEach(r->{
                getChildGroupsForUserGroup(r.getChildUserGroupId(),groups);
            });
        }
        else
        {
            groups.add(getUserGroupByCode(userGroupCode));
        }

    }


}
