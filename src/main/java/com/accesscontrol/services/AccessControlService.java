package com.accesscontrol.services;

import com.accesscontrol.exception.AccessControlException;

import java.util.Properties;

public interface AccessControlService {

    void init();

    void init(Properties properties);

    UserService getUserService();


}
