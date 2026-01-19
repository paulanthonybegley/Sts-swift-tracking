package com.example.sts.job.processor;

import com.example.sts.model.PaymentTransaction166;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUetrProcessor implements UetrProcessor {
    private final UetrProcessor delegate;
    private static final Logger log = LoggerFactory.getLogger(LoggingUetrProcessor.class);

    public LoggingUetrProcessor(UetrProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void process(PaymentTransaction166 transaction) {
        log.info(">>> BEGIN Processing UETR: {} <<<", transaction.getUETR());
        long start = System.currentTimeMillis();

        delegate.process(transaction);

        long duration = System.currentTimeMillis() - start;
        log.info("<<< END Processing UETR: {} (Duration: {}ms) <<<", transaction.getUETR(), duration);
    }
}
