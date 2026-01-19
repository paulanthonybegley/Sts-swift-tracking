package com.example.sts.service;

// No Transaction import needed
import com.example.sts.model.TransactionStatus;
import com.example.sts.service.persistence.ITrackingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class UetrService {
    private static final Logger log = LoggerFactory.getLogger(UetrService.class);

    private final ITrackingRepository repository;

    public UetrService(ITrackingRepository repository) {
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
        // Seed with the initial "From" BICs as Active (2) to reflect the start of the
        // simulation
        String efef = "eb3a88b0-3231-41f0-af8e-e5b86012efef"; // Starts at BANKFRBICXX
        String efeg = "eb3a88b0-3231-41f0-af8e-e5b86012efeg"; // Starts at BANKA

        repository.insertRecord(efef, "BANKFRBICXX", 2);
        repository.insertRecord(efeg, "BANKA", 2);
    }

    public List<String> loadUetrs() {
        return repository.findActiveUetrs();
    }

    public void processUpdate(com.example.sts.model.PaymentTransaction166 transaction) {
        String uetr = transaction.getUETR();
        TransactionStatus status = TransactionStatus.fromString(transaction.getTransactionStatus());

        String toNode = "UNKNOWN";
        if (transaction.getTransactionRouting() != null && !transaction.getTransactionRouting().isEmpty()) {
            // Requirement: Extract the CURRENT node from the LAST hop in the routing
            // history
            int lastIndex = transaction.getTransactionRouting().size() - 1;
            com.example.sts.model.TransactionRouting1 hop = transaction.getTransactionRouting().get(lastIndex);
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
