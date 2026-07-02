package com.android.hfsis.database.child;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.child.ChildImmunizationRecord;

import java.util.List;

@Dao
public interface ChildImmunizationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChildImmunizationRecord record);

    @Query("SELECT * FROM child_immunization_records")
    List<ChildImmunizationRecord> getAll();

    @Update
    void update(ChildImmunizationRecord record);

    @Delete
    void delete(ChildImmunizationRecord record);

    // Fetch all immunization records
    @Query("SELECT * FROM child_immunization_records ORDER BY id DESC")
    LiveData<List<ChildImmunizationRecord>> getAllRecords();

    // Fetch a single immunization record by database row ID
    @Query("SELECT * FROM child_immunization_records WHERE id = :recordId LIMIT 1")
    ChildImmunizationRecord getRecordById(long recordId);

    // Search query example to find records matching a child's name
    @Query("SELECT * FROM child_immunization_records WHERE childName LIKE :searchName")
    List<ChildImmunizationRecord> findRecordsByChildName(String searchName);
}