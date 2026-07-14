package com.android.hfsis.database.maternal_care_record;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("SELECT * FROM prenatal_supplementation_records WHERE isSynced = 0")
    List<PrenatalSupplementationEntity> getUnsyncedRecords();

    @Query("UPDATE prenatal_supplementation_records SET isSynced = 1 WHERE id IN (:ids)")
    void markAsSynced(List<Integer> ids);

    @Query("SELECT * FROM prenatal_supplementation_records WHERE newInsert = 1")
    List<PrenatalSupplementationEntity> getNewInsertRecords();

    @Query("UPDATE prenatal_supplementation_records SET newInsert = 0 WHERE id IN (:ids)")
    void markAsInserted(List<Integer> ids);
}