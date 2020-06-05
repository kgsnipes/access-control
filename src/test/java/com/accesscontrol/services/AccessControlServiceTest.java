package com.accesscontrol.services;

import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.services.impl.DefaultAccessControlService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    public void initTest() throws AccessControlException {
        Assertions.assertEquals(true, Objects.nonNull(accessControlService.getApplicationContext()),"Application context is not null");
    }

    @Test
    public void configPropertiesTest() throws AccessControlException {
        Assertions.assertEquals(true,accessControlService.getApplicationContext().getBean(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG) instanceof Properties);
    }

    @Test
    public void mandatoryConfigPropertiesTest() throws AccessControlException {
        Properties configProperties= (Properties) accessControlService.getApplicationContext().getBean(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG);
        log.info("Fetching a mandatory config "+configProperties.getProperty(AccessControlConfigConstants.MANDATORY_CONFIG[0]));
        Assertions.assertEquals(true, StringUtils.isNotEmpty(configProperties.getProperty(AccessControlConfigConstants.MANDATORY_CONFIG[0])));
    }
}
