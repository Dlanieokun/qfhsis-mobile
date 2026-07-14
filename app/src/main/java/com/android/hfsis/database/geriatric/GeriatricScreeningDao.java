package com.android.hfsis.database.geriatric;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.geriatric.GeriatricScreeningRecord;

import java.util.List;

@Dao
public interface GeriatricScreeningDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GeriatricScreeningRecord record);

    @Update
    void update(GeriatricScreeningRecord record);

    @Delete
    void delete(GeriatricScreeningRecord record);

    @Query("SELECT * FROM geriatric_screening_records WHERE recordNo = :id")
    GeriatricScreeningRecord getRecordById(long id);

    @Query("SELECT * FROM geriatric_screening_records ORDER BY dateOfScreening DESC")
    List<GeriatricScreeningRecord> getAllRecords();

    @Query("SELECT * FROM geriatric_screening_records WHERE name LIKE :searchQuery OR familySerialNumber LIKE :searchQuery ORDER BY dateOfScreening DESC")
    List<GeriatricScreeningRecord> searchRecords(String searchQuery);

    @Query("SELECT * FROM geriatric_screening_records ORDER BY dateOfScreening DESC")
    List<GeriatricScreeningRecord> getAll();

    @Query("SELECT * FROM geriatric_screening_records WHERE isSynced = 0")
    List<GeriatricScreeningRecord> getUnsyncedRecords();

    // FIXED: Changed record_no to recordNo to match the Room-generated column name
    @Query("UPDATE geriatric_screening_records SET isSynced = 1 WHERE recordNo IN (:ids)")
    void markAsSynced(List<Integer> ids);
}