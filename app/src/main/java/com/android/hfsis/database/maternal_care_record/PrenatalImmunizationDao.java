package com.android.hfsis.database.maternal_care_record;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.maternal_care_record.Prenatal8AncEntity;
import com.android.hfsis.model.maternal_care_record.PrenatalImmunizationEntity;

import java.util.List;

@Dao
public interface PrenatalImmunizationDao {
    @Query("SELECT * FROM prenatal_immunization_records")
    List<PrenatalImmunizationEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertImmunizationRecord(PrenatalImmunizationEntity record);

    @Update
    void updateImmunizationRecord(PrenatalImmunizationEntity record);

    // Pulls down baseline historical immunization matches for single operational mode form management
    @Query("SELECT * FROM prenatal_immunization_records WHERE maternalRecordId = :maternalId LIMIT 1")
    PrenatalImmunizationEntity getRecordByMaternalId(int maternalId);
}