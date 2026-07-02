package com.android.hfsis.database.environmental;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.environmental.EnvironmentalHealthModel;

import java.util.List;

@Dao
public interface EnvironmentalHealthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecord(EnvironmentalHealthModel record);

    @Update
    void updateRecord(EnvironmentalHealthModel record);

    @Delete
    void deleteRecord(EnvironmentalHealthModel record);

    @Query("SELECT * FROM environmental_health_records WHERE id = :id")
    EnvironmentalHealthModel getRecordById(long id);

    @Query("SELECT * FROM environmental_health_records ORDER BY householdHeadName ASC")
    List<EnvironmentalHealthModel> getAllRecords();

    @Query("SELECT * FROM environmental_health_records ORDER BY householdHeadName ASC")
    List<EnvironmentalHealthModel> getAll();
}