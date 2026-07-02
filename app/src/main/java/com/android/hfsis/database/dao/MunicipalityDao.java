package com.android.hfsis.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.android.hfsis.model.address.Municipality;

import java.util.List;

@Dao
public interface MunicipalityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMunicipality(Municipality municipality);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Municipality> municipalities);

    @Query("SELECT * FROM municipalities ORDER BY citymunDesc ASC")
    List<Municipality> getAllMunicipalities();

    @Query("SELECT * FROM municipalities WHERE provCode = :provCode ORDER BY citymunDesc ASC")
    List<Municipality> getMunicipalitiesByProvince(String provCode);

    @Query("SELECT * FROM municipalities WHERE citymunCode = :citymunCode LIMIT 1")
    Municipality getMunicipalityByCode(String citymunCode);

    @Query("DELETE FROM municipalities")
    void deleteAll();
}