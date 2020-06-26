package com.accesscontrol.constants;



public interface AccessControlConfigConstants {

    interface DataSourceConfigConstants
    {
        String DATASOURCE_URL="accesscontrol.datasource.url";
        String DATASOURCE_USERNAME="accesscontrol.datasource.username";
        String DATASOURCE_PASSWORD="accesscontrol.datasource.password";
        String DATASOURCE_DRIVER_CLASS="accesscontrol.datasource.driver-class-name";
    }

    interface JPAConfigConstants
    {
        String JPA_DATABASE_DIALECT="accesscontrol.jpa.database-platform";
        String JPA_SHOW_SQL="accesscontrol.jpa.show-sql";
        String JPA_DDL_AUTO="accesscontrol.jpa.hibernate.ddl-auto";
        String JPA_AUTO_COMMIT="accesscontrol.hibernate.connection.autocommit";
        String JPA_FORMAT_SQL=" accesscontrol.hibernate.format_sql";
        String JPA_MAX_STATEMENTS="accesscontrol.hibernate.c3p0.max_statements";
        String JPA_TIMEOUT="accesscontrol.hibernate.c3p0.timeout";
        String JPA_POOL_SIZE_MIN="accesscontrol.hibernate.c3p0.min_size";
        String JPA_POOL_SIZE_MAX="accesscontrol.hibernate.c3p0.max_size";

    }



    String BASE_PACKAGE_FOR_SCAN="com.accesscontrol.*";
    String ACCESS_CONTROL_CONFIG="accessControlConfigProperties";
    String CLASSPATH_CONFIG_FILE="classpath:accesscontrol.properties";
    String CHANGELOG_ENABLED="accesscontrol.changelog.enabled";

    interface HibernateConfiguration
    {
        String DDL_AUTO="hibernate.hbm2ddl.auto";
        String DIALECT="hibernate.dialect";
        String AUTO_COMMIT="hibernate.connection.autocommit";

        String MIN_POOL_SIZE="hibernate.c3p0.min_size";
        String MAX_POOL_SIZE="hibernate.c3p0.max_size";
        String TIMEOUT="hibernate.c3p0.timeout";
        String MAX_STATEMENTS="hibernate.c3p0.max_statements";

        String FORMAT_SQL="hibernate.format_sql";
    }

    interface PasswordEncryption
    {
        String PASSWORD_SALT="accesscontrol.password.salt";
        String PASSWORD_PEPPER="accesscontrol.password.pepper";
        String PASSWORD_DIGEST="accesscontrol.password.digest";
        String PASSWORD_ENCRYTPION_FLAG="accesscontrol.password.encryptionflag";
    }

    interface CRUD
    {
        String CREATE="CREATE";
        String READ="READ";
        String UPDATE="UPDATE";
        String DELETE="DELETE";
    }


    String MANDATORY_CONFIG[]=new String[]{
            DataSourceConfigConstants.DATASOURCE_URL,
            DataSourceConfigConstants.DATASOURCE_USERNAME,
            DataSourceConfigConstants.DATASOURCE_PASSWORD,
            DataSourceConfigConstants.DATASOURCE_DRIVER_CLASS,
            JPAConfigConstants.JPA_DATABASE_DIALECT,
            JPAConfigConstants.JPA_DDL_AUTO,
            PasswordEncryption.PASSWORD_DIGEST
        };

}
