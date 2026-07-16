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

    @Query("SELECT * FROM leprosy_registry WHERE isSynced = 0")
    List<LeprosyRegistryRecord> getUnsyncedRecords();

    @Query("UPDATE leprosy_registry SET isSynced = 1, newInsert = 0 WHERE id IN (:ids)")
    void markAsSynced(List<Long> ids);

    @Query("SELECT * FROM leprosy_registry WHERE newInsert = 1")
    List<LeprosyRegistryRecord> getNewInsertRecords();

    @Query("UPDATE leprosy_registry SET newInsert = 0 WHERE id IN (:ids)")
    void markAsInserted(List<Long> ids);
}