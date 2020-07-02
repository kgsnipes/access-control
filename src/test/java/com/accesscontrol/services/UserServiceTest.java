package com.accesscontrol.services;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.exception.UserGroupNotFoundException;
import com.accesscontrol.exception.UserNotFoundException;
import com.accesscontrol.models.User;
import com.accesscontrol.models.UserGroup;
import com.accesscontrol.services.impl.DefaultAccessControlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    static Logger log= LogManager.getLogger(UserServiceTest.class);

    static AccessControlService accessControlService;

    static UserService userService;

    AccessControlContext ctx=new AccessControlContext("system-user",null);

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
        userService.disableUser(userId,null);
        Assertions.assertEquals(false,userService.getUserById(userId).getEnabled());

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
    public void findUsersWithZeroPageNumber()
    {
        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.findUsers("hello",0);
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
        Assertions.assertThrows(UserGroupNotFoundException.class,()->{
            UserGroup savedUserGroup=userService.saveUserGroup(userGroup,ctx);
        });
    }

}
