package com.android.hfsis.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.android.hfsis.model.DropOutEntity;
import com.android.hfsis.model.FollowUpEntity;

import java.util.List;

@Dao
public interface DropOutDao {
    @Query("SELECT * FROM family_planning_drop_outs")
    List<DropOutEntity> getAll();


    @Query("SELECT * FROM family_planning_drop_outs WHERE recordId = :recId LIMIT 1")
    DropOutEntity getDropOutByRecordId(int recId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertDropOut(DropOutEntity dropOut);
}