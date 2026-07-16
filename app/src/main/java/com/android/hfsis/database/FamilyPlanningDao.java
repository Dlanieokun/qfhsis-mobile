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

    // FIX: previously LEFT JOINed family_planning_drop_outs and filtered out
    // any record with a matching drop-out row (WHERE fpdo.recordId IS NULL).
    // That meant saving a drop-out made the record vanish from this list even
    // though it was never deleted from family_planning_records. Now it just
    // returns every record; FamilyPlanningAdapter marks dropped-out ones so
    // they're still distinguishable in the UI instead of disappearing.
    @Query("SELECT * FROM family_planning_records ORDER BY id DESC")
    List<FamilyPlanningRecord> getAllRecords();

    @Query("SELECT * FROM family_planning_records WHERE id = :recordId LIMIT 1")
    FamilyPlanningRecord getRecordById(int recordId);

    @Query("SELECT * FROM family_planning_records WHERE familySerialNumber = :serialNumber")
    List<FamilyPlanningRecord> getRecordsBySerialNumber(String serialNumber);

    // FIX: same issue as getAllRecords() above — dropped the LEFT JOIN/WHERE
    // clause that hid dropped-out clients from search results.
    @Query("SELECT fpr.* FROM family_planning_records fpr " +
            "INNER JOIN household_profiles hp ON fpr.profileId = hp.id " +
            "WHERE (hp.memberLastName || ', ' || hp.memberFirstName || ' ' || COALESCE(hp.memberMiddleName, '')) LIKE :searchQuery " +
            "ORDER BY fpr.id DESC")
    List<FamilyPlanningRecord> searchRecordsByClientName(String searchQuery);

    @Query("SELECT * FROM family_planning_records WHERE isSynced = 0")
    List<FamilyPlanningRecord> getUnsyncedRecords();

    @Query("UPDATE family_planning_records SET isSynced = 1, newInsert = 0 WHERE id IN (:ids)")
    void markAsSynced(List<Integer> ids);

    @Query("SELECT * FROM family_planning_records WHERE newInsert = 1")
    List<FamilyPlanningRecord> getNewInsertRecords();

    @Query("UPDATE family_planning_records SET newInsert = 0 WHERE id IN (:ids)")
    void markAsInserted(List<Integer> ids);
}