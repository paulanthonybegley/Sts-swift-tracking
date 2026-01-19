package com.example.sts.mock.persistence;

import com.example.sts.model.PaymentTransaction166;
import com.example.sts.model.TransactionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;

public class PostgresTransitionHistoryStore implements ITransitionHistoryStore {
    private static final Logger log = LoggerFactory.getLogger(PostgresTransitionHistoryStore.class);
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PostgresTransitionHistoryStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void recordTransition(String uetr, TransactionStatus from, TransactionStatus to,
            PaymentTransaction166 data) {
        String apiDataJson = null;
        try {
            apiDataJson = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to serialize transaction data for audit", e);
        }

        jdbcTemplate.update(
                "INSERT INTO transition_history (uetr, from_state, to_state, timestamp, api_data) VALUES (?, ?, ?, ?, ?)",
                uetr,
                from != null ? from.name() : "INIT",
                to.name(),
                OffsetDateTime.now().toString(),
                apiDataJson);

        log.info("[POSTGRES] Recorded transition for UETR: {} [{} -> {}]", uetr, from, to);
    }
}
