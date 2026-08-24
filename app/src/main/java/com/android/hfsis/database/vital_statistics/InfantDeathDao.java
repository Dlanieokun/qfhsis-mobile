package com.android.hfsis.database.vital_statistics;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.vital_statistics.InfantDeathRecord;

import java.util.List;

@Dao
public interface InfantDeathDao {

    @Insert
    long insert(InfantDeathRecord record);

    @Update
    void update(InfantDeathRecord record);

    @Delete
    void delete(InfantDeathRecord record);

    @Query("SELECT * FROM infant_deaths ORDER BY id DESC")
    List<InfantDeathRecord> getAllRecords();

    @Query("SELECT * FROM infant_deaths WHERE id = :id")
    InfantDeathRecord getRecordById(int id);

    @Query("SELECT * FROM infant_deaths WHERE synced = 0 ORDER BY id DESC")
    List<InfantDeathRecord> getUnsyncedRecords();

    @Query("SELECT COUNT(*) FROM infant_deaths")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM infant_deaths WHERE synced = 0")
    int getUnsyncedCount();

    @Query("UPDATE infant_deaths SET synced = 1, syncTimestamp = :timestamp WHERE id = :id")
    void markAsSynced(int id, long timestamp);

    @Query("UPDATE infant_deaths SET synced = 1, syncTimestamp = :timestamp WHERE id IN (:ids)")
    void markMultipleAsSynced(List<Integer> ids, long timestamp);

    @Query("UPDATE infant_deaths SET synced = 1 WHERE id IN (:ids)")
    void markAsSynced(List<Integer> ids);

    @Query("DELETE FROM infant_deaths")
    void deleteAll();
}