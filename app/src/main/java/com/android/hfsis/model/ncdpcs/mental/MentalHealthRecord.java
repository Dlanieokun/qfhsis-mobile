package com.android.hfsis.model.ncdpcs.mental;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a single row of the "Target Client List for Mental Health" (TCL_MH)
 * register. Field names and meaning mirror the columns of the source spreadsheet.
 */
@Entity(tableName = "mental_health_records")
public class MentalHealthRecord {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("recordNo")
    private int recordNo;

    @SerializedName("userId")
    private int userId;

    @SerializedName("profile_id")
    private int profileId;

    @SerializedName("dateOfAssessment")
    private String dateOfAssessment;

    @SerializedName("familySerialNumber")
    private String familySerialNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("age")
    private int age;

    @SerializedName("ageGroup")
    private String ageGroup;

    @SerializedName("sex")
    private String sex;

    @SerializedName("screenedMhgap")
    private boolean screenedMhgap;

    // --- Sync Tracking ---
    @SerializedName("isSynced")
    private boolean isSynced = false;

    @SerializedName("newInsert")
    private boolean newInsert = true;

    @SerializedName("updated_at")
    private long updatedAt = System.currentTimeMillis();

    public MentalHealthRecord() {
    }

    public MentalHealthRecord(int recordNo, String dateOfAssessment, String familySerialNumber,
                              String name, String address, String dateOfBirth, int age,
                              String ageGroup, String sex, boolean screenedMhgap) {
        this.recordNo = recordNo;
        this.dateOfAssessment = dateOfAssessment;
        this.familySerialNumber = familySerialNumber;
        this.name = name;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.ageGroup = ageGroup;
        this.sex = sex;
        this.screenedMhgap = screenedMhgap;
    }

    public int getRecordNo() {
        return recordNo;
    }

    public void setRecordNo(int recordNo) {
        this.recordNo = recordNo;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDateOfAssessment() {
        return dateOfAssessment;
    }

    public void setDateOfAssessment(String dateOfAssessment) {
        this.dateOfAssessment = dateOfAssessment;
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

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public boolean isScreenedMhgap() {
        return screenedMhgap;
    }

    public void setScreenedMhgap(boolean screenedMhgap) {
        this.screenedMhgap = screenedMhgap;
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

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    @Override
    public String toString() {
        return "MentalHealthRecord{" +
                "recordNo=" + recordNo +
                ", dateOfAssessment='" + dateOfAssessment + '\'' +
                ", familySerialNumber='" + familySerialNumber + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", age=" + age +
                ", ageGroup='" + ageGroup + '\'' +
                ", sex='" + sex + '\'' +
                ", screenedMhgap=" + screenedMhgap +
                '}';
    }
}