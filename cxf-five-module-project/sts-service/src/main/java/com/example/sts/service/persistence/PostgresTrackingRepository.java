package com.example.sts.service.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

public class PostgresTrackingRepository implements ITrackingRepository {
    private static final Logger log = LoggerFactory.getLogger(PostgresTrackingRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public PostgresTrackingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private synchronized int getNextId() {
        Integer maxId = jdbcTemplate.queryForObject("SELECT MAX(ID) FROM tracking", Integer.class);
        return (maxId == null) ? 1 : maxId + 1;
    }

    @Override
    public void insertRecord(String uetr, String toNode, int status) {
        int id = getNextId();
        String now = LocalDateTime.now().toString();
        jdbcTemplate.update(
                "INSERT INTO tracking (ID, PAYMENT_ID, UPDATE_DATE, MODIFIED_DATE, STATUS, TO_NODE) VALUES (?, ?, ?, ?, ?, ?)",
                id, uetr, now, now, status, toNode);
        log.info("[POSTGRES] Inserted tracking record: ID={}, UETR={}, TO={}, STATUS={}", id, uetr, toNode, status);
    }

    @Override
    public void updatePreviousHopsToCompleted(String uetr) {
        jdbcTemplate.update(
                "UPDATE tracking SET STATUS = 3, MODIFIED_DATE = ? WHERE PAYMENT_ID = ? AND STATUS = 2",
                LocalDateTime.now().toString(), uetr);
    }

    @Override
    public List<String> findActiveUetrs() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT PAYMENT_ID FROM tracking WHERE STATUS IN (2, 3) AND PAYMENT_ID NOT IN (SELECT PAYMENT_ID FROM tracking WHERE STATUS = 4)",
                String.class);
    }

    @Override
    public int countTotalRecords() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tracking", Integer.class);
    }
}
