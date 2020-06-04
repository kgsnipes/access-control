package com.accesscontrol.services.impl;

import com.accesscontrol.constants.AccessControlConfigConstants;
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
                this.properties.put(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_URL,configuration.getString(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_URL));
                this.properties.put(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_USERNAME,configuration.getString(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_USERNAME));
                this.properties.put(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_PASSWORD,configuration.getString(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_PASSWORD));
                this.properties.put(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_DRIVER_CLASS,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_DATABASE_DIALECT));
                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_DATABASE_DIALECT,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_DATABASE_DIALECT));
                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_SHOW_SQL,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_SHOW_SQL));
                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_DDL_AUTO,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_DDL_AUTO));

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

    private void validateProperties()
    {

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
        return this.applicationContext;
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
