package com.sahyadri.samrakshane;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AlertEntity alert);

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    List<AlertEntity> getAllAlerts();

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC LIMIT :limit")
    List<AlertEntity> getLatestAlerts(int limit);

    @Query("SELECT COUNT(*) FROM alerts")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM alerts WHERE status != :resolvedStatus")
    int getActiveCount(String resolvedStatus);
}

