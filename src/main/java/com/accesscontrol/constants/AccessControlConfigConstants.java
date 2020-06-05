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

    String MANDATORY_CONFIG[]=new String[]{DataSourceConfigConstants.DATASOURCE_URL,
            DataSourceConfigConstants.DATASOURCE_USERNAME,
            DataSourceConfigConstants.DATASOURCE_PASSWORD,
            DataSourceConfigConstants.DATASOURCE_DRIVER_CLASS,
            JPAConfigConstants.JPA_DATABASE_DIALECT,
            JPAConfigConstants.JPA_DDL_AUTO
    };

    String BASE_PACKAGE_FOR_SCAN="com.accesscontrol.*";
    String ACCESS_CONTROL_CONFIG="accessControlConfigProperties";


}
