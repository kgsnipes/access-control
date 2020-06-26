package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.repository.ChangeLogRepository;
import com.accesscontrol.services.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

public class DefaultChangeLogService implements ChangeLogService {

    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;

    @Autowired
    private ChangeLogRepository changeLogRepository;


    @Override
    public void logChange(Long pk, String type, String action, Object previousState, Object newState, AccessControlContext context) {
        if(isChangeLogEnabled())
        {

        }
    }


    private boolean isChangeLogEnabled()
    {
        return accessControlConfigProperties.getProperty(AccessControlConfigConstants.CHANGELOG_ENABLED).equals("true");
    }
}
