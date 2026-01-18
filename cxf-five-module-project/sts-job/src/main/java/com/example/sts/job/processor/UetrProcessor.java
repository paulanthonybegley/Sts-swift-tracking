package com.example.sts.job.processor;

import com.example.sts.model.Transaction;

public interface UetrProcessor {
    void process(Transaction transaction);
}

// Decorator Implementation
class LoggingUetrProcessor implements UetrProcessor {
    private final UetrProcessor delegate;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingUetrProcessor.class);

    public LoggingUetrProcessor(UetrProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void process(Transaction transaction) {
        log.info(">>> BEGIN Processing UETR: {} <<<", transaction.getUetr());
        long start = System.currentTimeMillis();

        delegate.process(transaction);

        long duration = System.currentTimeMillis() - start;
        log.info("<<< END Processing UETR: {} (Duration: {}ms) <<<", transaction.getUetr(), duration);
    }
}
