package com.accesscontrol.services;

import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.models.User;
import com.accesscontrol.services.impl.DefaultAccessControlService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class UserServiceTest {

    static Logger log= LogManager.getLogger(UserServiceTest.class);

    static AccessControlService accessControlService;

    static UserService userService;

    @BeforeAll
    public static void setup() throws AccessControlException {
        log.info("Setting up accessControlService");
        accessControlService=new DefaultAccessControlService();
        userService= (UserService) accessControlService.getApplicationContext().getBean("userService");
    }

    @Test
    public void userServiceTest() throws AccessControlException {
        Assertions.assertEquals(true,accessControlService.getApplicationContext().getBean("userService") instanceof UserService);
    }

    @Test
    public void createUserTestWithException() throws AccessControlException {

        User user=new User();
        Assertions.assertThrows(AccessControlException.class,()->{
            userService.createUser(user);
        });
    }

    @Test
    public void createUserTestWithIllegalArgumentException() throws AccessControlException {

        Assertions.assertThrows(IllegalArgumentException.class,()->{
            userService.createUser(null);
        });
    }

    @Test
    public void createUserTestWithoutException() throws AccessControlException {

        User user=new User();
        user.setPassword("123456");
        user.setEnabled(true);
        user.setFirstName("test");
        user.setLastName("user");
        user.setUserId("testuser1@test.com");
        User persistedUser=userService.createUser(user);
        Assertions.assertEquals(user.getUserId(),persistedUser.getUserId());
        Assertions.assertEquals(true, Objects.nonNull(persistedUser.getId()));
        log.info("ID for the persisted user is "+persistedUser.getId());
    }

    @Test
    public void createUserTestWithExceptionForDuplicateUser() throws AccessControlException {
        User user=new User();
        user.setPassword("123456");
        user.setEnabled(true);
        user.setFirstName("test");
        user.setLastName("user");
        user.setUserId("testuser2@test.com");
        User persistedUser=userService.createUser(user);
        User user2=new User();
        user2.setPassword("123456");
        user2.setEnabled(true);
        user2.setFirstName("test");
        user2.setLastName("user");
        user2.setUserId("testuser2@test.com");
        Assertions.assertThrows(AccessControlException.class,()->{
            userService.createUser(user2);
        });
    }

}
