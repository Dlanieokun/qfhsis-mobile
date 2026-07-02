package com.android.hfsis.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.FamilyPlanningRecord;
import java.util.List;

@Dao
public interface FamilyPlanningDao {


    @Query("SELECT * FROM family_planning_records")
    List<FamilyPlanningRecord> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecord(FamilyPlanningRecord record);

    @Update
    void update(FamilyPlanningRecord record);

    @Delete
    void delete(FamilyPlanningRecord record);

    @Query("SELECT fpr.* FROM family_planning_records fpr " +
            "LEFT JOIN family_planning_drop_outs fpdo ON fpr.id = fpdo.recordId " +
            "WHERE fpdo.recordId IS NULL " +
            "ORDER BY fpr.id DESC")
    List<FamilyPlanningRecord> getAllRecords();

    @Query("SELECT * FROM family_planning_records WHERE id = :recordId LIMIT 1")
    FamilyPlanningRecord getRecordById(int recordId);

    @Query("SELECT * FROM family_planning_records WHERE familySerialNumber = :serialNumber")
    List<FamilyPlanningRecord> getRecordsBySerialNumber(String serialNumber);

    @Query("SELECT fpr.* FROM family_planning_records fpr " +
            "INNER JOIN household_profiles hp ON fpr.profileId = hp.id " +
            "LEFT JOIN family_planning_drop_outs fpdo ON fpr.id = fpdo.recordId " +
            "WHERE fpdo.recordId IS NULL " +
            "AND (hp.memberLastName || ', ' || hp.memberFirstName || ' ' || COALESCE(hp.memberMiddleName, '')) LIKE :searchQuery " +
            "ORDER BY fpr.id DESC")
    List<FamilyPlanningRecord> searchRecordsByClientName(String searchQuery);
}