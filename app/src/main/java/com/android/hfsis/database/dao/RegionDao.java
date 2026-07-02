package com.android.hfsis.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.android.hfsis.model.address.Region;

import java.util.List;

@Dao
public interface RegionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRegion(Region region);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Region> regions);

    @Query("SELECT * FROM regions ORDER BY regDesc ASC")
    List<Region> getAllRegions();

    @Query("SELECT * FROM regions WHERE regCode = :regCode LIMIT 1")
    Region getRegionByCode(String regCode);

    @Query("DELETE FROM regions")
    void deleteAll();
}