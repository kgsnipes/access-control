package com.accesscontrol.config;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class AccessControlStartupListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log= LogManager.getLogger(AccessControlStartupListener.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        log.info("********** Application Context Started ***********************");

    }



}
