package com.accesscontrol.config;

import com.accesscontrol.constants.AccessControlConfigConstants;
import com.accesscontrol.services.ChangeLogService;
import com.accesscontrol.services.PasswordEncryptionService;
import com.accesscontrol.services.UserService;
import com.accesscontrol.services.impl.DefaultChangeLogService;
import com.accesscontrol.services.impl.DefaultPasswordEncryptionService;
import com.accesscontrol.services.impl.DefaultUserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Properties;


@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableJpaRepositories(AccessControlConfigConstants.BASE_PACKAGE_FOR_SCAN)
@ComponentScan(basePackages = {AccessControlConfigConstants.BASE_PACKAGE_FOR_SCAN})
public class AccessControlConfig {

    private static final Logger log = LogManager.getLogger(AccessControlConfig.class);

    @Autowired
    @Qualifier(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG)
    private Properties accessControlConfigProperties;

    @Bean
    public DataSource dataSource() {

        // Properties accessControlConfigProperties= (Properties) applicationContext.getBean(AccessControlConfigConstants.ACCESS_CONTROL_CONFIG);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        log.info("fetching driver class name for data source " + accessControlConfigProperties.getProperty(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_DRIVER_CLASS));
        dataSource.setDriverClassName(accessControlConfigProperties.getProperty(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_DRIVER_CLASS));
        dataSource.setUsername(accessControlConfigProperties.getProperty(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_USERNAME));
        dataSource.setPassword(accessControlConfigProperties.getProperty(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_PASSWORD));
        dataSource.setUrl(accessControlConfigProperties.getProperty(AccessControlConfigConstants.DataSourceConfigConstants.DATASOURCE_URL));
        return dataSource;
    }


    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(new String[]{AccessControlConfigConstants.BASE_PACKAGE_FOR_SCAN});

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties additionalProperties = new Properties();
        additionalProperties.setProperty(AccessControlConfigConstants.HibernateConfiguration.DDL_AUTO, accessControlConfigProperties.getProperty(AccessControlConfigConstants.JPAConfigConstants.JPA_DDL_AUTO));
        additionalProperties.setProperty(AccessControlConfigConstants.HibernateConfiguration.DIALECT, accessControlConfigProperties.getProperty(AccessControlConfigConstants.JPAConfigConstants.JPA_DATABASE_DIALECT));
        // additionalProperties.setProperty("hibernate.connection.pool_size","50");
        additionalProperties.setProperty(AccessControlConfigConstants.HibernateConfiguration.AUTO_COMMIT, accessControlConfigProperties.getProperty(AccessControlConfigConstants.JPAConfigConstants.JPA_AUTO_COMMIT));

        additionalProperties.setProperty(AccessControlConfigConstants.HibernateConfiguration.MIN_POOL_SIZE, accessControlConfigProperties.getProperty(AccessControlConfigConstants.JPAConfigConstants.JPA_POOL_SIZE_MIN));
        additionalProperties.setProperty(AccessControlConfigConstants.HibernateConfiguration.MAX_POOL_SIZE, accessControlConfigProperties.getProperty(AccessControlConfigConstants.JPAConfigConstants.JPA_POOL_SIZE_MAX));
        additionalProperties.setProperty(AccessControlConfigConstants.HibernateConfiguration.TIMEOUT, accessControlConfigProperties.getProperty(AccessControlConfigConstants.JPAConfigConstants.JPA_TIMEOUT));
        additionalProperties.setProperty(AccessControlConfigConstants.HibernateConfiguration.MAX_STATEMENTS, accessControlConfigProperties.getProperty(AccessControlConfigConstants.JPAConfigConstants.JPA_MAX_STATEMENTS));

        additionalProperties.setProperty(AccessControlConfigConstants.HibernateConfiguration.FORMAT_SQL, accessControlConfigProperties.getProperty(AccessControlConfigConstants.JPAConfigConstants.JPA_FORMAT_SQL));

        em.setJpaProperties(additionalProperties);

        return em;
    }


    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }


    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager
                = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
                entityManagerFactory().getObject());
        return transactionManager;
    }

    @Bean
    public UserService userService() {
        return new DefaultUserService();
    }

    @Bean
    ValidatorFactory validatorFactory() {
        return Validation.buildDefaultValidatorFactory();
    }

    @Bean
    PasswordEncryptionService passwordEncryptionService() {
        return new DefaultPasswordEncryptionService();
    }

    @Bean
    ChangeLogService changeLogService() {
        return new DefaultChangeLogService();
    }




}
