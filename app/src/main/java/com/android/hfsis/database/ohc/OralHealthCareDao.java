package com.android.hfsis.database.ohc;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.ohc.OralHealthCareEntity;

import java.util.List;

@Dao
public interface OralHealthCareDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OralHealthCareEntity entry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(OralHealthCareEntity entry);

    @Delete
    void delete(OralHealthCareEntity entry);

    @Query("SELECT * FROM oral_health_care WHERE id = :id")
    OralHealthCareEntity getById(int id);

    // Renamed from getAllEntries() to getAll() to match OtherServicesFragment implementation
    @Query("SELECT * FROM oral_health_care ORDER BY id DESC")
    List<OralHealthCareEntity> getAll();
}