package com.android.hfsis.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.android.hfsis.model.address.Barangay;

import java.util.List;

@Dao
public interface BarangayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBarangay(Barangay barangay);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Barangay> barangays);

    @Query("SELECT * FROM barangays ORDER BY brgyDesc ASC")
    List<Barangay> getAllBarangays();

    @Query("SELECT * FROM barangays WHERE citymunCode = :citymunCode ORDER BY brgyDesc ASC")
    List<Barangay> getBarangaysByMunicipality(String citymunCode);

    @Query("SELECT * FROM barangays WHERE brgyCode = :brgyCode LIMIT 1")
    Barangay getBarangayByCode(String brgyCode);

    @Query("DELETE FROM barangays")
    void deleteAll();
}