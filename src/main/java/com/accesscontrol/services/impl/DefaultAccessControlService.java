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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Objects;
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
                log.info("Loading the configuration from the properties file from class path");
                Configuration configuration=getConfigurationFromClassPath();
                this.properties.put(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_URL,configuration.getString(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_URL));
                this.properties.put(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_USERNAME,configuration.getString(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_USERNAME));
                this.properties.put(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_PASSWORD,configuration.getString(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_PASSWORD));
                this.properties.put(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_DRIVER_CLASS,configuration.getString(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_DRIVER_CLASS));

                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_DATABASE_DIALECT,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_DATABASE_DIALECT));
                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_SHOW_SQL,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_SHOW_SQL));
                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_DDL_AUTO,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_DDL_AUTO));


                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_AUTO_COMMIT,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_AUTO_COMMIT,"true"));
                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_FORMAT_SQL,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_FORMAT_SQL,"true"));
                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_MAX_STATEMENTS,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_MAX_STATEMENTS,"50"));
                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_POOL_SIZE_MIN,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_POOL_SIZE_MIN,"2"));
                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_POOL_SIZE_MAX,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_POOL_SIZE_MAX,"10"));
                this.properties.put(AccessControlConfigConstants.JPAConfigConstants.JPA_TIMEOUT,configuration.getString(AccessControlConfigConstants.JPAConfigConstants.JPA_TIMEOUT,"1800"));

                this.properties.put(AccessControlConfigConstants.PasswordEncryption.PASSWORD_ENCRYTPION_FLAG,configuration.getString(AccessControlConfigConstants.PasswordEncryption.PASSWORD_ENCRYTPION_FLAG,"__IS_ENCRYPTED__"));
                this.properties.put(AccessControlConfigConstants.PasswordEncryption.PASSWORD_PEPPER,configuration.getString(AccessControlConfigConstants.PasswordEncryption.PASSWORD_PEPPER,""));
                this.properties.put(AccessControlConfigConstants.PasswordEncryption.PASSWORD_SALT,configuration.getString(AccessControlConfigConstants.PasswordEncryption.PASSWORD_SALT,""));
                this.properties.put(AccessControlConfigConstants.PasswordEncryption.PASSWORD_DIGEST,configuration.getString(AccessControlConfigConstants.PasswordEncryption.PASSWORD_DIGEST,"MD5"));

                log.info("Done! Loading the configuration from the properties file from class path");


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

    private void validateMandatoryConfiguration(Properties properties) throws AccessControlException
    {
        log.info("validating the mandatory configuration");
        for(String key:Arrays.asList(AccessControlConfigConstants.MANDATORY_CONFIG))
        {
            if(Objects.isNull(properties.getProperty(key)))
            {
                throw new AccessControlException("Configuration not found for key "+ key);
            }
        }
        log.info("Done! validating the mandatory configuration");
    }

    @Override
    public ApplicationContext getApplicationContext(Properties properties)throws AccessControlException {

        try
        {
            log.info("Starting to load access control application context");
            if(!loaded)
            {
                log.info("validating the mandatory configuration");
                validateMandatoryConfiguration(properties);
                this.applicationContext=new AnnotationConfigApplicationContext();


                log.info("injecting the configuration properties");
                this.applicationContext.registerBean(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG,Properties.class,()->this.getProperties(),bd->bd.setAutowireCandidate(true));

                this.applicationContext.scan(AccessControlConfigConstants.BASE_PACKAGE_FOR_SCAN);

                this.applicationContext.refresh();

                this.applicationContext.start();

                this.applicationContext.registerShutdownHook();

                loaded=true;
                log.info("Loaded access control application context !!");
            }

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
                        .configure(params.properties().setFile(ResourceUtils.getFile(AccessControlConfigConstants.CLASSPATH_CONFIG_FILE)));
        return builder.getConfiguration();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }



}
