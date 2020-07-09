package com.accesscontrol.services;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.models.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Reader;
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

    UserDetailsService getUserDetailsService();

    PasswordEncoder getPasswordEncoder();

    PageResult<User> importUsers(List<User> users, AccessControlContext ctx);

    PageResult<UserGroup> importUserGroups(List<UserGroup> userGroups, AccessControlContext ctx);

    PageResult<UserGroup2UserGroupRelation> importUserGroupRelations(List<UserGroup2UserGroupRelation> relations, AccessControlContext ctx);

    PageResult<User2UserGroupRelation> importUser2UserGroupRelations(List<User2UserGroupRelation> relations, AccessControlContext ctx);

    PageResult<AccessPermission> importAccessPermissions(List<AccessPermission> permissions, AccessControlContext ctx);

    PageResult<AccessPermission2UserGroupRelation> importAccessPermissions2UserGroupRelations(List<AccessPermission2UserGroupRelation> relations, AccessControlContext ctx);


    PageResult<User> importUsers(Reader reader, AccessControlContext ctx);

    PageResult<UserGroup> importUserGroups(Reader reader, AccessControlContext ctx);

    PageResult<UserGroup2UserGroupRelation> importUserGroupRelations(Reader reader, AccessControlContext ctx);

    PageResult<User2UserGroupRelation> importUser2UserGroupRelations(Reader reader, AccessControlContext ctx);

    PageResult<AccessPermission> importAccessPermissions(Reader reader, AccessControlContext ctx);

    PageResult<AccessPermission2UserGroupRelation> importAccessPermissions2UserGroupRelations(Reader reader, AccessControlContext ctx);

    Boolean isUserAuthorizedForResourceAndPermission(String userId,String resource,String permission);
    Boolean isUserGroupAuthorizedForResourceAndPermission(String userGroupCode,String resource, String permission);

}
