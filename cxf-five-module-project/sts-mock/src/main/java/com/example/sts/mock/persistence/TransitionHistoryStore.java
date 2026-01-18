package com.example.sts.mock.persistence;

import com.example.sts.model.Transaction;
import com.example.sts.model.TransactionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public class TransitionHistoryStore {
    private static final Logger log = LoggerFactory.getLogger(TransitionHistoryStore.class);
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransitionHistoryStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void recordTransition(String uetr, TransactionStatus from, TransactionStatus to, Transaction data) {
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

        log.info("Recorded transition for UETR: {} [{} -> {}]", uetr, from, to);
    }
}
