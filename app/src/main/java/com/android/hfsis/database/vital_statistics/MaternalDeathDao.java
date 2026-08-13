package com.android.hfsis.database.vital_statistics;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.vital_statistics.MaternalDeathRecord;

import java.util.List;

@Dao
public interface MaternalDeathDao {

    @Insert
    long insert(MaternalDeathRecord record);

    @Update
    void update(MaternalDeathRecord record);

    @Delete
    void delete(MaternalDeathRecord record);

    @Query("SELECT * FROM maternal_deaths ORDER BY id DESC")
    List<MaternalDeathRecord> getAllRecords();

    @Query("SELECT * FROM maternal_deaths WHERE id = :id")
    MaternalDeathRecord getRecordById(int id);

    @Query("SELECT * FROM maternal_deaths WHERE synced = 0 ORDER BY id DESC")
    List<MaternalDeathRecord> getUnsyncedRecords();

    @Query("SELECT COUNT(*) FROM maternal_deaths")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM maternal_deaths WHERE synced = 0")
    int getUnsyncedCount();

    @Query("UPDATE maternal_deaths SET synced = 1, syncTimestamp = :timestamp WHERE id = :id")
    void markAsSynced(int id, long timestamp);

    @Query("UPDATE maternal_deaths SET synced = 1, syncTimestamp = :timestamp WHERE id IN (:ids)")
    void markMultipleAsSynced(List<Integer> ids, long timestamp);

    @Query("DELETE FROM maternal_deaths")
    void deleteAll();
}