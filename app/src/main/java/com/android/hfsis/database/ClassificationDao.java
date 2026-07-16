package com.android.hfsis.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.android.hfsis.model.ClassificationEntity;

import java.util.List;

@Dao
public interface ClassificationDao {


    @Query("SELECT * FROM classification_metrics")
    List<ClassificationEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveClassification(ClassificationEntity metrics);

    @Query("SELECT * FROM classification_metrics WHERE profile_id = :profileId LIMIT 1")
    ClassificationEntity getClassificationByProfileId(long profileId);

    @Query("SELECT * FROM classification_metrics WHERE isSynced = 0")
    List<ClassificationEntity> getUnsyncedRecords();

    @Query("UPDATE classification_metrics SET isSynced = 1, newInsert = 0 WHERE id IN (:ids)")
    void markAsSynced(List<Long> ids);

    @Query("SELECT * FROM classification_metrics WHERE newInsert = 1")
    List<ClassificationEntity> getNewInsertRecords();

    @Query("UPDATE classification_metrics SET newInsert = 0 WHERE id IN (:ids)")
    void markAsInserted(List<Long> ids);
}