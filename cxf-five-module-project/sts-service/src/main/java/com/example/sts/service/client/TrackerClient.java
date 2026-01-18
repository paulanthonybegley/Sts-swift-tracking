package com.example.sts.service.client;

// No Transaction import needed
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;
import org.apache.cxf.jaxrs.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.Collections;

@Component
public class TrackerClient {
    private static final Logger log = LoggerFactory.getLogger(TrackerClient.class);
    private static final String BASE_URL = "http://localhost:8080/api";

    public String login(String username, String password) {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        JacksonXmlBindJsonProvider provider = new JacksonXmlBindJsonProvider();
        provider.setMapper(mapper);

        WebClient client = WebClient.create(BASE_URL + "/authentication/token",
                Collections.singletonList(provider));
        Response response = client.header("Authorization", authHeader).post(null);

        if (response.getStatus() == 200) {
            return response.readEntity(String.class);
        } else {
            log.error("Login failed with status: {}", response.getStatus());
            return null;
        }
    }

    public com.example.sts.model.PaymentTransaction166 getTransaction(String token, String uetr) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        JacksonXmlBindJsonProvider provider = new JacksonXmlBindJsonProvider();
        provider.setMapper(mapper);

        WebClient client = WebClient.create(BASE_URL + "/payment-transactions/" + uetr,
                Collections.singletonList(provider));
        Response response = client.header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if (response.getStatus() == 200) {
            return response.readEntity(com.example.sts.model.PaymentTransaction166.class);
        } else {
            log.error("Failed to get transaction {} with status: {}", uetr, response.getStatus());
            return null;
        }
    }
}
