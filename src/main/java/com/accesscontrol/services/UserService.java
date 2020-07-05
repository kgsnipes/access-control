package com.accesscontrol.services;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.models.AccessPermission;
import com.accesscontrol.models.User;
import com.accesscontrol.models.UserGroup;

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

    void enableUserGroup(String userGroupCode, AccessControlContext ctx);

    void deleteUserGroup(String userGroupCode, AccessControlContext ctx);

    UserGroup getUserGroupByCode(String code);

    PageResult<User> findUsers(String searchTerm, Integer pageNumber);

    PageResult<UserGroup> findUserGroups(String searchTerm, Integer pageNumber);

    PageResult<User> importUsers(List<User> users,Boolean updateIfExists,AccessControlContext ctx);

    PageResult<UserGroup> importUserGroups(List<UserGroup> userGroups,Boolean updateIfExists,AccessControlContext ctx);

    void addUserToUserGroup(String userId, String userGroupCode,AccessControlContext ctx);

    void addUserToUserGroup(User user, UserGroup userGroup,AccessControlContext ctx );

    void removeUserFromUserGroup(String userId, String userGroupCode,AccessControlContext ctx);

    void removeUserFromUserGroup(User user, UserGroup userGroup,AccessControlContext ctx );

    void addUserGroupToUserGroup(String childUserGroupCode,String parentUserGroupCode,AccessControlContext ctx );

    void addUserGroupToUserGroup(UserGroup childUserGroup,UserGroup parentUserGroup,AccessControlContext ctx );

    void removeUserGroupFromUserGroup(String childUserGroupCode,String parentUserGroupCode,AccessControlContext ctx );

    void removeUserGroupFromUserGroup(UserGroup childUserGroup,UserGroup parentUserGroup,AccessControlContext ctx );

    PageResult<UserGroup> getAllUserGroupsForUser(String userId, Integer pageNumber);

    PageResult<UserGroup> getParentUserGroupsForUser(String userId, Integer pageNumber);

    PageResult<UserGroup> getAllUserGroupsForUserGroup(String userGroupCode, Integer pageNumber);

    PageResult<UserGroup> getParentUserGroupsForUserGroup(String userGroupCode, Integer pageNumber);

    PageResult<UserGroup> getAllChildUserGroupsForUserGroup(String userGroupCode, Integer pageNumber);

    PageResult<UserGroup> getChildUserGroupsForUserGroup(String userGroupCode, Integer pageNumber);

    AccessPermission createPermission(AccessPermission permission, AccessControlContext ctx);

    AccessPermission createPermission(AccessPermission permission, UserGroup userGroup ,AccessControlContext ctx);

    void enablePermission(AccessPermission permission, UserGroup userGroup ,AccessControlContext ctx);

    void disablePermission(AccessPermission permission, UserGroup userGroup ,AccessControlContext ctx);

    PageResult<AccessPermission> getPermissionsForUserGroup(String userGroupCode,Boolean onlyEnabled,Integer pageNumber);

    PageResult<AccessPermission> getPermissionsByResourceAndUserGroup(String resource,String userGroupCode,Boolean onlyEnabled ,Integer pageNumber);

}
