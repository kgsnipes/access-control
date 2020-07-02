package com.accesscontrol.services.impl;

import com.accesscontrol.beans.AccessControlContext;
import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.models.ChangeLog;
import com.accesscontrol.repository.ChangeLogRepository;
import com.accesscontrol.services.ChangeLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Objects;
import java.util.Properties;


public class DefaultChangeLogService implements ChangeLogService {

    private static final Logger LOG= LogManager.getLogger(DefaultChangeLogService.class);

    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;

    @Autowired
    private ChangeLogRepository changeLogRepository;

    private ObjectMapper mapper=new ObjectMapper();


    @Override
    public void logChange(Long pk, String type, String action, Object previousState, Object newState, AccessControlContext context) {
        if(isChangeLogEnabled())
        {
            ChangeLog log=new ChangeLog();
            if(Objects.nonNull(pk))
            {
                log.setPk(pk);
            }
            if(Objects.nonNull(StringUtils.trimToNull(type)))
            {
                log.setType(type);
            }
            if(Objects.nonNull(StringUtils.trimToNull(action)))
            {
                log.setAction(action);
            }
            if(Objects.nonNull(previousState))
            {
                try {
                    log.setPreviousState(mapper.writeValueAsString(previousState));
                } catch (JsonProcessingException e) {
                    LOG.error("Exception in parsing object",e);
                }
            }

            if(Objects.nonNull(newState))
            {
                try {
                    log.setNewState(mapper.writeValueAsString(newState));
                } catch (JsonProcessingException e) {
                    LOG.error("Exception in parsing object",e);
                }
            }

            if(Objects.nonNull(context))
            {
                if(Objects.nonNull(StringUtils.trimToNull(context.getUserId())))
                {
                    log.setUserId(context.getUserId());
                }
                if(Objects.nonNull(StringUtils.trimToNull(context.getMessage())))
                {
                    log.setMessage(context.getMessage());
                }

                try {
                    log.setContextObject(mapper.writeValueAsString(context));
                } catch (JsonProcessingException e) {
                    LOG.error("Exception in parsing object",e);
                }
            }

            if(StringUtils.isNotEmpty(log.getAction())){
                changeLogRepository.save(log);
            }

        }
    }


    private boolean isChangeLogEnabled()
    {
        return accessControlConfigProperties.getProperty(AccessControlConfigConstants.CHANGELOG_ENABLED).equals("true");
    }
}