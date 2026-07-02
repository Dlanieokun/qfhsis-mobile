package com.android.hfsis.database.child;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.child.ChildImmunizationSchoolRecord;

import java.util.List;

@Dao
public interface ChildImmunizationSchoolDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChildImmunizationSchoolRecord record);

    @Query("SELECT * FROM child_immunization_school_records")
    List<ChildImmunizationSchoolRecord> getAll();

    @Update
    void update(ChildImmunizationSchoolRecord record);

    @Delete
    void delete(ChildImmunizationSchoolRecord record);

    // Fetch all records descending to show newest first
    @Query("SELECT * FROM child_immunization_school_records ORDER BY id DESC")
    LiveData<List<ChildImmunizationSchoolRecord>> getAllSchoolRecords();

    // Fetch single record for deep loading/editing actions
    @Query("SELECT * FROM child_immunization_school_records WHERE id = :recordId LIMIT 1")
    ChildImmunizationSchoolRecord getRecordById(long recordId);


    // Filtered lookup feature based on name, layout serials, or addresses
    @Query("SELECT * FROM child_immunization_school_records WHERE childName LIKE :searchQuery OR familySerialNumber LIKE :searchQuery OR address LIKE :searchQuery ORDER BY id DESC")
    List<ChildImmunizationSchoolRecord> searchSchoolRecords(String searchQuery);
}