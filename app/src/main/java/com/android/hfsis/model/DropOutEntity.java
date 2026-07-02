package com.android.hfsis.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName; // Import for explicit mapping conversions

@Entity(tableName = "family_planning_drop_outs")
public class DropOutEntity {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    @SerializedName("recordId")
    public int recordId;          // Reference ID map linking back to FamilyPlanningRecord

    @SerializedName("profileId")
    public long profileId;        // Reference long map linking back to HouseholdProfile

    @SerializedName("dropOutDate")
    public String dropOutDate;    // Formatted date string standard format

    @SerializedName("reasonCode")
    public String reasonCode;     // Coded values A through P

    @SerializedName("remarks")
    public String remarks;        // Open notes tracking text field
}