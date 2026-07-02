package com.android.hfsis.model.ohc;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "oral_health_care")
public class OralHealthCareEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // Add the foreign key reference field here
    @SerializedName("profile_id")
    @ColumnInfo(name = "profile_id")
    public int profileId;

    @SerializedName("date_of_visit")
    @ColumnInfo(name = "date_of_visit")
    public String dateOfVisit;

    @SerializedName("family_serial")
    @ColumnInfo(name = "family_serial")
    public String familySerial;

    public String name;
    public String address;

    @SerializedName("date_of_birth")
    @ColumnInfo(name = "date_of_birth")
    public String dateOfBirth;

    @SerializedName("age_months")
    @ColumnInfo(name = "age_months")
    public String ageMonths;

    public String sex;

    @SerializedName("rpoc0_oral_screening")
    @ColumnInfo(name = "rpoc0_oral_screening")
    public boolean rpoc0OralScreening;

    @SerializedName("rpoc0_risk_assessment")
    @ColumnInfo(name = "rpoc0_risk_assessment")
    public boolean rpoc0RiskAssessment;

    @SerializedName("rpoc0_oral_hygiene")
    @ColumnInfo(name = "rpoc0_oral_hygiene")
    public boolean rpoc0OralHygiene;

    @SerializedName("rpoc0_counseling")
    @ColumnInfo(name = "rpoc0_counseling")
    public boolean rpoc0Counseling;

    @SerializedName("rpoc0_fluoride_varnish")
    @ColumnInfo(name = "rpoc0_fluoride_varnish")
    public boolean rpoc0FluorideVarnish;

    @SerializedName("complete_rpoc0")
    @ColumnInfo(name = "complete_rpoc0")
    public int completeRpoc0;

    @SerializedName("age_years")
    @ColumnInfo(name = "age_years")
    public String ageYears;

    @SerializedName("age_group1st")
    @ColumnInfo(name = "age_group1st")
    public String ageGroup1st;

    @SerializedName("age_group2nd")
    @ColumnInfo(name = "age_group2nd")
    public String ageGroup2nd;

    @SerializedName("oral_screening1st")
    @ColumnInfo(name = "oral_screening1st")
    public String oralScreening1st;

    @SerializedName("oral_screening2nd")
    @ColumnInfo(name = "oral_screening2nd")
    public String oralScreening2nd;

    @SerializedName("risk_assessment1st")
    @ColumnInfo(name = "risk_assessment1st")
    public String riskAssessment1st;

    @SerializedName("risk_assessment2nd")
    @ColumnInfo(name = "risk_assessment2nd")
    public String riskAssessment2nd;

    @SerializedName("oral_prophylaxis1st")
    @ColumnInfo(name = "oral_prophylaxis1st")
    public String oralProphylaxis1st;

    @SerializedName("oral_prophylaxis2nd")
    @ColumnInfo(name = "oral_prophylaxis2nd")
    public String oralProphylaxis2nd;

    @SerializedName("fluoride_varnish1st")
    @ColumnInfo(name = "fluoride_varnish1st")
    public String fluorideVarnish1st;

    @SerializedName("fluoride_varnish2nd")
    @ColumnInfo(name = "fluoride_varnish2nd")
    public String fluorideVarnish2nd;

    @SerializedName("counseling1st")
    @ColumnInfo(name = "counseling1st")
    public String counseling1st;

    @SerializedName("counseling2nd")
    @ColumnInfo(name = "counseling2nd")
    public String counseling2nd;

    @SerializedName("complete_rpoc1st")
    @ColumnInfo(name = "complete_rpoc1st")
    public int completeRpoc1st;

    @SerializedName("complete_rpoc2nd")
    @ColumnInfo(name = "complete_rpoc2nd")
    public int completeRpoc2nd;

    @SerializedName("service_location1st")
    @ColumnInfo(name = "service_location1st")
    public String serviceLocation1st;

    @SerializedName("service_location2nd")
    @ColumnInfo(name = "service_location2nd")
    public String serviceLocation2nd;

    public String remarks;
}