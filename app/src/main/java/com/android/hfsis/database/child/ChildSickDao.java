package com.android.hfsis.database.child;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.child.ChildSickRecord;

import java.util.List;

@Dao
public interface ChildSickDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChildSickRecord record);

    @Update
    void update(ChildSickRecord record);

    @Delete
    void delete(ChildSickRecord record);

    @Query("SELECT * FROM child_sick_records ORDER BY id DESC")
    List<ChildSickRecord> getAllRecords();

    @Query("SELECT * FROM child_sick_records WHERE id = :id LIMIT 1")
    ChildSickRecord getRecordById(long id);

    // Filter used inside ViewChildManagementOfSickFragment live lookup loops
    @Query("SELECT * FROM child_sick_records WHERE childName LIKE :searchQuery OR familySerialNumber LIKE :searchQuery ORDER BY id DESC")
    List<ChildSickRecord> searchRecords(String searchQuery);

    @Query("SELECT * FROM child_sick_records WHERE isSynced = 0")
    List<ChildSickRecord> getUnsyncedRecords();

    @Query("UPDATE child_sick_records SET isSynced = 1 WHERE id IN (:ids)")
    void markAsSynced(List<Long> ids);
}