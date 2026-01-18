package com.example.sts.job.visitor;

import com.example.sts.model.Transaction;
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
    public void visit(Transaction transaction) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputPath, true))) {
            out.println(String.format("\"%s\" -> \"%s\": Payment Update (%s)",
                    transaction.getFrom(), transaction.getTo(), transaction.getStatus()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
