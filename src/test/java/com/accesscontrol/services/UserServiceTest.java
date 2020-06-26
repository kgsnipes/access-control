package com.accesscontrol.services;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.exception.UserNotFoundException;
import com.accesscontrol.models.User;
import com.accesscontrol.services.impl.DefaultAccessControlService;
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

}
