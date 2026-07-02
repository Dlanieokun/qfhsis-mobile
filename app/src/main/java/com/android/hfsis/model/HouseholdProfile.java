package com.android.hfsis.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "household_profiles")
public class HouseholdProfile {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // Household Structure Columns
    public String sitio;
    public String barangay;
    public String municipality;
    public String province;
    public String region;
    public String hhNumber;
    public String respondent;
    public String socioStatus;
    public String waterSource;
    public String toiletType;

    // Member Columns
    public String familyNumber;
    public String memberLastName;
    public String memberMiddleName;
    public String memberFirstName;
    public String relationship;
    public String sex;
    public String dob;
    public String philhealthId;
    public String philType;
    public String philCategory;

    // Health Assessment Checks
    public boolean hpn;
    public boolean dm;
    public boolean tb;

    // Reproductive & Social Data
    public boolean fpMethod;
    public String fpMethodUsed;
    public String education;
    public String religion;

    // Public empty constructor required by Room
    public HouseholdProfile() {}
}