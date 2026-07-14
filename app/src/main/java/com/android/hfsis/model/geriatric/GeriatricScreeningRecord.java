package com.android.hfsis.model.geriatric;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a single row of the "Target Client List for Geriatric Screening
 * and Senior Citizen Immunization" table.
 */
@Entity(tableName = "geriatric_screening_records")
public class GeriatricScreeningRecord {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("record_no")
    private int recordNo;

    @SerializedName("profile_id")
    private int profileId;

    @SerializedName("date_of_screening")
    private String dateOfScreening;

    @SerializedName("userId")
    private int userId;

    @SerializedName("family_serial_number")
    private String familySerialNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("date_of_birth")
    private String dateOfBirth;

    @SerializedName("age")
    private int age;

    @SerializedName("sex")
    private String sex;

    @SerializedName("results")
    private String results;

    @SerializedName("care_plan_provided")
    private boolean carePlanProvided;

    @SerializedName("ppv_received_at60")
    private boolean ppvReceivedAt60;

    @SerializedName("ppv_date_given")
    private String ppvDateGiven;

    @SerializedName("influenza_date_given")
    private String influenzaDateGiven;

    @SerializedName("remarks")
    private String remarks;

    // --- Sync Tracking ---
    @SerializedName("isSynced")
    private boolean isSynced = false;

    @SerializedName("newInsert")
    private boolean newInsert = true;

    @SerializedName("updated_at")
    private long updatedAt = System.currentTimeMillis();

    // Getters and Setters
    public int getRecordNo() {
        return recordNo;
    }

    public void setRecordNo(int recordNo) {
        this.recordNo = recordNo;
    }

    // ---> ADDED GETTER AND SETTER FOR PROFILE ID <---
    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDateOfScreening() {
        return dateOfScreening;
    }

    public void setDateOfScreening(String dateOfScreening) {
        this.dateOfScreening = dateOfScreening;
    }

    public String getFamilySerialNumber() {
        return familySerialNumber;
    }

    public void setFamilySerialNumber(String familySerialNumber) {
        this.familySerialNumber = familySerialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public boolean isCarePlanProvided() {
        return carePlanProvided;
    }

    public void setCarePlanProvided(boolean carePlanProvided) {
        this.carePlanProvided = carePlanProvided;
    }

    public boolean isPpvReceivedAt60() {
        return ppvReceivedAt60;
    }

    public void setPpvReceivedAt60(boolean ppvReceivedAt60) {
        this.ppvReceivedAt60 = ppvReceivedAt60;
    }

    public String getPpvDateGiven() {
        return ppvDateGiven;
    }

    public void setPpvDateGiven(String ppvDateGiven) {
        this.ppvDateGiven = ppvDateGiven;
    }

    public String getInfluenzaDateGiven() {
        return influenzaDateGiven;
    }

    public void setInfluenzaDateGiven(String influenzaDateGiven) {
        this.influenzaDateGiven = influenzaDateGiven;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        this.isSynced = synced;
    }

    public boolean isNewInsert() {
        return newInsert;
    }

    public void setNewInsert(boolean newInsert) {
        this.newInsert = newInsert;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}