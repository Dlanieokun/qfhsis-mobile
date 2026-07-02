package com.android.hfsis.database.idpcs.leprosy;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.idpcs.leprosy.LeprosyRegistryRecord;

import java.util.List;

@Dao
public interface LeprosyRegistryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LeprosyRegistryRecord record);

    @Update
    void update(LeprosyRegistryRecord record);

    @Delete
    void delete(LeprosyRegistryRecord record);

    @Query("SELECT * FROM leprosy_registry ORDER BY id DESC")
    List<LeprosyRegistryRecord> getAllRecords();

    @Query("SELECT * FROM leprosy_registry WHERE id = :id LIMIT 1")
    LeprosyRegistryRecord getRecordById(long id);

    @Query("SELECT * FROM leprosy_registry ORDER BY id DESC")
    List<LeprosyRegistryRecord> getAll();
}