package com.android.hfsis.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName; // Import required for GSON mapping

@Entity(tableName = "family_planning_follow_ups")
public class FollowUpEntity {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    @SerializedName("recordId")
    public int recordId;       // Link back to FamilyPlanningRecord card

    @SerializedName("profileId")
    public long profileId;     // Link back to HouseholdProfile profile

    @SerializedName("monthName")
    public String monthName;   // Jan, Feb, Mar, etc.

    @SerializedName("scheduledDate")
    public String scheduledDate;

    @SerializedName("actualDate")
    public String actualDate;
}