package com.example.sts.job;

import com.example.sts.job.processor.UetrProcessor;
import com.example.sts.job.visitor.AsciiDocVisitor;
import com.example.sts.job.visitor.AuditingVisitor;
import com.example.sts.job.visitor.PlantUMLVisitor;
import com.example.sts.job.visitor.TransactionVisitor;
import com.example.sts.model.Transaction;
import com.example.sts.service.UetrService;
import com.example.sts.service.client.TrackerClient;
import com.example.sts.service.config.ServiceAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;

public class JobMain {
    private static final Logger log = LoggerFactory.getLogger(JobMain.class);

    public static void main(String[] args) {
        log.info("Starting STS Job Orchestrator...");

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                ServiceAppConfig.class)) {
            UetrService uetrService = context.getBean(UetrService.class);
            TrackerClient client = new TrackerClient();

            // 1. Get Authentication
            String token = client.login("admin", "password");
            if (token == null) {
                log.error("Authentication failed. Exiting job.");
                return;
            }

            // 2. Setup Visitors
            AuditingVisitor auditVisitor = new AuditingVisitor();
            AsciiDocVisitor docVisitor = new AsciiDocVisitor("transaction_report.adoc");
            PlantUMLVisitor umlVisitor = new PlantUMLVisitor("sequence_diagram.puml");
            umlVisitor.startDiagram();

            List<TransactionVisitor> visitors = Arrays.asList(auditVisitor, docVisitor, umlVisitor);

            // 3. Process each active UETR
            List<String> activeUetrs = uetrService.loadUetrs();
            log.info("Found {} active UETRs in local DB", activeUetrs.size());

            for (String uetr : activeUetrs) {
                // Fetch update from Mock (Source of Truth)
                Transaction update = client.getTransaction(token, uetr);

                if (update != null) {
                    // Apply Service Logic
                    uetrService.processUpdate(update);

                    // apply Visitors directly
                    for (TransactionVisitor visitor : visitors) {
                        visitor.visit(update);
                    }
                }
            }

            umlVisitor.endDiagram();
            log.info("Job completed successfully.");
        }
    }
}
