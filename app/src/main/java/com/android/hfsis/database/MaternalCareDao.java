package com.android.hfsis.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.DropOutEntity;
import com.android.hfsis.model.MaternalCareRecord;

import java.util.List;

@Dao
public interface MaternalCareDao {

    @Query("SELECT * FROM maternal_care_records")
    List<MaternalCareRecord> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMaternalRecord(MaternalCareRecord record);

    @Update
    void updateMaternalRecord(MaternalCareRecord record);

    @Query("SELECT * FROM maternal_care_records ORDER BY id DESC")
    List<MaternalCareRecord> getAllMaternalRecords();

    // Query to retrieve a single record for editing
    @Query("SELECT * FROM maternal_care_records WHERE id = :recordId LIMIT 1")
    MaternalCareRecord getMaternalRecordById(int recordId);
}