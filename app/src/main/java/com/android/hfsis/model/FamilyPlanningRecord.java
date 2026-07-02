package com.android.hfsis.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName; // Required import

@Entity(tableName = "family_planning_records")
public class FamilyPlanningRecord {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    // Matches the exact camelCase naming scheme from your Laravel migration file
    @SerializedName("profileId")
    public long profileId;

    @SerializedName("registrationDate")
    public String registrationDate;

    @SerializedName("familySerialNumber")
    public String familySerialNumber;

    @SerializedName("address")
    public String address;

    @SerializedName("age")
    public int age;

    @SerializedName("birthDate")
    public String birthDate;

    @SerializedName("ageGroupCategory")
    public String ageGroupCategory;

    @SerializedName("clientType")
    public String clientType;

    @SerializedName("commoditySource")
    public String commoditySource;

    @SerializedName("previousMethod")
    public String previousMethod;

    public FamilyPlanningRecord() {}
}