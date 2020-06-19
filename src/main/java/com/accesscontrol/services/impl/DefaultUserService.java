package com.accesscontrol.services.impl;

import com.accesscontrol.beans.PageResult;
import com.accesscontrol.models.User;
import com.accesscontrol.models.UserGroup;
import com.accesscontrol.repository.UserRepository;
import com.accesscontrol.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


public class DefaultUserService implements UserService {

    private static Logger log= LogManager.getLogger(DefaultUserService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public User createUser(User user) {
        return null;
    }

    @Override
    public User saveUser(User user) {
        return null;
    }

    @Override
    public void disableUser(String userId) {

    }

    @Override
    public void deleteUser(String userId) {

    }

    @Override
    public User getUserById(String userId) {
        return null;
    }

    @Override
    public PageResult<User> findUsers(String searchTerm, Integer pageNumber) {
        return null;
    }

    @Override
    public UserGroup createUserGroup(UserGroup userGroup) {
        return null;
    }

    @Override
    public UserGroup saveUserGroup(UserGroup userGroup) {
        return null;
    }

    @Override
    public void disableUserGroup(String userGroupCode) {

    }

    @Override
    public void deleteUserGroup(String userGroupCode) {

    }

    @Override
    public PageResult<UserGroup> findUserGroups(String searchTerm, Integer pageNumber) {
        return null;
    }
}
