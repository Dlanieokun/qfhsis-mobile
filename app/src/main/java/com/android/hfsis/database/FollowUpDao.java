package com.android.hfsis.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

// Explicit import statement resolving the compiler search lookups
import com.android.hfsis.model.ClassificationEntity;
import com.android.hfsis.model.FollowUpEntity;

import java.util.List;

@Dao
public interface FollowUpDao {
    @Query("SELECT * FROM family_planning_follow_ups")
    List<FollowUpEntity> getAll();

    @Query("SELECT * FROM family_planning_follow_ups WHERE recordId = :recId ORDER BY id ASC")
    List<FollowUpEntity> getFollowUpsForRecord(int recId);

    @Query("SELECT * FROM family_planning_follow_ups WHERE recordId = :recId AND monthName = :month LIMIT 1")
    FollowUpEntity getFollowUpByMonth(int recId, String month);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertFollowUp(FollowUpEntity followUp);

    @Update
    void updateFollowUp(FollowUpEntity followUp);

    @Query("SELECT * FROM family_planning_follow_ups WHERE isSynced = 0")
    List<FollowUpEntity> getUnsyncedRecords();

    @Query("UPDATE family_planning_follow_ups SET isSynced = 1, newInsert = 0 WHERE id IN (:ids)")
    void markAsSynced(List<Integer> ids);

    @Query("SELECT * FROM family_planning_follow_ups WHERE newInsert = 1")
    List<FollowUpEntity> getNewInsertRecords();

    @Query("UPDATE family_planning_follow_ups SET newInsert = 0 WHERE id IN (:ids)")
    void markAsInserted(List<Integer> ids);
}