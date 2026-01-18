package com.example.sts.job.visitor;

import com.example.sts.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditingVisitor implements TransactionVisitor {
    private static final Logger log = LoggerFactory.getLogger(AuditingVisitor.class);

    @Override
    public void visit(Transaction transaction) {
        log.info("[AUDIT] Processing UETR: {}. Masking PII...", transaction.getUetr());
        // Simulating PII redaction by creating a log-safe representation
        String maskedFrom = transaction.getFrom().substring(0, 2) + "****";
        String maskedTo = transaction.getTo().substring(0, 2) + "****";
        log.info("[AUDIT] From: {}, To: {}, Amount: {} {}", maskedFrom, maskedTo, transaction.getAmount(),
                transaction.getCurrency());
    }
}
