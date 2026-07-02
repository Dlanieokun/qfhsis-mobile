package com.android.hfsis.database.ncdps;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.ncdpcs.PhilPENAssessmentEntity;

import java.util.List;

@Dao
public interface PhilPENDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PhilPENAssessmentEntity assessment);

    @Update
    void update(PhilPENAssessmentEntity assessment);

    @Delete
    void delete(PhilPENAssessmentEntity assessment);

    @Query("SELECT * FROM philpen_risk_assessments ORDER BY id DESC")
    List<PhilPENAssessmentEntity> getAllAssessments();

    @Query("SELECT * FROM philpen_risk_assessments WHERE id = :id LIMIT 1")
    PhilPENAssessmentEntity getAssessmentById(long id);

    @Query("SELECT * FROM philpen_risk_assessments WHERE name LIKE :searchQuery OR familySerial LIKE :searchQuery")
    List<PhilPENAssessmentEntity> searchAssessments(String searchQuery);

    @Query("SELECT * FROM philpen_risk_assessments ORDER BY id DESC")
    List<PhilPENAssessmentEntity> getAll();
}