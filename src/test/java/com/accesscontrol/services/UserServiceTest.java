package com.accesscontrol.services;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.AccessControlPermissions;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.exception.UserGroupNotFoundException;
import com.accesscontrol.exception.UserNotFoundException;
import com.accesscontrol.models.*;
import com.accesscontrol.services.impl.DefaultAccessControlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    static Logger log= LogManager.getLogger(UserServiceTest.class);

    static AccessControlService accessControlService;

    static UserService userService;

    AccessControlContext ctx=new AccessControlContext("system-user","system running test cases");

    @BeforeAll
    public static void setup() throws AccessControlException {
        log.info("Setting up accessControlService");
        accessControlService=new DefaultAccessControlService();
        userService= accessControlService.getUserService();
    }

    @Order(1)
    @Test
    public void userServiceTest() throws AccessControlException {
        Assertions.assertEquals(true,Objects.nonNull( accessControlService.getUserService()));
    }

    @Order(2)
    @Test
    public void createUserTestWithException() throws AccessControlException {

        User user=new User();
        Assertions.assertThrows(AccessControlException.class,()->{
            userService.createUser(user,ctx);
        });
    }

    @Order(3)
    @Test
    public void createUserTestWithIllegalArgumentException() throws AccessControlException {

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createUser(null,ctx);
        });
    }

    @Order(4)
    @Test
    public void createUserTestWithoutException() throws AccessControlException {

        User user=new User();
        user.setPassword("123456");
        user.setEnabled(true);
        user.setFirstName("test");
        user.setLastName("user");
        user.setUserId("testuser1@test.com");
        User persistedUser=userService.createUser(user,ctx);
        Assertions.assertEquals(user.getUserId(),persistedUser.getUserId());
        Assertions.assertEquals(true, Objects.nonNull(persistedUser.getId()));
        log.info("ID for the persisted user is "+persistedUser.getId());
    }

    @Order(5)
    @Test
    public void createUserTestWithExceptionForDuplicateUser() throws AccessControlException {
        User user=new User();
        user.setPassword("123456");
        user.setEnabled(true);
        user.setFirstName("test");
        user.setLastName("user");
        user.setUserId("testuser2@test.com");
        User persistedUser=userService.createUser(user,ctx);
        User user2=new User();
        user2.setPassword("123456");
        user2.setEnabled(true);
        user2.setFirstName("test");
        user2.setLastName("user");
        user2.setUserId("testuser2@test.com");
        Assertions.assertThrows(AccessControlException.class,()->{
            userService.createUser(user2,ctx);
        });
    }

    @Order(6)
    @Test
    public void disableUserWithNullUserId()throws AccessControlException{
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.disableUser(null,ctx);
        });
    }

    @Order(7)
    @Test
    public void disableUserWithUnknownUserId()throws AccessControlException{
        Assertions.assertThrows(UserNotFoundException.class,()->{
            userService.disableUser("unknownuser@test.com",ctx);
        });
    }

    @Order(8)
    @Test
    public void disableUserWithKnownUserId()throws AccessControlException{
        String userId="testuser2@test.com";
        userService.disableUser(userId,ctx);
        Assertions.assertEquals(false,userService.getUserById(userId).getEnabled());

    }

    @Order(9)
    @Test
    public void disableUserWithKnownUserIdWithNullContext()throws AccessControlException{
        String userId="testuser2@test.com";

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.disableUser(userId,null);
        });

    }

    @Order(10)
    @Test
    public void enableUserWithKnownUserId()throws AccessControlException{
        String userId="testuser2@test.com";
        userService.enableUser(userId,ctx);
        Assertions.assertEquals(true,userService.getUserById(userId).getEnabled());

    }

    @Order(11)
    @Test
    public void enableUserWithunKnownUserId()throws AccessControlException{
        String userId="testuser3@test.com";

        Assertions.assertThrows(UserNotFoundException.class,()->{
            userService.enableUser(userId,ctx);
        });

    }


    @Order(12)
    @Test
    public void enableUserWithNoUserId()throws AccessControlException{
        String userId="";

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.enableUser(userId,ctx);
        });

    }

    @Order(13)
    @Test
    public void enableUserWithNullUserId()throws AccessControlException{
        String userId=null;

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.enableUser(userId,ctx);
        });

    }


    @Order(14)
    @Test
    public void deleteUserWithKnownUser()throws AccessControlException{
        User user=new User();
        user.setPassword("123456");
        user.setEnabled(true);
        user.setFirstName("test");
        user.setLastName("user");
        user.setUserId("testuser3@test.com");
        User persistedUser=userService.createUser(user,ctx);
        userService.deleteUser(user.getUserId(),ctx);
        Assertions.assertThrows(UserNotFoundException.class,()->{
            userService.getUserById(user.getUserId());
        });
    }

    @Order(15)
    @Test
    public void deleteUserWithunKnownUser()throws AccessControlException{

        Assertions.assertThrows(UserNotFoundException.class,()->{
            userService.deleteUser("testuser3300@test.com",ctx);
        });
    }

    @Order(16)
    @Test
    public void deleteUserWithNullUser()throws AccessControlException{

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.deleteUser(null,ctx);
        });
    }

    @Order(17)
    @Test
    public void saveUserWithProperUser()throws AccessControlException{
        User user=userService.getUserById("testuser1@test.com");
        user.setPassword("123456");
        user.setEnabled(true);
        user.setFirstName("test");
        user.setLastName("user");
        User persistedUser=userService.saveUser(user,ctx);
        Assertions.assertEquals("testuser1@test.com",persistedUser.getUserId());
    }

    @Order(18)
    @Test
    public void saveUserWithoutUserId()throws AccessControlException{
        User user=new User();
        user.setPassword("123456");
        user.setEnabled(true);
        user.setFirstName("test");
        user.setLastName("user");
        user.setUserId(null);

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            User persistedUser=userService.saveUser(user,ctx);
        });
    }

    @Order(19)
    @Test
    public void saveUserWithExistingUserId()throws AccessControlException{
        User user=new User();
        user.setPassword("123456");
        user.setEnabled(true);
        user.setFirstName("test");
        user.setLastName("user");
        user.setUserId("testuser5@test.com");
        User persistedUser=userService.createUser(user,ctx);

        persistedUser.setLastName("changed");
        User persistedUser1=userService.saveUser(persistedUser,ctx);
        Assertions.assertEquals("changed",persistedUser1.getLastName());

    }

    @Order(20)
    @Test
    public void saveUserWithExistingUserIdButWithoutID()throws AccessControlException{
        User user=new User();
        user.setPassword("123456");
        user.setEnabled(true);
        user.setFirstName("test");
        user.setLastName("user");
        user.setUserId("testuser6@test.com");
        User persistedUser=userService.createUser(user,ctx);

        persistedUser.setId(null);
        persistedUser.setLastName("changed");

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            User persistedUser1=userService.saveUser(persistedUser,ctx);
        });

    }

    @Order(21)
    @Test
    public void getUserByUserIdTest()
    {
        Assertions.assertEquals("testuser5@test.com",userService.getUserById("testuser5@test.com").getUserId());
    }

    @Order(22)
    @Test
    public void getUserByUserIdWithNullInputTest()
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.getUserById(null).getUserId();
        });
    }

    @Order(22)
    @Test
    public void getUserByUserIdWithUnknownUserTest()
    {
        Assertions.assertThrows(UserNotFoundException.class,()->{
            userService.getUserById("testuser1234567890@test.com").getUserId();
        });
    }

    @Order(23)
    @Test
    public void findUsersWithoutSearchTerm()
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.findUsers("",1);
        });
    }

    @Order(25)
    @Test
    public void findUsersWithNullSearchTerm()
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.findUsers(null,1);
        });
    }

    @Order(26)
    @Test
    public void findUsersWithValidUserId()throws Exception
    {
        String searchTerm="testuser1@test.com";
        PageResult<User> userResults=userService.findUsers(searchTerm,1);
        log.info(new ObjectMapper().writeValueAsString(userResults));
        Assertions.assertEquals(true,userResults.getResults().stream().filter(u->u.getUserId().equals(searchTerm)).findAny().isPresent());
       // Assertions.assertEquals(true,userService.findUsers(searchTerm,1).getResults().stream().filter(u->u.getUserId().equals(searchTerm)).findAny().isPresent());

    }

    @Order(27)
    @Test
    public void findUsersWithValidFirstname()throws Exception
    {
        String searchTerm="test";
        PageResult<User> userResults=userService.findUsers(searchTerm,1);
        log.info(new ObjectMapper().writeValueAsString(userResults));
        Assertions.assertEquals(true,userResults.getResults().stream().filter(u->u.getFirstName().equals(searchTerm)).findAny().isPresent());
        Assertions.assertEquals(true,userResults.getResults().size()>1);

    }

    @Order(28)
    @Test
    public void findUsersWithoutPageNumber()
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.findUsers("",null);
        });
    }

    @Order(29)
    @Test
    public void findUsersWithNegativePageNumber()
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.findUsers("",-1);
        });
    }

    @Order(30)
    @Test
    public void findUsersWithNullPageNumber()
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.findUsers("hello",null);
        });
    }

    @Order(31)
    @Test
    public void findUsersWithValidLastname()throws Exception
    {
        String searchTerm="user";
        PageResult<User> userResults=userService.findUsers(searchTerm,1);
        log.info(new ObjectMapper().writeValueAsString(userResults));
        Assertions.assertEquals(true,userResults.getResults().stream().filter(u->u.getLastName().equals(searchTerm)).findAny().isPresent());
        Assertions.assertEquals(true,userResults.getResults().size()>1);

    }


    @Order(32)
    @Test
    public void createUserGroupWithException()throws Exception
    {
        UserGroup userGroup=new UserGroup();
        userGroup.setCode(null);
        userGroup.setName("Admin Group");
        userGroup.setEnabled(true);

        Assertions.assertThrows(AccessControlException.class,()->{
            Objects.nonNull(userService.createUserGroup(userGroup,ctx).getId());
        });

    }

    @Order(33)
    @Test
    public void createUserGroup()throws Exception
    {
        UserGroup userGroup=new UserGroup();
        userGroup.setCode("admingroup");
        userGroup.setName("Admin Group");
        userGroup.setEnabled(true);

        Assertions.assertEquals(true,Objects.nonNull(userService.createUserGroup(userGroup,ctx).getId()));

    }


    @Order(34)
    @Test
    public void createUserGroupWithAccessControlException()throws Exception
    {
        UserGroup userGroup=new UserGroup();
        userGroup.setCode("admingroup");
        userGroup.setName("Admin Group");
        userGroup.setEnabled(true);

        Assertions.assertThrows(AccessControlException.class,()->{
            Objects.nonNull(userService.createUserGroup(userGroup,ctx).getId());
        });

    }

    @Order(35)
    @Test
    public void getUserGroupTest()throws Exception
    {
        UserGroup userGroup=userService.getUserGroupByCode("admingroup");
        Assertions.assertNotNull(userGroup);
        Assertions.assertTrue(userGroup.getEnabled());
        Assertions.assertEquals("admingroup",userGroup.getCode());

    }

    @Order(36)
    @Test
    public void getUserGroupTestWithUnknownGroupCode()throws Exception
    {

        Assertions.assertThrows(UserGroupNotFoundException.class,()->{
           userService.getUserGroupByCode("unknowncode");
        });


    }

    @Order(37)
    @Test
    public void getUserGroupTestWithNullGroupCode()throws Exception
    {

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.getUserGroupByCode(null);
        });

    }

    @Order(38)
    @Test
    public void saveUserGroupTest()throws Exception
    {
        UserGroup userGroup=userService.getUserGroupByCode("admingroup");
        userGroup.setName("Admin Group changed name");
        UserGroup savedUserGroup=userService.saveUserGroup(userGroup,ctx);
        Assertions.assertEquals("Admin Group changed name",savedUserGroup.getName());
    }


    @Order(39)
    @Test
    public void saveUserGroupTestWithException()throws Exception
    {
        UserGroup userGroup=new UserGroup();
        userGroup.setCode("customergroup");
        userGroup.setName("customer Group");
        userGroup.setEnabled(true);
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            UserGroup savedUserGroup=userService.saveUserGroup(userGroup,ctx);
        });
    }

    @Order(40)
    @Test
    public void enableUserGroupTest()throws Exception
    {
        userService.enableUserGroup("admingroup",ctx);
        Assertions.assertTrue(userService.getUserGroupByCode("admingroup").getEnabled());
    }

    @Order(41)
    @Test
    public void disableUserGroupTest()throws Exception
    {
        userService.disableUserGroup("admingroup",ctx);
        Assertions.assertFalse(userService.getUserGroupByCode("admingroup").getEnabled());
    }

    @Order(42)
    @Test
    public void enableUserGroupTestWithNullInput()throws Exception
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.enableUserGroup(null,ctx);
        });

    }

    @Order(43)
    @Test
    public void enableUserGroupTestWithNullCtx()throws Exception
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.enableUserGroup(null,null);
        });

    }

    @Order(44)
    @Test
    public void enableUserGroupTestWithUnknownGroup()throws Exception
    {
        Assertions.assertThrows(UserGroupNotFoundException.class,()->{
            userService.enableUserGroup("unknowngroup",ctx);
        });

    }

    @Order(45)
    @Test
    public void enableUserGroupTestWithKnownGroupAndNullContext()throws Exception
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.enableUserGroup("admingroup",null);
        });

    }

    @Order(46)
    @Test
    public void disableUserGroupTestWithKnownGroupAndNullContext()throws Exception
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.disableUserGroup("admingroup",null);
        });

    }

    @Order(47)
    @Test
    public void disableUserGroupTestWithNullInput()throws Exception
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.disableUserGroup(null,ctx);
        });

    }

    @Order(48)
    @Test
    public void disableUserGroupTestWithNullCtx()throws Exception
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.disableUserGroup(null,null);
        });

    }

    @Order(49)
    @Test
    public void disableUserGroupTestWithUnknownGroup()throws Exception
    {
        Assertions.assertThrows(UserGroupNotFoundException.class,()->{
            userService.disableUserGroup("unknowngroup",ctx);
        });

    }


    @Order(50)
    @Test
    public void importUsersTest()throws Exception
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/users.csv"));
        PageResult<User> result=userService.importUsers(inputStreamReader,ctx);
        result.getResults().stream().forEach(user -> {
            log.info("user id :"+user.getId());
        });
        Assertions.assertEquals(28,result.getResults().size());
    }

    @Order(51)
    @Test
    public void importUserGroupsTest()throws Exception
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/usergroup.csv"));


        PageResult<UserGroup> result=userService.importUserGroups(inputStreamReader,ctx);
        result.getResults().stream().forEach(ug -> {
            log.info("user group id :"+ug.getId());
        });

        Assertions.assertEquals(27,result.getResults().size());

    }


    @Order(52)
    @Test
    public void importUserGroupsTestWithException()throws Exception
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/usergroup.csv"));


        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> result=userService.importUserGroups(inputStreamReader,null);
        });

    }

    @Order(53)
    @Test
    public void importUsersTestWithException()throws Exception
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/users.csv"));


        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<User> result=userService.importUsers(inputStreamReader,null);

        });

    }



    @Order(54)
    @Test
    public void importUsersTestWithNullListAndContext()throws Exception
    {

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<User> result=userService.importUsers((Reader) null,null);

        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<User> result=userService.importUsers((Reader) null,ctx);

        });

    }

    @Order(55)
    @Test
    public void importUserGroupsTestWithNullListAndContext()throws Exception
    {

        Assertions.assertThrows(IllegalArgumentException.class,()->{
           userService.importUserGroups(Collections.emptyList(),null);

        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.importUsers((Reader) null,ctx);

        });

    }



    @Order(56)
    @Test
    public void importUsersTestWithMissingUserId()throws Exception
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/users_without_userId.csv"));

        PageResult<User> result=userService.importUsers(inputStreamReader,ctx);

        result.getErrors().stream().forEach(err->{
            log.info(err.getMessage());
        });

        Assertions.assertNotNull(result.getErrors());
        Assertions.assertNotNull(result.getErrors().iterator().next());

    }

    @Order(57)
    @Test
    public void findUserGroupsTest()throws Exception
    {
        PageResult<UserGroup> userGroups=userService.findUserGroups("group",1);
        Assertions.assertFalse(userGroups.getResults().isEmpty());

    }

    @Order(58)
    @Test
    public void findUserGroupsTestWithoutSearchTerm()throws Exception
    {

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> userGroups=userService.findUserGroups("",1);
        });

    }

    @Order(59)
    @Test
    public void findUserGroupsTestWithSearchTermAndInvalidPageNumber()throws Exception
    {

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> userGroups=userService.findUserGroups("group",-2);
        });

    }

    @Order(60)
    @Test
    public void findUserGroupsTestWithoutSearchTermAndInvalidPageNumber()throws Exception
    {

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> userGroups=userService.findUserGroups(null,-1);
        });

    }

    @Order(61)
    @Test
    public void deleteUserGroupTest()
    {
        UserGroup userGroup=new UserGroup();
        userGroup.setCode("fordeletion");
        userGroup.setName("fordeletion");
        userGroup.setEnabled(true);
        userService.createUserGroup(userGroup,ctx);
        Assertions.assertNotNull(userService.getUserGroupByCode("fordeletion"));
        userService.deleteUserGroup("fordeletion",ctx);
        Assertions.assertThrows(UserGroupNotFoundException.class,()->{
            userService.getUserGroupByCode("fordeletion");
        });
    }

    @Order(62)
    @Test
    public void deleteUserGroupTestWithNullUserGroup()
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.deleteUserGroup(null,ctx);
        });
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.deleteUserGroup(null,null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.deleteUserGroup("",ctx);
        });

        Assertions.assertThrows(UserGroupNotFoundException.class,()->{
            userService.deleteUserGroup("thisisnotintheDB",ctx);
        });
    }

    @Order(63)
    @Test
    public void addUserToUserGroupTest()
    {
        User user=new User();
        user.setEnabled(true);
        user.setUserId("usertoaddtogroup");
        user.setFirstName("user");
        user.setLastName("addtogroup");
        user.setPassword("123456");
        userService.createUser(user,ctx);
        UserGroup userGroup=new UserGroup();
        userGroup.setCode("foraddinguser");
        userGroup.setName("foraddinguser");
        userGroup.setEnabled(true);
        userService.createUserGroup(userGroup,ctx);

        UserGroup userGroup1=new UserGroup();
        userGroup1.setCode("foraddinguser1");
        userGroup1.setName("foraddinguser1");
        userGroup1.setEnabled(true);
        userService.createUserGroup(userGroup1,ctx);

        userService.addUserToUserGroup("usertoaddtogroup","foraddinguser",ctx);
        userService.addUserToUserGroup("usertoaddtogroup","foraddinguser1",ctx);
        PageResult<UserGroup> userGroups=userService.getAllUserGroupsForUser("usertoaddtogroup",1);
        userGroups.getResults().stream().forEach(ug -> log.info(ug.getCode()));
        Assertions.assertTrue(userGroups.getResults().stream().filter(ug->ug.getCode().equals("foraddinguser")).findAny().isPresent());
        Assertions.assertEquals(2,userGroups.getResults().size());
    }

    @Order(64)
    @Test
    public void addUserToUserGroupTestWithUsergroupHierarchy()
    {
        User user=new User();
        user.setEnabled(true);
        user.setUserId("tesuser1234");
        user.setFirstName("test");
        user.setLastName("user");
        user.setPassword("123456");
        userService.createUser(user,ctx);

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup10","usergroup10",true),ctx);
        UserGroup userGroup11=userService.createUserGroup(new UserGroup("usergroup11","usergroup11",true),ctx);
        UserGroup userGroup12=userService.createUserGroup(new UserGroup("usergroup12","usergroup12",true),ctx);
        UserGroup userGroup13=userService.createUserGroup(new UserGroup("usergroup13","usergroup13",true),ctx);

        userService.addUserGroupToUserGroup(userGroup11,userGroup10,ctx);
        userService.addUserGroupToUserGroup(userGroup13,userGroup12,ctx);
        userService.addUserGroupToUserGroup(userGroup12,userGroup10,ctx);

        userService.addUserToUserGroup("tesuser1234","usergroup13",ctx);
        userService.addUserToUserGroup("tesuser1234","userGroup11",ctx);


        PageResult<UserGroup> userGroups=userService.getAllUserGroupsForUser("tesuser1234",1);
        userGroups.getResults().stream().forEach(ug -> log.info(ug.getCode()));
        Assertions.assertEquals(4,userGroups.getResults().size());
    }

    @Order(65)
    @Test
    public void addUserToUserGroupTestForIllegalArgumentException()
    {
        User user=new User();
        user.setEnabled(true);
        user.setUserId("tesuser123456");
        user.setFirstName("test");
        user.setLastName("user");
        user.setPassword("123456");
        userService.createUser(user,ctx);

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup100","usergroup100",true),ctx);

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserToUserGroup("","usergroup100",ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserToUserGroup("","",ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserToUserGroup("tesuser123456","usergroup100",null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserToUserGroup("tesuser123456",null,null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserToUserGroup(null,"usergroup100",null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserToUserGroup("","",null);
        });

    }

    @Order(66)
    @Test
    public void addUserToUserGroupTestWithSameUserAddedToSameGroup()
    {
        User user=new User();
        user.setEnabled(true);
        user.setUserId("tesuser1234567");
        user.setFirstName("test");
        user.setLastName("user");
        user.setPassword("123456");
        userService.createUser(user,ctx);

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup1001","usergroup1001",true),ctx);

        userService.addUserToUserGroup("tesuser1234567","usergroup1001",ctx);
        userService.addUserToUserGroup("tesuser1234567","usergroup1001",ctx);
        PageResult<UserGroup> userGroups=userService.getAllUserGroupsForUser("tesuser1234567",1);
        userGroups.getResults().stream().forEach(ug -> log.info(ug.getCode()));
        Assertions.assertEquals(1,userGroups.getResults().size());

    }

    @Order(67)
    @Test
    public void removeUserFromUserGroupWithIllegalArguments()
    {
        User user=new User();
        user.setEnabled(true);
        user.setUserId("tesuser12345678");
        user.setFirstName("test");
        user.setLastName("user");
        user.setPassword("123456");
        userService.createUser(user,ctx);

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup10012","usergroup10012",true),ctx);
        userService.addUserToUserGroup("tesuser12345678","usergroup10012",ctx);

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserFromUserGroup("","usergroup10012",ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserFromUserGroup("","",ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserFromUserGroup("tesuser12345678","usergroup10012",null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserFromUserGroup("tesuser12345678",null,null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserFromUserGroup(null,"usergroup10012",null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserFromUserGroup("","",null);
        });

    }

    @Order(68)
    @Test
    public void removeUserFromUserGroupTest()
    {
        User user=new User();
        user.setEnabled(true);
        user.setUserId("tesuser123456789");
        user.setFirstName("test");
        user.setLastName("user");
        user.setPassword("123456");
        userService.createUser(user,ctx);

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup100123","usergroup100123",true),ctx);
        userService.addUserToUserGroup("tesuser123456789","usergroup100123",ctx);

        PageResult<UserGroup> userGroups=userService.getAllUserGroupsForUser("tesuser123456789",1);
        userGroups.getResults().stream().forEach(ug -> log.info(ug.getCode()));
        Assertions.assertEquals(1,userGroups.getResults().size());

        userService.removeUserFromUserGroup("tesuser123456789","usergroup100123",ctx);
        userGroups=userService.getAllUserGroupsForUser("tesuser123456789",1);
        userGroups.getResults().stream().forEach(ug -> log.info(ug.getCode()));
        Assertions.assertEquals(0,userGroups.getResults().size());


    }


    @Order(69)
    @Test
    public void addUserGroupToUserGroupWithIllegalArgument()
    {

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup1001234","usergroup1001234",true),ctx);
        UserGroup userGroup11=userService.createUserGroup(new UserGroup("usergroup1001235","usergroup1001235",true),ctx);
        //userService.addUserGroupToUserGroup("usergroup1001234","usergroup1001235",ctx);

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserGroupToUserGroup("","usergroup1001235",ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserGroupToUserGroup("","",ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserGroupToUserGroup("usergroup1001234","usergroup1001235",null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserGroupToUserGroup("usergroup1001234",null,null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserGroupToUserGroup(null,"usergroup1001235",null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserGroupToUserGroup("","",null);
        });


    }

    @Order(70)
    @Test
    public void addUserGroupToUserGroupTest()
    {

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup10011234","usergroup10011234",true),ctx);
        UserGroup userGroup11=userService.createUserGroup(new UserGroup("usergroup10011235","usergroup10011235",true),ctx);
        userService.addUserGroupToUserGroup("usergroup10011234","usergroup10011235",ctx);
        PageResult<UserGroup> groups=userService.getAllUserGroupsForUserGroup("usergroup10011234",1);
        groups.getResults().stream().forEach(ug->{
            log.info("Usergroup "+ug.getCode());
        });
        Assertions.assertEquals(1,groups.getResults().size());

    }

    @Order(71)
    @Test
    public void addUserGroupToUserGroupWithMultipleParentGroups()
    {

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup11001234","usergroup11001234",true),ctx);
        UserGroup userGroup11=userService.createUserGroup(new UserGroup("usergroup11001235","usergroup11001235",true),ctx);
        UserGroup userGroup12=userService.createUserGroup(new UserGroup("usergroup11001236","usergroup11001236",true),ctx);
        userService.addUserGroupToUserGroup("usergroup11001234","usergroup11001235",ctx);
        userService.addUserGroupToUserGroup("usergroup11001234","usergroup11001236",ctx);
        PageResult<UserGroup> groups=userService.getAllUserGroupsForUserGroup("usergroup11001234",1);
        groups.getResults().stream().forEach(ug->{
            log.info("Usergroup "+ug.getCode());
        });
        Assertions.assertEquals(2,groups.getResults().size());

    }

    @Order(72)
    @Test
    public void removeUserGroupFromUserGroupTest()
    {

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup10012341","usergroup10012341",true),ctx);
        UserGroup userGroup11=userService.createUserGroup(new UserGroup("usergroup10012351","usergroup10012351",true),ctx);
        UserGroup userGroup12=userService.createUserGroup(new UserGroup("usergroup10012361","usergroup10012361",true),ctx);
        userService.addUserGroupToUserGroup("usergroup10012341","usergroup10012351",ctx);
        userService.addUserGroupToUserGroup("usergroup10012341","usergroup10012361",ctx);
        PageResult<UserGroup> groups=userService.getAllUserGroupsForUserGroup("usergroup10012341",1);
        groups.getResults().stream().forEach(ug->{
            log.info("Usergroup "+ug.getCode());
        });
        Assertions.assertEquals(2,groups.getResults().size());
        userService.removeUserGroupFromUserGroup("usergroup10012341","usergroup10012351",ctx);
        groups=userService.getAllUserGroupsForUserGroup("usergroup10012341",1);
        groups.getResults().stream().forEach(ug->{
            log.info("Usergroup "+ug.getCode());
        });
        Assertions.assertEquals(1,groups.getResults().size());

        userService.removeUserGroupFromUserGroup("usergroup10012341","usergroup10012361",ctx);
        groups=userService.getAllUserGroupsForUserGroup("usergroup10012341",1);

        Assertions.assertEquals(0,groups.getResults().size());

    }

    @Order(73)
    @Test
    public void removeUserGroupFromUserGroupWithIllegalArguments()
    {

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup10012342","usergroup10012342",true),ctx);
        UserGroup userGroup11=userService.createUserGroup(new UserGroup("usergroup10012352","usergroup10012352",true),ctx);
        UserGroup userGroup12=userService.createUserGroup(new UserGroup("usergroup10012362","usergroup10012362",true),ctx);
        userService.addUserGroupToUserGroup("usergroup10012342","usergroup10012352",ctx);

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserGroupFromUserGroup("","usergroup10012352",ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserGroupFromUserGroup("","",ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserGroupFromUserGroup("usergroup10012342","usergroup10012352",null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserGroupFromUserGroup("usergroup10012342",null,null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserGroupFromUserGroup(null,"usergroup10012352",null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.removeUserGroupFromUserGroup("","",null);
        });

    }

    @Order(74)
    @Test
    public void getParentUserGroupsForUserGroupTest()
    {
        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup10012343","usergroup10012343",true),ctx);
        UserGroup userGroup11=userService.createUserGroup(new UserGroup("usergroup10012353","usergroup10012353",true),ctx);
        UserGroup userGroup12=userService.createUserGroup(new UserGroup("usergroup10012363","usergroup10012363",true),ctx);
        userService.addUserGroupToUserGroup("usergroup10012343","usergroup10012353",ctx);
        userService.addUserGroupToUserGroup("usergroup10012343","usergroup10012363",ctx);

        PageResult<UserGroup> groups=userService.getParentUserGroupsForUserGroup("usergroup10012343",1);

        groups.getResults().stream().forEach(ug->{
            log.info("Usergroup "+ug.getCode());
        });

        Assertions.assertEquals(2,groups.getResults().size());

        userService.removeUserGroupFromUserGroup("usergroup10012343","usergroup10012353",ctx);
        groups=userService.getParentUserGroupsForUserGroup("usergroup10012343",1);
        groups.getResults().stream().forEach(ug->{
            log.info("Usergroup "+ug.getCode());
        });
        Assertions.assertEquals(1,groups.getResults().size());

        userService.removeUserGroupFromUserGroup("usergroup10012343","usergroup10012363",ctx);
        groups=userService.getParentUserGroupsForUserGroup("usergroup10012343",1);

        Assertions.assertEquals(0,groups.getResults().size());


    }

    @Order(75)
    @Test
    public void getParentUserGroupsForUserGroupTestWithIllegalArguments()
    {
        UserGroup userGroup10=userService.createUserGroup(new UserGroup("usergroup1110012343","usergroup1110012343",true),ctx);
        UserGroup userGroup11=userService.createUserGroup(new UserGroup("usergroup1110012353","usergroup1110012353",true),ctx);
        UserGroup userGroup12=userService.createUserGroup(new UserGroup("usergroup1110012363","usergroup1110012363",true),ctx);
        userService.addUserGroupToUserGroup("usergroup10012343","usergroup1110012353",ctx);
        userService.addUserGroupToUserGroup("usergroup10012343","usergroup1110012363",ctx);



        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getParentUserGroupsForUserGroup("usergroup1110012343",null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getParentUserGroupsForUserGroup("",1);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getParentUserGroupsForUserGroup(null,1);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getParentUserGroupsForUserGroup(null,null);
        });

    }


    @Order(76)
    @Test
    public void getAllChildUserGroupsForUserGroupScenario1()
    {
        userService.createUserGroup(new UserGroup("uugg11220","uugg11220",true),ctx);
        userService.createUserGroup(new UserGroup("uugg11221","uugg112221",true),ctx);
        userService.createUserGroup(new UserGroup("uugg11222","uugg1122",true),ctx);
        userService.createUserGroup(new UserGroup("uugg11223","uugg11223",true),ctx);
        userService.createUserGroup(new UserGroup("uugg11224","uugg11224",true),ctx);
        userService.createUserGroup(new UserGroup("uugg11225","uugg11225",true),ctx);

        userService.addUserGroupToUserGroup("uugg11225","uugg11224",ctx);
        userService.addUserGroupToUserGroup("uugg11224","uugg11223",ctx);
        userService.addUserGroupToUserGroup("uugg11223","uugg11222",ctx);
        userService.addUserGroupToUserGroup("uugg11222","uugg11221",ctx);
        userService.addUserGroupToUserGroup("uugg11221","uugg11220",ctx);


        PageResult<UserGroup> groups=userService.getAllChildUserGroupsForUserGroup("uugg11220",1);
        groups.getResults().stream().forEach(userGroup -> {
            log.info("User Group  :"+userGroup.getCode());
        });
        Assertions.assertEquals(5,groups.getResults().size());


    }

    @Order(77)
    @Test
    public void getAllChildUserGroupsForUserGroupScenario2()
    {
        userService.createUserGroup(new UserGroup("uugg112201","uugg11220",true),ctx);
        userService.createUserGroup(new UserGroup("uugg112212","uugg112221",true),ctx);
        userService.createUserGroup(new UserGroup("uugg112223","uugg11222",true),ctx);
        userService.createUserGroup(new UserGroup("uugg112234","uugg11223",true),ctx);
        userService.createUserGroup(new UserGroup("uugg112245","uugg11224",true),ctx);
        userService.createUserGroup(new UserGroup("uugg112256","uugg11225",true),ctx);

        userService.addUserGroupToUserGroup("uugg112256","uugg112245",ctx);
        userService.addUserGroupToUserGroup("uugg112245","uugg112234",ctx);
        userService.addUserGroupToUserGroup("uugg112234","uugg112201",ctx);

        userService.addUserGroupToUserGroup("uugg112223","uugg112212",ctx);
        userService.addUserGroupToUserGroup("uugg112212","uugg112201",ctx);


        PageResult<UserGroup> groups=userService.getAllChildUserGroupsForUserGroup("uugg112201",1);
        groups.getResults().stream().forEach(userGroup -> {
            log.info("User Group  :"+userGroup.getCode());
        });
        Assertions.assertEquals(5,groups.getResults().size());


    }

    @Order(78)
    @Test
    public void getAllChildUserGroupsForUserGroupWithIllegalArgument()
    {
        userService.createUserGroup(new UserGroup("uugg11220i","uugg11220i",true),ctx);
//        userService.createUserGroup(new UserGroup("uugg11221","uugg112221",true),ctx);
//        userService.createUserGroup(new UserGroup("uugg11222","uugg1122",true),ctx);
//        userService.createUserGroup(new UserGroup("uugg11223","uugg11223",true),ctx);
//        userService.createUserGroup(new UserGroup("uugg11224","uugg11224",true),ctx);
//        userService.createUserGroup(new UserGroup("uugg11225","uugg11225",true),ctx);
//
//        userService.addUserGroupToUserGroup("uugg11225","uugg11224",ctx);
//        userService.addUserGroupToUserGroup("uugg11224","uugg11223",ctx);
//        userService.addUserGroupToUserGroup("uugg11223","uugg11220",ctx);
//
//        userService.addUserGroupToUserGroup("uugg11222","uugg11221",ctx);
//        userService.addUserGroupToUserGroup("uugg11221","uugg11220",ctx);



        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getAllChildUserGroupsForUserGroup("uugg11220i",-2);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getAllChildUserGroupsForUserGroup("",1);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getAllChildUserGroupsForUserGroup("uugg11220i",null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getAllChildUserGroupsForUserGroup(null,1);
        });

    }

    @Order(79)
    @Test
    public void addUserGroupToUserGroupWithChildAndParentSameGroup()
    {

        UserGroup userGroup10=userService.createUserGroup(new UserGroup("u12341","u12341",true),ctx);

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.addUserGroupToUserGroup("u12341","u12341",ctx);
        });

    }

    @Order(80)
    @Test
    public void getChildUserGroupsForUserGroupTest()
    {
        userService.createUserGroup(new UserGroup("uugg112201a","uugg11220a",true),ctx);
        userService.createUserGroup(new UserGroup("uugg112212a","uugg112221a",true),ctx);
        userService.createUserGroup(new UserGroup("uugg112223a","uugg11222a",true),ctx);
        userService.createUserGroup(new UserGroup("uugg112234a","uugg11223a",true),ctx);
        userService.createUserGroup(new UserGroup("uugg112245a","uugg11224a",true),ctx);
        userService.createUserGroup(new UserGroup("uugg112256a","uugg11225a",true),ctx);

        userService.addUserGroupToUserGroup("uugg112256a","uugg112245a",ctx);
        userService.addUserGroupToUserGroup("uugg112245a","uugg112234a",ctx);
        userService.addUserGroupToUserGroup("uugg112234a","uugg112201a",ctx);

        userService.addUserGroupToUserGroup("uugg112223a","uugg112212a",ctx);
        userService.addUserGroupToUserGroup("uugg112212a","uugg112201a",ctx);


        PageResult<UserGroup> groups=userService.getChildUserGroupsForUserGroup("uugg112201a",1);
        groups.getResults().stream().forEach(userGroup -> {
            log.info("User Group  :"+userGroup.getCode());
        });
        Assertions.assertEquals(2,groups.getResults().size());
    }

    @Order(81)
    @Test
    public void getChildUserGroupsForUserGroupTestWithIllegalArguments()
    {
        userService.createUserGroup(new UserGroup("zzuugg112201a","zzuugg11220a",true),ctx);
//        userService.createUserGroup(new UserGroup("uugg112212a","uugg112221a",true),ctx);
//        userService.createUserGroup(new UserGroup("uugg112223a","uugg11222a",true),ctx);
//        userService.createUserGroup(new UserGroup("uugg112234a","uugg11223a",true),ctx);
//        userService.createUserGroup(new UserGroup("uugg112245a","uugg11224a",true),ctx);
//        userService.createUserGroup(new UserGroup("uugg112256a","uugg11225a",true),ctx);
//
//        userService.addUserGroupToUserGroup("uugg112256a","uugg112245a",ctx);
//        userService.addUserGroupToUserGroup("uugg112245a","uugg112234a",ctx);
//        userService.addUserGroupToUserGroup("uugg112234a","uugg112201a",ctx);
//
//        userService.addUserGroupToUserGroup("uugg112223a","uugg112212a",ctx);
//        userService.addUserGroupToUserGroup("uugg112212a","uugg112201a",ctx);


        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getChildUserGroupsForUserGroup("zzuugg112201a",-2);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getChildUserGroupsForUserGroup("",1);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getChildUserGroupsForUserGroup(null,1);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> groups=userService.getChildUserGroupsForUserGroup(null,null);
        });

    }


    @Order(82)
    @Test
    public void createPermissionTest()
    {
        Assertions.assertNotNull(userService.createPermission(new AccessPermission("READ","User"),ctx));
    }

    @Order(83)
    @Test
    public void createPermissionTestWithInvalidINput()
    {
        Assertions.assertThrows(AccessControlException.class,()->{
            userService.createPermission(new AccessPermission(null,null),ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createPermission(null,ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createPermission(null,null);
        });

        Assertions.assertThrows(AccessControlException.class,()->{
            userService.createPermission(new AccessPermission(null,null),null);
        });

    }

    @Order(84)
    @Test
    public void createPermissionTestScenario1()
    {
        Assertions.assertNotNull(userService.createPermission(new AccessPermission("READ","User"),ctx));
        Assertions.assertNotNull(userService.createPermission(new AccessPermission("READ","User"),ctx));
    }

    @Order(85)
    @Test
    public void createPermissionTestScenario2()
    {
        UserGroup ug=userService.createUserGroup(new UserGroup("auugg112201a","auugg11220a",true),ctx);
        Assertions.assertNotNull(userService.createPermission(new AccessPermission("READ","User"),ug,ctx));

    }

    @Order(86)
    @Test
    public void createPermissionTestWithInvalidInputForUserGroup()
    {
        UserGroup ug=userService.createUserGroup(new UserGroup("abuugg112201a","abuugg11220a",true),ctx);
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createPermission(new AccessPermission("READ","User"),null,ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createPermission(null,null,ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createPermission(null,ug,ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createPermission(null,null,null);
        });

    }


    @Order(87)
    @Test
    public void createPermissionTestWithInvalidInputForUserGroupWIthINvalidInputs()
    {
        UserGroup ug=userService.createUserGroup(new UserGroup("abcuugg112201a","abcuugg11220a",true),ctx);
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createPermission(new AccessPermission("READ","User"),null,ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createPermission(null,null,ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createPermission(null,ug,ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createPermission(null,null,null);
        });

    }


    @Order(88)
    @Test
    public void getPasswordEncodertest() {
            Assertions.assertNotNull(userService.getPasswordEncoder());
            Assertions.assertTrue(userService.getPasswordEncoder() instanceof PasswordEncoder);
    }

    @Order(89)
    @Test
    public void getPasswordEncodertest1() {

        Assertions.assertNotNull(userService.getPasswordEncoder().encode("Hello"));
        Assertions.assertTrue(userService.getPasswordEncoder().encode("Hello").endsWith("__IS_ENCRYPTED__"));
    }

    @Order(90)
    @Test
    public void getPasswordEncodertest2() {


        Assertions.assertTrue(userService.getPasswordEncoder().matches("Hello",userService.getPasswordEncoder().encode("Hello")));
    }


    @Order(91)
    @Test
    public void getUserDetailsServiceTest() {

        Assertions.assertNotNull(userService.getUserDetailsService());
        Assertions.assertTrue(userService.getUserDetailsService() instanceof UserDetailsService);
    }

    @Order(92)
    @Test
    public void getUserDetailsServiceTest1() {

        User user=userService.createUser(new User("user first","user last","userisunique","password",true),ctx);
        UserGroup ug=userService.createUserGroup(new UserGroup("groupforuserdetails","groupforuserdetails",true),ctx);
        userService.addUserToUserGroup(user.getUserId(),ug.getCode(),ctx);
        Assertions.assertNotNull(userService.getUserDetailsService().loadUserByUsername("userisunique"));
        Assertions.assertEquals(user.getUserId(),userService.getUserDetailsService().loadUserByUsername("userisunique").getUsername());
        Assertions.assertEquals(user.getPassword(),userService.getUserDetailsService().loadUserByUsername("userisunique").getPassword());
        Assertions.assertTrue(userService.getUserDetailsService().loadUserByUsername("userisunique").getAuthorities().stream().filter(auth->((GrantedAuthority) auth).getAuthority().contains(ug.getCode())).findAny().isPresent());
    }

    @Order(93)
    @Test
    public void enablePermissionTest()
    {
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission","groupforenablepermission",true),ctx);
        Assertions.assertNotNull(permission);
        Assertions.assertNotNull(userGroup);
        userService.enablePermission(permission,userGroup,ctx);
        Assertions.assertNotNull(userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1));
        Assertions.assertEquals(1,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());

    }

    @Order(94)
    @Test
    public void enablePermissionTestWithInvalidInputs()
    {
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"UserGroup"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission1","groupforenablepermission1",true),ctx);

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.enablePermission(permission,userGroup,null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.enablePermission(permission,null,null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.enablePermission(null,userGroup,null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.enablePermission(null,null,null);

        });

    }

    @Order(95)
    @Test
    public void enablePermissionTestScenario1()
    {
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        AccessPermission permission1=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"UserGroup"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission2","groupforenablepermission2",true),ctx);
        userService.enablePermission(permission,userGroup,ctx);

        Assertions.assertEquals(1,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
        userService.enablePermission(permission1,userGroup,ctx);
        Assertions.assertEquals(2,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
    }


    @Order(96)
    @Test
    public void disablePermissionTestScenario1()
    {
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        AccessPermission permission1=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"UserGroup"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission3","groupforenablepermission3",true),ctx);
        userService.enablePermission(permission,userGroup,ctx);
        userService.enablePermission(permission1,userGroup,ctx);
        Assertions.assertEquals(2,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
        userService.disablePermission(permission1,userGroup,ctx);
        Assertions.assertEquals(1,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
    }

    @Order(97)
    @Test
    public void disablePermissionTestScenario2()
    {
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        AccessPermission permission1=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"UserGroup"),ctx);
        AccessPermission permission2=userService.createPermission(new AccessPermission(AccessControlPermissions.WRITE,"Projects"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission4","groupforenablepermission4",true),ctx);
        userService.enablePermission(permission,userGroup,ctx);
        userService.enablePermission(permission1,userGroup,ctx);
        userService.enablePermission(permission2,userGroup,ctx);
        Assertions.assertEquals(3,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
        userService.disablePermission(permission1,userGroup,ctx);
        userService.disablePermission(permission2,userGroup,ctx);
        Assertions.assertEquals(1,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
    }

    @Order(98)
    @Test
    public void getPermissionsForUserGroupTest()
    {
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        AccessPermission permission1=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"UserGroup"),ctx);
        AccessPermission permission2=userService.createPermission(new AccessPermission(AccessControlPermissions.EXECUTE,"Projects"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission14","groupforenablepermission14",true),ctx);
        userService.enablePermission(permission,userGroup,ctx);
        userService.enablePermission(permission1,userGroup,ctx);
        userService.enablePermission(permission2,userGroup,ctx);
        Assertions.assertEquals(3,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
        userService.disablePermission(permission1,userGroup,ctx);
        userService.disablePermission(permission2,userGroup,ctx);
        Assertions.assertEquals(1,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
        Assertions.assertEquals(3,userService.getPermissionsForUserGroup(userGroup.getCode(),false,-1).getResults().size());
    }

    @Order(99)
    @Test
    public void getPermissionsByResourceAndUserGroup()
    {
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        AccessPermission permission1=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"UserGroup"),ctx);
        AccessPermission permission2=userService.createPermission(new AccessPermission(AccessControlPermissions.EXECUTE,"Projects"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission24","groupforenablepermission24",true),ctx);
        userService.enablePermission(permission,userGroup,ctx);
        userService.enablePermission(permission1,userGroup,ctx);
        userService.enablePermission(permission2,userGroup,ctx);
        Assertions.assertEquals(3,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
        userService.disablePermission(permission1,userGroup,ctx);
        userService.disablePermission(permission2,userGroup,ctx);
        Assertions.assertEquals(1,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
        Assertions.assertEquals(3,userService.getPermissionsForUserGroup(userGroup.getCode(),false,-1).getResults().size());
    }


    @Order(100)
    @Test
    public void getPermissionsByResourceAndUserGroupTest()
    {
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        AccessPermission permission1=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"UserGroup"),ctx);
        AccessPermission permission2=userService.createPermission(new AccessPermission(AccessControlPermissions.EXECUTE,"Projects"),ctx);
        AccessPermission permission3=userService.createPermission(new AccessPermission(AccessControlPermissions.WRITE,"Projects"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission6","groupforenablepermission6",true),ctx);
        userService.enablePermission(permission,userGroup,ctx);
        userService.enablePermission(permission1,userGroup,ctx);
        userService.enablePermission(permission2,userGroup,ctx);
        userService.enablePermission(permission3,userGroup,ctx);

        Assertions.assertEquals(2,userService.getPermissionsByResourceAndUserGroup("Projects","groupforenablepermission6",true,-1).getResults().size());

        Assertions.assertEquals(1,userService.getPermissionsByResourceAndUserGroup("UserGroup","groupforenablepermission6",true,-1).getResults().size());

        Assertions.assertEquals(1,userService.getPermissionsByResourceAndUserGroup("User","groupforenablepermission6",true,-1).getResults().size());

        userService.disablePermission(permission3,userGroup,ctx);
        Assertions.assertEquals(1,userService.getPermissionsByResourceAndUserGroup("Projects","groupforenablepermission6",true,-1).getResults().size());
        Assertions.assertEquals(2,userService.getPermissionsByResourceAndUserGroup("Projects","groupforenablepermission6",false,-1).getResults().size());

    }

    @Order(101)
    @Test
    public void disablePermissionTestWithInvalid()
    {
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        AccessPermission permission1=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"UserGroup"),ctx);
        AccessPermission permission2=userService.createPermission(new AccessPermission(AccessControlPermissions.WRITE,"Projects"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission5","groupforenablepermission5",true),ctx);
        userService.enablePermission(permission,userGroup,ctx);
        userService.enablePermission(permission1,userGroup,ctx);
        userService.enablePermission(permission2,userGroup,ctx);

        // userService.disablePermission(permission1,userGroup,ctx);

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.disablePermission(permission1,null,ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.disablePermission(null,userGroup,ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.disablePermission(permission,userGroup,null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.disablePermission(null,null,null);
        });


    }

    @Order(102)
    @Test
    public void importUserGroupRelationsTest()
    {
        InputStreamReader inputStreamReader1=new InputStreamReader(this.getClass().getResourceAsStream("/data/usergroup_11.csv"));
        PageResult<UserGroup> result=userService.importUserGroups(inputStreamReader1,ctx);
        result.getResults().stream().forEach(ug -> {
            log.info("user group id :"+ug.getId());
        });
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/usergroup2usergroup.csv"));
        PageResult<UserGroup2UserGroupRelation> result1=userService.importUserGroupRelations(inputStreamReader,ctx);
        result1.getResults().stream().forEach(rel -> {
            log.info("relation id :"+rel.getId());
        });
        Assertions.assertEquals(4,result1.getResults().size());
    }


    @Order(103)
    @Test
    public void importUser2UserGroupRelationstest()
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/users_12.csv"));
        PageResult<User> result=userService.importUsers(inputStreamReader,ctx);
        result.getResults().stream().forEach(u -> {
            log.info("user group id :"+u.getUserId());
        });
        Assertions.assertEquals(4,result.getResults().size());

        InputStreamReader inputStreamReader1=new InputStreamReader(this.getClass().getResourceAsStream("/data/usergroup_12.csv"));
        PageResult<UserGroup> result1=userService.importUserGroups(inputStreamReader1,ctx);
        result1.getResults().stream().forEach(ug -> {
            log.info("user group id :"+ug.getCode());
        });
        Assertions.assertEquals(5,result1.getResults().size());

        InputStreamReader inputStreamReader2=new InputStreamReader(this.getClass().getResourceAsStream("/data/user2usergroup.csv"));
        PageResult<User2UserGroupRelation> result2=userService.importUser2UserGroupRelations(inputStreamReader2,ctx);
        result2.getResults().stream().forEach(ug -> {
            log.info("user group id :"+ug.getUserGroupCode());
        });

        Assertions.assertEquals(4,result2.getResults().size());
    }



    @Order(104)
    @Test
    public void importUserGroupRelationsTestWithInvalidInput()
    {
        InputStreamReader inputStreamReader1=new InputStreamReader(this.getClass().getResourceAsStream("/data/usergroup_13.csv"));
        PageResult<UserGroup> result=userService.importUserGroups(inputStreamReader1,ctx);
        result.getResults().stream().forEach(ug -> {
            log.info("user group id :"+ug.getId());
        });

        Assertions.assertEquals(5,result.getResults().size());

        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/usergroup2usergroup_13.csv"));
//        PageResult<UserGroup2UserGroupRelation> result1=userService.importUserGroupRelations(inputStreamReader,ctx);
//        result1.getResults().stream().forEach(rel -> {
//            log.info("relation id :"+rel.getId());
//        });
//        Assertions.assertEquals(4,result.getResults().size());

//        Assertions.assertThrows(IllegalArgumentException.class,()->{
//            PageResult<UserGroup2UserGroupRelation> result1=userService.importUserGroupRelations(inputStreamReader,ctx);
//        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup2UserGroupRelation> result1=userService.importUserGroupRelations(inputStreamReader,null);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup2UserGroupRelation> result1=userService.importUserGroupRelations((Reader) null,ctx);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup2UserGroupRelation> result1=userService.importUserGroupRelations((Reader) null,null);
        });
    }


    @Order(105)
    @Test
    public void isUserAuthorizedForResourceAndPermissionTest()
    {
        User user=userService.createUser(new User("firstname","lastname","checkforauth@test.com","123456",true),ctx);
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        AccessPermission permission1=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"UserGroup"),ctx);
        AccessPermission permission2=userService.createPermission(new AccessPermission(AccessControlPermissions.EXECUTE,"Projects"),ctx);
        AccessPermission permission3=userService.createPermission(new AccessPermission(AccessControlPermissions.WRITE,"Projects"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission7","groupforenablepermission7",true),ctx);
        UserGroup userGroup1=userService.createUserGroup(new UserGroup("groupforenablepermission8","groupforenablepermission8",true),ctx);
        userService.addUserGroupToUserGroup(userGroup,userGroup1,ctx);
        userService.addUserToUserGroup(user,userGroup,ctx);
        userService.enablePermission(permission,userGroup,ctx);
        userService.enablePermission(permission1,userGroup,ctx);
        userService.enablePermission(permission2,userGroup1,ctx);
        userService.enablePermission(permission3,userGroup1,ctx);
        StopWatch stopWatch = new StopWatch();

        // Start the watch, do some task and stop the watch.
        stopWatch.start();
        userService.isUserAuthorizedForResourceAndPermission(user.getUserId(),"Projects",AccessControlPermissions.WRITE);
        stopWatch.stop();
        log.info("Time taken for the operation to execute :"+stopWatch.getTime(TimeUnit.MILLISECONDS)+"ms");
        Assertions.assertTrue(userService.isUserAuthorizedForResourceAndPermission(user.getUserId(),"Projects",AccessControlPermissions.WRITE));
    }

    @Order(106)
    @Test
    public void isUserGroupAuthorizedForResourceAndPermissionTest()
    {
        User user=userService.createUser(new User("firstname","lastname","checkforauth1@test.com","123456",true),ctx);
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        AccessPermission permission1=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"UserGroup"),ctx);
        AccessPermission permission2=userService.createPermission(new AccessPermission(AccessControlPermissions.EXECUTE,"Projects"),ctx);
        AccessPermission permission3=userService.createPermission(new AccessPermission(AccessControlPermissions.WRITE,"Projects"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission9","groupforenablepermission9",true),ctx);
        UserGroup userGroup1=userService.createUserGroup(new UserGroup("groupforenablepermission10","groupforenablepermission10",true),ctx);
        userService.addUserGroupToUserGroup(userGroup,userGroup1,ctx);
        userService.addUserToUserGroup(user,userGroup,ctx);
        userService.enablePermission(permission,userGroup1,ctx);
        userService.enablePermission(permission1,userGroup,ctx);
        userService.enablePermission(permission2,userGroup1,ctx);
        userService.enablePermission(permission3,userGroup1,ctx);
        StopWatch stopWatch = new StopWatch();

        // Start the watch, do some task and stop the watch.
        stopWatch.start();
        userService.isUserGroupAuthorizedForResourceAndPermission(userGroup.getCode(),"User",AccessControlPermissions.READ);
        stopWatch.stop();
        log.info("Time taken for the operation to execute :"+stopWatch.getTime(TimeUnit.MILLISECONDS)+"ms");
        Assertions.assertTrue(userService.isUserGroupAuthorizedForResourceAndPermission(userGroup.getCode(),"User",AccessControlPermissions.READ));
    }


    @Order(107)
    @Test
    public void exportUserDataTest()
    {

        Assertions.assertDoesNotThrow(()->{
            PageResult<User> result=userService.importUsers(new InputStreamReader(this.getClass().getResourceAsStream("/data/users_13.csv")),ctx);
            String filePath=new File(".").getCanonicalPath()+"/src/test/resources/export/user_data_export.csv";
            userService.exportData(new FileWriter(filePath),User.class,1,-1);
        });
    }


    @Order(108)
    @Test
    public void exportUserGroupDataTest()
    {
        Assertions.assertDoesNotThrow(()->{
            String filePath=new File(".").getCanonicalPath()+"/src/test/resources/export/usergroup_data_export.csv";
            userService.exportData(new FileWriter(filePath),UserGroup.class,1,-1);
        });
    }

    @Order(109)
    @Test
    public void exportUserGroupRelationDataTest()
    {
        Assertions.assertDoesNotThrow(()->{
            String filePath=new File(".").getCanonicalPath()+"/src/test/resources/export/usergrouprelation_data_export.csv";
            userService.exportData(new FileWriter(filePath),UserGroup2UserGroupRelation.class,1,-1);
        });
    }


    @Order(110)
    @Test
    public void exportAccessPermissionDataTest()
    {
        Assertions.assertDoesNotThrow(()->{
            String filePath=new File(".").getCanonicalPath()+"/src/test/resources/export/accessPermission_data_export.csv";
            userService.exportData(new FileWriter(filePath),AccessPermission.class,1,-1);
        });
    }

    @Order(111)
    @Test
    public void exportAccessPermission2userGroupRelationDataTest()
    {
        Assertions.assertDoesNotThrow(()->{
            String filePath=new File(".").getCanonicalPath()+"/src/test/resources/export/accessPermission2UserGroupRelation_data_export.csv";
            userService.exportData(new FileWriter(filePath),AccessPermission2UserGroupRelation.class,1,-1);
        });
    }


    @Order(112)
    @Test
    public void exportChangeLogDataTest()
    {
        Assertions.assertDoesNotThrow(()->{
            String filePath=new File(".").getCanonicalPath()+"/src/test/resources/export/changeLog_data_export.csv";
            userService.exportData(new FileWriter(filePath),ChangeLog.class,1,-1);
        });
    }


    @Order(113)
    @Test
    public void exportDataWithInvalidInput()
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.exportData(null,ChangeLog.class,1,-1);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.exportData(null,null,1,-1);
        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.exportData(null,null,null,-1);
        });


    }

    @Order(114)
    @Test
    public void importAccessPermissionDatatest()
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/access_permissions.csv"));
        PageResult<AccessPermission> result=userService.importAccessPermissions(inputStreamReader,ctx);
        result.getResults().stream().forEach(u -> {
            log.info("user group id :"+u.getResource());
        });
        Assertions.assertEquals(3,result.getResults().size());

    }

    @Order(115)
    @Test
    public void importAccessPermission2UserGroupRelationDatatest()
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/accesspermission2usergroup.csv"));
        PageResult<AccessPermission2UserGroupRelation> result=userService.importAccessPermissions2UserGroupRelations(inputStreamReader,ctx);
        result.getResults().stream().forEach(u -> {
            log.info("user group id :"+u.getUserGroupCode());
        });
        Assertions.assertEquals(5,result.getResults().size());

    }

    @Order(116)
    @Test
    public void getPermissionsByResourceAndUserGroupForGroupWIthNoPermissions()
    {
        UserGroup userGroup=userService.createUserGroup(new UserGroup("groupforenablepermission245","groupforenablepermission245",true),ctx);
        Assertions.assertEquals(0,userService.getPermissionsForUserGroup(userGroup.getCode(),true,-1).getResults().size());
    }


    @Order(117)
    @Test
    public void saveUserWithoutProperUser()throws AccessControlException{
        User user=userService.getUserById("testuser1@test.com");
        user.setPassword(null);
        user.setEnabled(true);
        user.setFirstName(null);
        user.setLastName("user");
        Assertions.assertThrows(AccessControlException.class,()->{
            User persistedUser=userService.saveUser(user,ctx);

        });

    }


    @Order(118)
    @Test
    public void findUsersWIthEmptyResults()throws AccessControlException{
        PageResult<User> users=userService.findUsers("cannotfind",1);

       Assertions.assertEquals(0,users.getResults().size());

    }


    @Order(119)
    @Test
    public void createUserGroupWithNullUserGroup()throws AccessControlException{

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createUserGroup(null,ctx);

        });

    }


    @Order(120)
    @Test
    public void findUserGroupsWIthEmptyResults()throws AccessControlException{
        PageResult<UserGroup> userGroups=userService.findUserGroups("cannotfind",1);

        Assertions.assertEquals(0,userGroups.getResults().size());

    }

    @Order(121)
    @Test
    public void createPermissionWithoutExistingPermissionForUserGroup()throws AccessControlException{
        UserGroup userGroup=userService.createUserGroup(new UserGroup("createPermissionWithoutExistingPermissionForUserGroup","createPermissionWithoutExistingPermissionForUserGroup",true),ctx);
        Assertions.assertNotNull(userService.createPermission(new AccessPermission(AccessControlPermissions.EXECUTE,"Projects"),userGroup,ctx));

    }

    @Order(122)
    @Test
    public void createPermissionWithDataErrors()throws AccessControlException{
        UserGroup userGroup=userService.createUserGroup(new UserGroup("createPermissionWithDataErrors","createPermissionWithDataErrors",true),ctx);
        Assertions.assertThrows(AccessControlException.class,()->{
            userService.createPermission(new AccessPermission(AccessControlPermissions.EXECUTE,null),userGroup,ctx);
        });

    }

    @Order(123)
    @Test
    public void enablePermissionWithErrors()throws AccessControlException{
        UserGroup userGroup=userService.createUserGroup(new UserGroup("enablePermissionWithErrors","enablePermissionWithErrors",true),ctx);
        Assertions.assertThrows(AccessControlException.class,()->{
            userService.enablePermission(new AccessPermission(AccessControlPermissions.EXECUTE,null),userGroup,ctx);
        });

    }

    @Order(124)
    @Test
    public void enablePermissionWithErrors1()throws AccessControlException{
        AccessPermission permission=userService.createPermission(new AccessPermission(AccessControlPermissions.READ,"User"),ctx);
        UserGroup userGroup=userService.createUserGroup(new UserGroup("enablePermissionWithErrors","enablePermissionWithErrors",true),ctx);
        userService.createPermission(permission,userGroup,ctx);
        Assertions.assertDoesNotThrow(()->{
            userService.enablePermission(permission,userGroup,ctx);
        });

    }

    @Order(125)
    @Test
    public void exportUser2UserGroupDataTest()
    {
        Assertions.assertDoesNotThrow(()->{
            String filePath=new File(".").getCanonicalPath()+"/src/test/resources/export/user2usergroup_data_export.csv";
            userService.exportData(new FileWriter(filePath),User2UserGroupRelation.class,1,-1);
        });
    }

}
