package com.example.sts.job.visitor;

// No Transaction import needed
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditingVisitor implements TransactionVisitor {
    private static final Logger log = LoggerFactory.getLogger(AuditingVisitor.class);

    @Override
    public void visit(com.example.sts.model.PaymentTransaction166 transaction) {
        log.info("[AUDIT] Processing UETR: {}. Masking PII...", transaction.getUETR());

        String fromNode = "UNKNOWN";
        String toNode = "UNKNOWN";

        if (transaction.getTransactionRouting() != null && !transaction.getTransactionRouting().isEmpty()) {
            com.example.sts.model.TransactionRouting1 lastHop = transaction.getTransactionRouting()
                    .get(transaction.getTransactionRouting().size() - 1);
            fromNode = lastHop.getFrom();
            if (lastHop.getTo() != null) {
                toNode = lastHop.getTo();
            }
        }

        String maskedFrom = fromNode.length() >= 2 ? fromNode.substring(0, 2) + "****" : "****";
        String maskedTo = toNode.length() >= 2 ? toNode.substring(0, 2) + "****" : "****";

        String amt = "0.0";
        String curr = "XXX";
        if (transaction.getTransactionInstructedAmount() != null) {
            amt = transaction.getTransactionInstructedAmount().getAmount();
            curr = transaction.getTransactionInstructedAmount().getCurrency();
        }

        log.info("[AUDIT] From: {}, To: {}, Amount: {} {}", maskedFrom, maskedTo, amt, curr);
    }
}
