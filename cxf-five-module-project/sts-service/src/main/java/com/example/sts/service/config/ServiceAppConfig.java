package com.example.sts.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.example.sts.service")
@PropertySource("classpath:application.properties")
public class ServiceAppConfig {

    @Value("${db.driver-class-name}")
    private String dbDriverClassName;

    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.username:}")
    private String dbUsername;

    @Value("${db.password:}")
    private String dbPassword;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public DataSource serviceDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dbDriverClassName);
        dataSource.setUrl(dbUrl);
        if (dbUsername != null && !dbUsername.isEmpty()) {
            dataSource.setUsername(dbUsername);
        }
        if (dbPassword != null && !dbPassword.isEmpty()) {
            dataSource.setPassword(dbPassword);
        }

        // Ensure schema is created before any other bean uses the dataSource
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.setContinueOnError(true);
        org.springframework.jdbc.datasource.init.DatabasePopulatorUtils.execute(populator, dataSource);

        return dataSource;
    }

    @Bean
    public JdbcTemplate serviceJdbcTemplate(DataSource serviceDataSource) {
        return new JdbcTemplate(serviceDataSource);
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.setContinueOnError(true);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
