package com.example.sts.mock.persistence;

import com.example.sts.model.PaymentTransaction166;
import com.example.sts.model.TransactionStatus;

public interface ITransitionHistoryStore {
    void recordTransition(String uetr, TransactionStatus from, TransactionStatus to, PaymentTransaction166 data);
}
