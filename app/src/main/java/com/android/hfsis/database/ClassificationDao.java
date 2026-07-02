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
}