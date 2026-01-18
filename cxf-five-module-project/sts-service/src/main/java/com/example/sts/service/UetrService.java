package com.example.sts.service;

import com.example.sts.model.Transaction;
import com.example.sts.model.TransactionStatus;
import com.example.sts.service.persistence.TrackingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class UetrService {
    private static final Logger log = LoggerFactory.getLogger(UetrService.class);

    private final TrackingRepository repository;

    public UetrService(TrackingRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing UetrService...");
        if (repository.countTotalRecords() == 0) {
            log.info("Local DB is empty. Seeding defaults...");
            seedDefaults();
        } else {
            log.info("Local DB already contains records. Skipping seed.");
        }
    }

    private void seedDefaults() {
        // Seed with a default UETR to start tracking
        String defaultUetr = "eb3a88b0-3231-41f0-af8e-e5b86012efef";
        // Requirement: For first hop, insert record for FROM with status 3 and an entry
        // for TO with status 2.
        repository.insertRecord(defaultUetr, "BANKA", 3); // From
        repository.insertRecord(defaultUetr, "BANKB", 2); // To (Active)
    }

    public List<String> loadUetrs() {
        return repository.findActiveUetrs();
    }

    public void processUpdate(Transaction transaction) {
        String uetr = transaction.getUetr().toString();
        TransactionStatus status = transaction.getStatus();
        String toNode = transaction.getTo();

        log.info("Processing update for UETR: {} -> Status: {}, To: {}", uetr, status, toNode);

        // Logic:
        // If status is ACCC then last insert final status is 3
        // If status is RJCT then final status is 4
        // Otherwise (PDNG), update previous to 3 and insert new TO as 2

        if (status == TransactionStatus.ACCC) {
            repository.updatePreviousHopsToCompleted(uetr);
            repository.insertRecord(uetr, toNode, 3);
        } else if (status == TransactionStatus.RJCT) {
            repository.updatePreviousHopsToCompleted(uetr);
            repository.insertRecord(uetr, toNode, 4);
        } else {
            // PDNG -> Moving to next hop
            repository.updatePreviousHopsToCompleted(uetr);
            repository.insertRecord(uetr, toNode, 2);
        }
    }
}
