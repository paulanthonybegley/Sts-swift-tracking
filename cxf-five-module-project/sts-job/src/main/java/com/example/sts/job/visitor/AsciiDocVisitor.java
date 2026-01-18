package com.example.sts.job.visitor;

import com.example.sts.model.Transaction;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AsciiDocVisitor implements TransactionVisitor {
    private final String outputPath;

    public AsciiDocVisitor(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void visit(Transaction transaction) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputPath, true))) {
            out.println("== Transaction: " + transaction.getUetr());
            out.println("- *Status:* " + transaction.getStatus());
            out.println("- *Hop:* " + transaction.getFrom() + " -> " + transaction.getTo());
            out.println("- *Amount:* " + transaction.getAmount() + " " + transaction.getCurrency());
            out.println("- *Time:* " + transaction.getTimestamp());
            out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
