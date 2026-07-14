package com.android.hfsis.database.maternal_care_record;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.maternal_care_record.PrenatalLabScreeningEntity;
import com.android.hfsis.model.maternal_care_record.PrenatalSupplementationEntity;

import java.util.List;

@Dao
public interface PrenatalLabScreeningDao {
    @Query("SELECT * FROM prenatal_lab_screening_records")
    List<PrenatalLabScreeningEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertLabScreening(PrenatalLabScreeningEntity record);

    @Update
    void updateLabScreening(PrenatalLabScreeningEntity record);

    @Query("SELECT * FROM prenatal_lab_screening_records WHERE maternalRecordId = :maternalId LIMIT 1")
    PrenatalLabScreeningEntity getRecordByMaternalId(int maternalId);

    @Query("SELECT * FROM prenatal_lab_screening_records WHERE isSynced = 0")
    List<PrenatalLabScreeningEntity> getUnsyncedRecords();

    @Query("UPDATE prenatal_lab_screening_records SET isSynced = 1 WHERE id IN (:ids)")
    void markAsSynced(List<Integer> ids);

    @Query("SELECT * FROM prenatal_lab_screening_records WHERE newInsert = 1")
    List<PrenatalLabScreeningEntity> getNewInsertRecords();

    @Query("UPDATE prenatal_lab_screening_records SET newInsert = 0 WHERE id IN (:ids)")
    void markAsInserted(List<Integer> ids);
}