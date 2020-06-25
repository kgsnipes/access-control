package com.accesscontrol.services;

import com.accesscontrol.exception.AccessControlException;

import java.util.Properties;

public interface AccessControlService {

    void init() throws AccessControlException;

    void init(Properties properties) throws AccessControlException;

    UserService getUserService();


}
