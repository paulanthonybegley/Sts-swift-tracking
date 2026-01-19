package com.example.sts.service.persistence;

import java.util.List;

public interface ITrackingRepository {
    void insertRecord(String uetr, String toNode, int status);

    void updatePreviousHopsToCompleted(String uetr);

    List<String> findActiveUetrs();

    int countTotalRecords();
}
