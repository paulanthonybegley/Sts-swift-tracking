package com.example.sts.mock.config;

import com.example.sts.mock.api.AuthenticationController;
import com.example.sts.mock.api.PaymentTransactionController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@ComponentScan(basePackages = "com.example.sts.mock")
@PropertySource("classpath:application.properties")
public class MockAppConfig {

    @Value("${server.port:8080}")
    private int port;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.ssl.key-store:}")
    private String keyStore;

    @Value("${server.ssl.key-store-password:}")
    private String keyStorePassword;

    @Value("${server.ssl.key-password:}")
    private String keyPassword;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

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
            AuthenticationController authController,
            JettyHTTPServerEngineFactory jettyEngineFactory) {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setBus(bus);
        factory.setServiceBeans(Arrays.asList(paymentController, authController));

        String protocol = sslEnabled ? "https" : "http";
        factory.setAddress(protocol + "://localhost:" + port + "/api");
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
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
    public JettyHTTPServerEngineFactory jettyEngineFactory(Bus bus) throws Exception {
        JettyHTTPServerEngineFactory factory = new JettyHTTPServerEngineFactory();
        factory.setBus(bus);

        if (sslEnabled) {
            TLSServerParameters tls = new TLSServerParameters();

            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource(keyStore).getInputStream()) {
                ks.load(is, keyStorePassword.toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keyPassword.toCharArray());
            tls.setKeyManagers(kmf.getKeyManagers());

            factory.setTLSServerParametersForPort(port, tls);
        }

        return factory;
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
