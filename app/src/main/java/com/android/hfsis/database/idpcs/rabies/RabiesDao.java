package com.android.hfsis.database.idpcs.rabies;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.idpcs.rabies.RabiesRecord;

import java.util.List;

@Dao
public interface RabiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdate(RabiesRecord record);

    @Update
    void update(RabiesRecord record);

    @Query("SELECT * FROM rabies_records WHERE id = :id")
    RabiesRecord getRecordById(long id);

    @Query("SELECT * FROM rabies_records ORDER BY id DESC")
    List<RabiesRecord> getAllRecords();

    @Query("SELECT * FROM rabies_records ORDER BY id DESC")
    List<RabiesRecord> getAll();
}