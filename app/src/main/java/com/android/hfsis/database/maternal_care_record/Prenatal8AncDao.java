package com.android.hfsis.database.maternal_care_record;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.MaternalCareRecord;
import com.android.hfsis.model.maternal_care_record.Prenatal8AncEntity;

import java.util.List;

@Dao
public interface Prenatal8AncDao {
    @Query("SELECT * FROM prenatal_8anc_records")
    List<Prenatal8AncEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert8AncRecord(Prenatal8AncEntity record);

    @Update
    void update8AncRecord(Prenatal8AncEntity record);

    // Queries a single record entry row based on the parent Maternal Care Log ID matching
    @Query("SELECT * FROM prenatal_8anc_records WHERE maternalRecordId = :maternalId LIMIT 1")
    Prenatal8AncEntity getRecordByMaternalId(int maternalId);

    @Query("SELECT * FROM prenatal_8anc_records WHERE isSynced = 0")
    List<Prenatal8AncEntity> getUnsyncedRecords();

    @Query("UPDATE prenatal_8anc_records SET isSynced = 1 WHERE id IN (:ids)")
    void markAsSynced(List<Integer> ids);

    @Query("SELECT * FROM prenatal_8anc_records WHERE newInsert = 1")
    List<Prenatal8AncEntity> getNewInsertRecords();

    @Query("UPDATE prenatal_8anc_records SET newInsert = 0 WHERE id IN (:ids)")
    void markAsInserted(List<Integer> ids);
}