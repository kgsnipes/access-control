package com.accesscontrol.services;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.models.User;
import com.accesscontrol.models.UserGroup;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface UserService {

    User createUser(User user, AccessControlContext ctx);

    User saveUser(User user, AccessControlContext ctx);

    void disableUser(String userId, AccessControlContext ctx);

    void enableUser(String userId, AccessControlContext ctx);

    void deleteUser(String userId, AccessControlContext ctx);

    User getUserById(String userId);

    UserGroup createUserGroup(UserGroup userGroup, AccessControlContext ctx);

    UserGroup saveUserGroup(UserGroup userGroup, AccessControlContext ctx);

    void disableUserGroup(String userGroupCode, AccessControlContext ctx);

    void deleteUserGroup(String userGroupCode, AccessControlContext ctx);

    PageResult<User> findUsers(String searchTerm, Integer pageNumber);

    PageResult<UserGroup> findUserGroups(String searchTerm, Integer pageNumber);


}
