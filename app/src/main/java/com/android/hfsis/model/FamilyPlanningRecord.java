package com.android.hfsis.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "family_planning_records")
public class FamilyPlanningRecord {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    @SerializedName("userId")
    public long userId;

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

    @SerializedName("methodUsed")
    public String methodUsed;

    @SerializedName("commoditySource")
    public String commoditySource;

    @SerializedName("previousMethod")
    public String previousMethod;

    // --- Sync Tracking ---
    @SerializedName("isSynced")
    public boolean isSynced = false;

    @SerializedName("newInsert")
    public boolean newInsert = true;

    @SerializedName("updated_at")
    public long updatedAt = System.currentTimeMillis();

    public FamilyPlanningRecord() {}
}