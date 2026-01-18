package com.example.sts.job.visitor;

// No Transaction import needed
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AsciiDocVisitor implements TransactionVisitor {
    private final String outputPath;

    public AsciiDocVisitor(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void visit(com.example.sts.model.PaymentTransaction166 transaction) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputPath, true))) {
            out.println("== Transaction: " + transaction.getUETR());
            out.println("- *Status:* " + transaction.getTransactionStatus());

            String from = "N/A";
            String to = "N/A";
            if (transaction.getTransactionRouting() != null && !transaction.getTransactionRouting().isEmpty()) {
                com.example.sts.model.TransactionRouting1 hop = transaction.getTransactionRouting().get(0);
                from = hop.getFrom();
                to = hop.getTo() != null ? hop.getTo() : "N/A";
            }
            out.println("- *Hop:* " + from + " -> " + to);

            if (transaction.getTransactionInstructedAmount() != null) {
                out.println("- *Amount:* " + transaction.getTransactionInstructedAmount().getAmount() + " "
                        + transaction.getTransactionInstructedAmount().getCurrency());
            }
            out.println("- *Time:* " + transaction.getTransactionInitiationDateTime());
            out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
