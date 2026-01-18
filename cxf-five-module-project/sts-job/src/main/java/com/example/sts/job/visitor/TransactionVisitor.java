package com.example.sts.job.visitor;

import com.example.sts.model.PaymentTransaction166;

public interface TransactionVisitor {
    void visit(PaymentTransaction166 transaction);
}
