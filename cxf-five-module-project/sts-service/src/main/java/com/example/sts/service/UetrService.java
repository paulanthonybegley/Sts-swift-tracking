package com.example.sts.service;

// No Transaction import needed
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
        String defaultUetrg = "eb3a88b0-3231-41f0-af8e-e5b86012efeg";
        // Requirement: For first hop, insert record for FROM with status 3 and an entry
        // for TO with status 2.
        repository.insertRecord(defaultUetr, "BANKA", 3); // From
        repository.insertRecord(defaultUetr, "BANKB", 2); // To (Active)
        repository.insertRecord(defaultUetrg, "BANKA", 3); // From
        repository.insertRecord(defaultUetrg, "BANKB", 2); // To (Active)
    }

    public List<String> loadUetrs() {
        return repository.findActiveUetrs();
    }

    public void processUpdate(com.example.sts.model.PaymentTransaction166 transaction) {
        String uetr = transaction.getUETR();
        TransactionStatus status = TransactionStatus.fromString(transaction.getTransactionStatus());

        String toNode = "UNKNOWN";
        if (transaction.getTransactionRouting() != null && !transaction.getTransactionRouting().isEmpty()) {
            com.example.sts.model.TransactionRouting1 hop = transaction.getTransactionRouting().get(0);
            toNode = hop.getTo() != null ? hop.getTo() : "UNKNOWN";
        }

        log.info("Processing update for UETR: {} -> Status: {}, To: {}", uetr, status, toNode);

        if (status == TransactionStatus.ACCC) {
            repository.updatePreviousHopsToCompleted(uetr);
            repository.insertRecord(uetr, toNode, 3);
        } else if (status == TransactionStatus.RJCT) {
            repository.updatePreviousHopsToCompleted(uetr);
            repository.insertRecord(uetr, toNode, 4);
        } else if (status == TransactionStatus.PDNG) {
            // PDNG -> Moving to next hop
            repository.updatePreviousHopsToCompleted(uetr);
            repository.insertRecord(uetr, toNode, 2);
        }
    }
}
