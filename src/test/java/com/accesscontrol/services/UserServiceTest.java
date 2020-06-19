package com.accesscontrol.services;

import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.models.User;
import com.accesscontrol.services.impl.DefaultAccessControlService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

    static Logger log= LogManager.getLogger(UserServiceTest.class);

    static AccessControlService accessControlService;

    @BeforeAll
    public static void setup() throws AccessControlException {
        log.info("Setting up accessControlService");
        accessControlService=new DefaultAccessControlService();
    }

    @Test
    public void userServiceTest() throws AccessControlException {
        Assertions.assertEquals(true,accessControlService.getApplicationContext().getBean("userService") instanceof UserService);
    }


}
