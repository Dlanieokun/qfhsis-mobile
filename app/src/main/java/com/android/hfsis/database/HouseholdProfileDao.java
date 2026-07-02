package com.android.hfsis.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.HouseholdProfile;

import java.util.List;

@Dao
public interface HouseholdProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProfile(HouseholdProfile profile);

    @Query("SELECT * FROM household_profiles ORDER BY id DESC")
    List<HouseholdProfile> getAllProfiles();

    @Query("SELECT * FROM household_profiles")
    List<HouseholdProfile> getAll();

    // Combines database name components into a single formatted string for dropdown view listings
    @Query("SELECT (memberLastName || ', ' || memberFirstName || ' ' || COALESCE(memberMiddleName, '')) AS calculatedFullName FROM household_profiles WHERE memberLastName IS NOT NULL AND memberFirstName IS NOT NULL ORDER BY calculatedFullName ASC")
    List<String> getAllHouseholdNames();

    // Query tool layer matching selected autocomplete full text string structure against raw database row data tables
    @Query("SELECT * FROM household_profiles WHERE (memberLastName || ', ' || memberFirstName || ' ' || COALESCE(memberMiddleName, '')) = :fullName LIMIT 1")
    HouseholdProfile getProfileByCalculatedName(String fullName);

    @Query("SELECT * FROM household_profiles WHERE id = :profileId LIMIT 1")
    HouseholdProfile getProfileById(long profileId);

    @Update
    void update(HouseholdProfile profile);

    @Update
    void updateProfile(HouseholdProfile profile);

    @Delete
    void delete(HouseholdProfile profile);
}