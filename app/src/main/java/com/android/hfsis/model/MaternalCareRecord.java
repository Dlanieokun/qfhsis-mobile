package com.android.hfsis.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName; // Required for mapping

@Entity(tableName = "maternal_care_records")
public class MaternalCareRecord {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    @SerializedName("profileId")
    public int profileId;

    @SerializedName("registrationDate")
    public String registrationDate;

    @SerializedName("familySerialNumber")
    public String familySerialNumber;

    @SerializedName("patientName")
    public String patientName;

    @SerializedName("homeAddress")
    public String homeAddress;

    @SerializedName("age")
    public int age;

    @SerializedName("ageGroup")
    public String ageGroup;

    @SerializedName("birthDate")
    public String birthDate;

    // Maps the Android 'lmpDate' to match the Laravel migration 'ImpDate' column typo
    @SerializedName("ImpDate")
    public String lmpDate;

    @SerializedName("gravidaPara")
    public String gravidaPara;

    @SerializedName("eddDate")
    public String eddDate;

    @SerializedName("weightKg")
    public double weightKg;

    @SerializedName("heightCm")
    public double heightCm;

    @SerializedName("bmiValue")
    public String bmiValue;

    @SerializedName("bmiStatus")
    public String bmiStatus;

    public MaternalCareRecord() {}
}