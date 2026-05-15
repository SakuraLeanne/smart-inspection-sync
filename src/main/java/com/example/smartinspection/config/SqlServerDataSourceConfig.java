package com.example.smartinspection.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class SqlServerDataSourceConfig {

    @Bean(name = "sqlServerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.sqlserver")
    public DataSource sqlServerDataSource() {
        return new DriverManagerDataSource();
    }

    @Bean(name = "sqlServerJdbcTemplate")
    public JdbcTemplate sqlServerJdbcTemplate(DataSource sqlServerDataSource) {
        return new JdbcTemplate(sqlServerDataSource);
    }
}
