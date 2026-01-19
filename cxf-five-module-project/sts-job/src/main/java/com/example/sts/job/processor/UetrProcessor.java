package com.example.sts.job.processor;

import com.example.sts.model.PaymentTransaction166;

public interface UetrProcessor {
    void process(PaymentTransaction166 transaction);
}
