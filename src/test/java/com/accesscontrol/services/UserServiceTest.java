package com.accesscontrol.services;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.beans.AccessControlPermissions;
import com.accesscontrol.beans.PageResult;
import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.exception.UserGroupNotFoundException;
import com.accesscontrol.exception.UserNotFoundException;
import com.accesscontrol.models.AccessPermission;
import com.accesscontrol.models.User;
import com.accesscontrol.models.UserGroup;
import com.accesscontrol.services.impl.DefaultAccessControlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.PageRequest;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.*;

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

        CSVReader csvReader=new CSVReaderBuilder(inputStreamReader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(',').build()).withSkipLines(1).build();
        List<User> userList=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String arr[]=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                User user=new User();
                user.setUserId(arr[0]);
                user.setPassword(arr[1]);
                user.setFirstName(arr[2]);
                user.setLastName(arr[3]);
                user.setEnabled(Boolean.getBoolean(arr[4]));
                userList.add(user);
            }
        }
        PageResult<User> result=userService.importUsers(userList,false,ctx);
        result.getResults().stream().forEach(user -> {
            log.info("user id :"+user.getId());
        });

        Assertions.assertEquals(userList.size(),result.getResults().size());

    }

    @Order(51)
    @Test
    public void importUserGroupsTest()throws Exception
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/usergroup.csv"));

        CSVReader csvReader=new CSVReaderBuilder(inputStreamReader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(',').build()).withSkipLines(1).build();
        List<UserGroup> userGroupList=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String arr[]=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                UserGroup group=new UserGroup();
                group.setCode(arr[0]);
                group.setName(arr[1]);
                group.setEnabled(Boolean.getBoolean(arr[2]));
                userGroupList.add(group);
            }
        }
        PageResult<UserGroup> result=userService.importUserGroups(userGroupList,false,ctx);
        result.getResults().stream().forEach(ug -> {
            log.info("user group id :"+ug.getId());
        });

        Assertions.assertEquals(userGroupList.size(),result.getResults().size());

    }


    @Order(52)
    @Test
    public void importUserGroupsTestWithException()throws Exception
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/usergroup.csv"));

        CSVReader csvReader=new CSVReaderBuilder(inputStreamReader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(',').build()).withSkipLines(1).build();
        List<UserGroup> userGroupList=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String arr[]=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                UserGroup group=new UserGroup();
                group.setCode(arr[0]);
                group.setName(arr[1]);
                group.setEnabled(Boolean.getBoolean(arr[2]));
                userGroupList.add(group);
            }
        }


        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<UserGroup> result=userService.importUserGroups(userGroupList,false,null);
        });

    }

    @Order(53)
    @Test
    public void importUsersTestWithException()throws Exception
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/users.csv"));

        CSVReader csvReader=new CSVReaderBuilder(inputStreamReader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(',').build()).withSkipLines(1).build();
        List<User> userList=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String arr[]=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                User user=new User();
                user.setUserId(arr[0]);
                user.setPassword(arr[1]);
                user.setFirstName(arr[2]);
                user.setLastName(arr[3]);
                user.setEnabled(Boolean.getBoolean(arr[4]));
                userList.add(user);
            }
        }

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<User> result=userService.importUsers(userList,false,null);

        });

    }



    @Order(54)
    @Test
    public void importUsersTestWithNullListAndContext()throws Exception
    {

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<User> result=userService.importUsers(null,false,null);

        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            PageResult<User> result=userService.importUsers(null,false,ctx);

        });

    }

    @Order(55)
    @Test
    public void importUserGroupsTestWithNullListAndContext()throws Exception
    {

        Assertions.assertThrows(IllegalArgumentException.class,()->{
           userService.importUserGroups(Collections.emptyList(),false,null);

        });

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.importUsers(null,false,ctx);

        });

    }



    @Order(56)
    @Test
    public void importUsersTestWithMissingUserId()throws Exception
    {
        InputStreamReader inputStreamReader=new InputStreamReader(this.getClass().getResourceAsStream("/data/users_without_userId.csv"));

        CSVReader csvReader=new CSVReaderBuilder(inputStreamReader).withVerifyReader(true).withCSVParser(new CSVParserBuilder().withSeparator(',').build()).withSkipLines(1).build();
        List<User> userList=new ArrayList<>();


        Iterator<String[]> itr=csvReader.iterator();
        while (itr.hasNext())
        {
            String arr[]=itr.next();
            if(StringUtils.isNotEmpty(StringUtils.join(arr)))
            {
                User user=new User();
                user.setUserId(arr[0]);
                user.setPassword(arr[1]);
                user.setFirstName(arr[2]);
                user.setLastName(arr[3]);
                user.setEnabled(Boolean.getBoolean(arr[4]));
                userList.add(user);
            }
        }
        PageResult<User> result=userService.importUsers(userList,false,ctx);

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
        userService.createUserGroup(new UserGroup("uugg112201a","uugg11220a",true),ctx);
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
            PageResult<UserGroup> groups=userService.getChildUserGroupsForUserGroup("uugg112201a",-2);
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



}
