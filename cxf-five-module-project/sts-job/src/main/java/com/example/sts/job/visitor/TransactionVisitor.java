package com.example.sts.job.visitor;

import com.example.sts.model.Transaction;

public interface TransactionVisitor {
    void visit(Transaction transaction);
}
