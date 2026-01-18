package com.example.sts.service.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class TrackingRepository {
    private static final Logger log = LoggerFactory.getLogger(TrackingRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public TrackingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private synchronized int getNextId() {
        Integer maxId = jdbcTemplate.queryForObject("SELECT MAX(ID) FROM tracking", Integer.class);
        return (maxId == null) ? 1 : maxId + 1;
    }

    public void insertRecord(String uetr, String toNode, int status) {
        int id = getNextId();
        String now = LocalDateTime.now().toString();
        jdbcTemplate.update(
                "INSERT INTO tracking (ID, PAYMENT_ID, UPDATE_DATE, MODIFIED_DATE, STATUS, TO_NODE) VALUES (?, ?, ?, ?, ?, ?)",
                id, uetr, now, now, status, toNode);
        log.info("Inserted tracking record: ID={}, UETR={}, TO={}, STATUS={}", id, uetr, toNode, status);
    }

    public void updatePreviousHopsToCompleted(String uetr) {
        jdbcTemplate.update(
                "UPDATE tracking SET STATUS = 3, MODIFIED_DATE = ? WHERE PAYMENT_ID = ? AND STATUS = 2",
                LocalDateTime.now().toString(), uetr);
    }

    public List<String> findActiveUetrs() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT PAYMENT_ID FROM tracking WHERE STATUS IN (2, 3) AND PAYMENT_ID NOT IN (SELECT PAYMENT_ID FROM tracking WHERE STATUS = 4)",
                String.class);
    }

    public int countTotalRecords() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tracking", Integer.class);
    }
}
