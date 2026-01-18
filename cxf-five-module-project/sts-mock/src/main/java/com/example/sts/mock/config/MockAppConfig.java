package com.example.sts.mock.config;

import com.example.sts.mock.api.AuthenticationController;
import com.example.sts.mock.api.PaymentTransactionController;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@ComponentScan(basePackages = "com.example.sts.mock")
public class MockAppConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite:mock_server.db");

        // Ensure schema is created before any other bean uses the dataSource
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.setContinueOnError(true);
        org.springframework.jdbc.datasource.init.DatabasePopulatorUtils.execute(populator, dataSource);

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(destroyMethod = "shutdown")
    public Bus cxf() {
        return new SpringBus();
    }

    @Bean
    public Server jaxRsServer(Bus bus,
            PaymentTransactionController paymentController,
            AuthenticationController authController) {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setBus(bus);
        factory.setServiceBeans(Arrays.asList(paymentController, authController));
        factory.setAddress("http://localhost:8080/api");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        JacksonXmlBindJsonProvider provider = new JacksonXmlBindJsonProvider();
        provider.setMapper(mapper);
        factory.setProvider(provider);

        // Add OpenAPI feature (requires cxf-rt-rs-service-description-openapi-v3)
        // Disabling temporarily to fix compilation if dependency isn't being picked up
        // correctly
        // OpenApiFeature openApiFeature = new OpenApiFeature();
        // openApiFeature.setSupportSwaggerUi(true);
        // factory.setFeatures(Arrays.asList(openApiFeature));

        return factory.create();
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
