package com.android.hfsis.database.maternal_care_record;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy; // Import this!
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.maternal_care_record.IntrapartumEntity;
import com.android.hfsis.model.maternal_care_record.PostpartumEntity;

import java.util.List;

@Dao
public interface PostpartumDao {
    @Query("SELECT * FROM postpartum_records")
    List<PostpartumEntity> getAll();

    // Use REPLACE to update local record if ID already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PostpartumEntity entity);

    @Update
    void update(PostpartumEntity entity);

    @Query("SELECT * FROM postpartum_records WHERE maternalRecordId = :maternalId LIMIT 1")
    PostpartumEntity getByMaternalId(int maternalId);

    @Query("SELECT * FROM postpartum_records WHERE isSynced = 0")
    List<PostpartumEntity> getUnsyncedRecords();

    @Query("UPDATE postpartum_records SET isSynced = 1, newInsert = 0 WHERE id IN (:ids)")
    void markAsSynced(List<Integer> ids);

    @Query("SELECT * FROM postpartum_records WHERE newInsert = 1")
    List<PostpartumEntity> getNewInsertRecords();

    @Query("UPDATE postpartum_records SET newInsert = 0 WHERE id IN (:ids)")
    void markAsInserted(List<Integer> ids);
}