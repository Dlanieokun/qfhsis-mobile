package com.android.hfsis.database.maternal_care_record;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.maternal_care_record.PrenatalImmunizationEntity;
import com.android.hfsis.model.maternal_care_record.PrenatalSupplementationEntity;

import java.util.List;

@Dao
public interface PrenatalSupplementationDao {
    @Query("SELECT * FROM prenatal_supplementation_records")
    List<PrenatalSupplementationEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSupplementationRecord(PrenatalSupplementationEntity record);

    @Update
    void updateSupplementationRecord(PrenatalSupplementationEntity record);

    // Contextual query to match single-form instance checks per maternal case log
    @Query("SELECT * FROM prenatal_supplementation_records WHERE maternalRecordId = :maternalId LIMIT 1")
    PrenatalSupplementationEntity getRecordByMaternalId(int maternalId);
}