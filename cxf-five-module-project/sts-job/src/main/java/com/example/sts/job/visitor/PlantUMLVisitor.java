package com.example.sts.job.visitor;

// No Transaction import needed
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PlantUMLVisitor implements TransactionVisitor {
    private final String outputPath;

    public PlantUMLVisitor(String outputPath) {
        this.outputPath = outputPath;
    }

    public void startDiagram() {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputPath))) {
            out.println("@startuml");
            out.println("title Payment Flow");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endDiagram() {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputPath, true))) {
            out.println("@enduml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(com.example.sts.model.PaymentTransaction166 transaction) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputPath, true))) {
            String uetr = transaction.getUETR();
            out.println(String.format("== Flow for UETR: %s ==", uetr));
            out.println(String.format("group \"UETR: %s\"", uetr));

            if (transaction.getTransactionRouting() != null && !transaction.getTransactionRouting().isEmpty()) {
                for (com.example.sts.model.TransactionRouting1 hop : transaction.getTransactionRouting()) {
                    String from = hop.getFrom();
                    String to = hop.getTo() != null ? hop.getTo() : "UNKNOWN";
                    out.println(String.format("\"%s\" -> \"%s\": Payment Update (%s)",
                            from, to, transaction.getTransactionStatus()));
                }
            } else {
                out.println(String.format("note right: No routing info for %s", uetr));
            }
            out.println("end");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
