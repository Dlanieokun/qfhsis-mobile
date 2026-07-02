package com.android.hfsis.database.idpcs.sthpc;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.idpcs.sthpc.SoilTransmittedHelminthiasisRegistryRecord;

import java.util.List;

@Dao
public interface SoilTransmittedHelminthiasisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecord(SoilTransmittedHelminthiasisRegistryRecord record);

    @Update
    void updateRecord(SoilTransmittedHelminthiasisRegistryRecord record);

    @Delete
    void deleteRecord(SoilTransmittedHelminthiasisRegistryRecord record);

    @Query("SELECT * FROM sth_registry_records ORDER BY id DESC")
    List<SoilTransmittedHelminthiasisRegistryRecord> getAll();

    @Query("SELECT * FROM sth_registry_records ORDER BY id DESC")
    List<SoilTransmittedHelminthiasisRegistryRecord> getAllRecords();

    @Query("SELECT * FROM sth_registry_records WHERE id = :recordId LIMIT 1")
    SoilTransmittedHelminthiasisRegistryRecord getRecordById(int recordId);

    @Query("SELECT * FROM sth_registry_records WHERE name LIKE :searchQuery OR familySerialNumber LIKE :searchQuery ORDER BY id DESC")
    List<SoilTransmittedHelminthiasisRegistryRecord> searchRecords(String searchQuery);
}