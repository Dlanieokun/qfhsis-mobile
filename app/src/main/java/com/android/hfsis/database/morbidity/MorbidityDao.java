package com.android.hfsis.database.morbidity;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.morbidity.MorbidityRecord;

import java.util.List;

@Dao
public interface MorbidityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MorbidityRecord record);

    @Update
    void update(MorbidityRecord record);

    @Delete
    void delete(MorbidityRecord record);

    @Query("SELECT * FROM morbidity_records ORDER BY createdAt DESC")
    List<MorbidityRecord> getAllRecords();

    @Query("SELECT * FROM morbidity_records WHERE id = :id LIMIT 1")
    MorbidityRecord getRecordById(int id);

    @Query("SELECT * FROM morbidity_records WHERE householdId = :householdId ORDER BY createdAt DESC")
    List<MorbidityRecord> getRecordsByHousehold(String householdId);

    @Query("SELECT * FROM morbidity_records WHERE reportYear = :year AND reportMonth = :month ORDER BY diseaseName ASC")
    List<MorbidityRecord> getRecordsByPeriod(String year, String month);

    @Query("SELECT * FROM morbidity_records WHERE isSynced = 0")
    List<MorbidityRecord> getUnsyncedRecords();

    @Query("UPDATE morbidity_records SET isSynced = 1 WHERE id = :id")
    void markAsSynced(int id);

    @Query("UPDATE morbidity_records SET isSynced = 1 WHERE id IN (:ids)")
    void markAsSynced(List<Integer> ids);

    @Query("DELETE FROM morbidity_records WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM morbidity_records")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM morbidity_records")
    int getTotalCount();
}