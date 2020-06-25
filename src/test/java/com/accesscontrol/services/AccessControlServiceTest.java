package com.accesscontrol.services;

import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.services.impl.DefaultAccessControlService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.util.Objects;
import java.util.Properties;


public class AccessControlServiceTest {

    static Logger log= LogManager.getLogger(AccessControlServiceTest.class);

    static AccessControlService accessControlService;

    @BeforeAll
    public static void setup() throws AccessControlException {
        log.info("Setting up accessControlService");
        accessControlService=new DefaultAccessControlService();
    }

    @Test
    public void userServicePassWithInit() throws AccessControlException {
        Assertions.assertEquals(true, Objects.nonNull(accessControlService.getUserService()),"Application context is not null");
    }

}
