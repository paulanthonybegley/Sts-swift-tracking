package com.example.sts.mock;

import com.example.sts.mock.config.MockAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

public class MockServerMain {
    private static final Logger log = LoggerFactory.getLogger(MockServerMain.class);

    public static void main(String[] args) {
        log.info("Starting Mock STS Server...");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MockAppConfig.class);

        // Initialize Database
        try {
            DataSource ds = context.getBean(DataSource.class);
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
            populator.execute(ds);
            log.info("Database schema initialized.");
        } catch (Exception e) {
            log.error("Failed to initialize database schema", e);
        }

        log.info("Mock STS Server is running at http://localhost:8080/");

        // Keep main thread alive
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Mock STS Server...");
            context.close();
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            log.info("Mock STS Server interrupted");
            Thread.currentThread().interrupt();
        }
    }
}
