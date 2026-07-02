package com.android.hfsis.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.android.hfsis.model.address.Province;

import java.util.List;

@Dao
public interface ProvinceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProvince(Province province);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Province> provinces);

    @Query("SELECT * FROM provinces ORDER BY provDesc ASC")
    List<Province> getAllProvinces();

    @Query("SELECT * FROM provinces WHERE regCode = :regCode ORDER BY provDesc ASC")
    List<Province> getProvincesByRegion(String regCode);

    @Query("SELECT * FROM provinces WHERE provCode = :provCode LIMIT 1")
    Province getProvinceByCode(String provCode);

    @Query("DELETE FROM provinces")
    void deleteAll();
}