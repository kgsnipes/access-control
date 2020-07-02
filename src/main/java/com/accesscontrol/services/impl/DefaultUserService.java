package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.exception.UserNotFoundException;
import com.accesscontrol.models.ChangeLog;
import com.accesscontrol.models.User;
import com.accesscontrol.models.UserGroup;
import com.accesscontrol.repository.ChangeLogRepository;
import com.accesscontrol.repository.UserRepository;
import com.accesscontrol.services.ChangeLogService;
import com.accesscontrol.services.PasswordEncryptionService;
import com.accesscontrol.services.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;


public class DefaultUserService implements UserService {

    private static Logger log= LogManager.getLogger(DefaultUserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ValidatorFactory validatorFactory;

    @Autowired
    private PasswordEncryptionService passwordEncryptionService;

    @Autowired
    private ChangeLogService changeLogService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

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
            userRepository.delete(user);
            changeLogService.logChange(user.getId(),user.getClass().getSimpleName(), AccessControlConfigConstants.CRUD.DELETE,user,null,ctx);

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

        if(StringUtils.isEmpty(searchTerm) || Objects.isNull(pageNumber) || pageNumber<0)
        {
            throw new IllegalArgumentException("search term cannot be empty or page number cannot be null or less than 1");
        }



        Page<User> userList=userRepository.findUsers(searchTerm,PageRequest.of(pageNumber-1,(Integer) accessControlConfigProperties.get(AccessControlConfigConstants.PAGINATION_PAGELIMIT)));

        if(Objects.nonNull(userList))
        {
            return new PageResult<User>(userList.getContent(),pageNumber,userList.getSize(), (int) userList.getTotalElements());
        }
        return new PageResult<User>(Collections.EMPTY_LIST,pageNumber,0, 0);
    }

    @Transactional
    @Override
    public UserGroup createUserGroup(UserGroup userGroup, AccessControlContext ctx) {
        return null;
    }

    @Transactional
    @Override
    public UserGroup saveUserGroup(UserGroup userGroup, AccessControlContext ctx) {
        return null;
    }

    @Transactional
    @Override
    public void disableUserGroup(String userGroupCode, AccessControlContext ctx) {

    }

    @Transactional
    @Override
    public void deleteUserGroup(String userGroupCode, AccessControlContext ctx) {

    }

    @Override
    public PageResult<UserGroup> findUserGroups(String searchTerm, Integer pageNumber) {
        return null;
    }


}
