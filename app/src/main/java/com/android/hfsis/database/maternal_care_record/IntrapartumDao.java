package com.android.hfsis.database.maternal_care_record;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.android.hfsis.model.maternal_care_record.IntrapartumEntity;
import com.android.hfsis.model.maternal_care_record.PrenatalLabScreeningEntity;

import java.util.List;

@Dao
public interface IntrapartumDao {

    @Query("SELECT * FROM intrapartum_records")
    List<IntrapartumEntity> getAll();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertIntrapartum(IntrapartumEntity record);

    @Update
    void updateIntrapartum(IntrapartumEntity record);

    @Delete
    void deleteIntrapartum(IntrapartumEntity record);

    @Query("SELECT * FROM intrapartum_records WHERE maternalRecordId = :maternalId LIMIT 1")
    IntrapartumEntity getRecordByMaternalId(int maternalId);
}