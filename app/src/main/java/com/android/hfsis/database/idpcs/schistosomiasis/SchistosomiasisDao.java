package com.android.hfsis.database.idpcs.schistosomiasis;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.android.hfsis.model.idpcs.schistosomiasis.SchistosomiasisRegistryRecord;
import java.util.List;

@Dao
public interface SchistosomiasisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SchistosomiasisRegistryRecord record);

    @Update
    void update(SchistosomiasisRegistryRecord record);

    @Delete
    void delete(SchistosomiasisRegistryRecord record);

    @Query("SELECT * FROM schistosomiasis_registry WHERE id = :id")
    SchistosomiasisRegistryRecord getRecordById(long id);

    @Query("SELECT * FROM schistosomiasis_registry ORDER BY id DESC")
    List<SchistosomiasisRegistryRecord> getAllRecords();

    @Query("SELECT * FROM schistosomiasis_registry ORDER BY id DESC")
    List<SchistosomiasisRegistryRecord> getAll();
}