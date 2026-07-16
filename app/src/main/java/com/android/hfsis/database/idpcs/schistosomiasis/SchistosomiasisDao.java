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

    @Query("SELECT * FROM schistosomiasis_registry WHERE isSynced = 0")
    List<SchistosomiasisRegistryRecord> getUnsyncedRecords();

    @Query("UPDATE schistosomiasis_registry SET isSynced = 1, newInsert = 0 WHERE id IN (:ids)")
    void markAsSynced(List<Long> ids);

    @Query("SELECT * FROM schistosomiasis_registry WHERE newInsert = 1")
    List<SchistosomiasisRegistryRecord> getNewInsertRecords();

    @Query("UPDATE schistosomiasis_registry SET newInsert = 0 WHERE id IN (:ids)")
    void markAsInserted(List<Long> ids);
}