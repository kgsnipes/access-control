package com.accesscontrol.services.impl;

import com.accesscontrol.exception.AccessControlException;
import com.accesscontrol.services.AccessControlService;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.util.Properties;

public class DefaultAccessControlService implements AccessControlService {

    private static final Logger log= LogManager.getLogger(DefaultAccessControlService.class);

    private AnnotationConfigApplicationContext applicationContext;

    private Properties properties=new Properties();

    private Boolean loaded=false;

    public DefaultAccessControlService() throws AccessControlException {
        getApplicationContext();
    }

    public DefaultAccessControlService(Properties properties) throws AccessControlException {
        getApplicationContext(properties);
    }

    @Override
    public ApplicationContext getApplicationContext() throws AccessControlException {

        if(!loaded)
        {

            try {
                Configuration configuration=getConfigurationFromClassPath();
                this.properties.put("accesscontrol.datasource.url",configuration.getString("accesscontrol.datasource.url"));
                this.properties.put("accesscontrol.datasource.username",configuration.getString("accesscontrol.datasource.username"));
                this.properties.put("accesscontrol.datasource.password",configuration.getString("accesscontrol.datasource.password"));
                this.properties.put("accesscontrol.datasource.driver-class-name",configuration.getString("accesscontrol.datasource.driver-class-name"));
                this.properties.put("accesscontrol.jpa.database-platform",configuration.getString("accesscontrol.jpa.database-platform"));
                this.properties.put("accesscontrol.jpa.show-sql",configuration.getString("accesscontrol.jpa.show-sql"));
                this.properties.put("accesscontrol.jpa.hibernate.ddl-auto",configuration.getString("accesscontrol.jpa.hibernate.ddl-auto"));

            } catch (ConfigurationException|FileNotFoundException e) {
                log.error("Exception occurred while loading access control application context",e);
                throw new AccessControlException("Exception occurred while loading access control application context",e);
            }
            return getApplicationContext(properties);
        }
        else
        {
            return this.applicationContext;
        }


    }

    @Override
    public ApplicationContext getApplicationContext(Properties properties)throws AccessControlException {

        try
        {
            log.info("Starting to load access control application context");
            if(!loaded)
            {
                this.applicationContext=new AnnotationConfigApplicationContext("com.accesscontrol.*");
                this.applicationContext.start();
                this.applicationContext.registerShutdownHook();
            }
            loaded=true;
            log.info("Loaded access control application context !!");
        }
        catch (Exception ex)
        {
            loaded=false;
            applicationContext=null;
            log.error("Exception occurred while loading Access Control Application context",ex);
            throw new AccessControlException("Exception occurred while loading access control application context",ex);
        }
        return applicationContext;
    }

    private Configuration getConfigurationFromClassPath() throws ConfigurationException, FileNotFoundException {
        final Parameters params = new Parameters();
        final FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties().setFile(ResourceUtils.getFile("classpath:accesscontrol.properties")));
        return builder.getConfiguration();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }



}
