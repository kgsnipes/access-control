package com.accesscontrol.services;

import com.accesscontrol.beans.PageResult;
import com.accesscontrol.models.User;
import com.accesscontrol.models.UserGroup;

import java.util.List;

public interface UserService {

    User createUser(User user);

    User saveUser(User user);

    void disableUser(String userId);

    void deleteUser(String userId);

    User getUserById(String userId);

    PageResult<User> findUsers(String searchTerm, Integer pageNumber);

    UserGroup createUserGroup(UserGroup userGroup);

    UserGroup saveUserGroup(UserGroup userGroup);

    void disableUserGroup(String userGroupCode);

    void deleteUserGroup(String userGroupCode);

    PageResult<UserGroup> findUserGroups(String searchTerm, Integer pageNumber);

}
