package com.android.hfsis.database.idpcs.filariasis;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.idpcs.filariasis.FilariasisRegistryRecord;

import java.util.List;

@Dao
public interface FilariasisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecord(FilariasisRegistryRecord record);

    @Update
    void updateRecord(FilariasisRegistryRecord record);

    @Delete
    void deleteRecord(FilariasisRegistryRecord record);

    @Query("SELECT * FROM filariasis_registry_table WHERE id = :id LIMIT 1")
    FilariasisRegistryRecord getRecordById(long id);

    @Query("SELECT * FROM filariasis_registry_table ORDER BY id DESC")
    List<FilariasisRegistryRecord> getAllRecords();

    @Query("SELECT * FROM filariasis_registry_table ORDER BY id DESC")
    List<FilariasisRegistryRecord> getAll();
}