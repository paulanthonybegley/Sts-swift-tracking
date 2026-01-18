package com.example.sts.service;

import com.example.sts.service.config.ServiceAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

public class ServiceMain {
    private static final Logger log = LoggerFactory.getLogger(ServiceMain.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServiceAppConfig.class);

        try {
            DataSource ds = context.getBean(DataSource.class);
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
            populator.execute(ds);
            log.info("Service database schema initialized.");
        } catch (Exception e) {
            log.error("Failed to initialize service database schema", e);
        }

        // This is primarily for testing/demo purposes as its a library module
        UetrService service = context.getBean(UetrService.class);
        log.info("UETRs Loaded: {}", service.loadUetrs());

        context.close();
    }
}
