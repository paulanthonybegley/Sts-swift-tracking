package com.example.sts.model;

public enum TransactionStatus {
    PDNG,
    ACCC,
    RJCT;

    public static TransactionStatus fromString(String value) {
        if (value == null)
            return null;
        try {
            return TransactionStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
