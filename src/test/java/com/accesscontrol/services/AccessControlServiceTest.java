package com.accesscontrol.services;

import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.services.impl.DefaultAccessControlService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class AccessControlServiceTest {

    static Logger log= LogManager.getLogger(AccessControlServiceTest.class);

    static AccessControlService accessControlService;

    @BeforeAll
    public static void setup() throws AccessControlException {
        log.info("Setting up accessControlService");
        accessControlService=new DefaultAccessControlService();
    }

    @Test
    public void initTest() throws AccessControlException {
        Assertions.assertEquals(true, Objects.nonNull(accessControlService.getApplicationContext()),"Application context is not null");
    }
}
