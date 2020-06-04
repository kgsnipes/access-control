package com.accesscontrol.services;

import com.accesscontrol.exception.AccessControlException;
import org.springframework.context.ApplicationContext;

import java.util.Properties;

public interface AccessControlService {

    ApplicationContext getApplicationContext() throws AccessControlException;

    ApplicationContext getApplicationContext(Properties properties) throws AccessControlException;


}
