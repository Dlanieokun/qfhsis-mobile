package com.android.hfsis.model.ncdpcs;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "eyes_screenings")
public class EyesScreeningsData {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private long id;

    @SerializedName("profile_id")
    private int profileId;

    @SerializedName("date_screening")
    private String dateScreening;

    @SerializedName("family_serial")
    private String familySerial;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("date_of_birth")
    private String dateOfBirth;

    @SerializedName("age")
    private String age;

    @SerializedName("age_group")
    private String ageGroup;

    @SerializedName("sex")
    private String sex;

    @SerializedName("screened")
    private int screened;

    @SerializedName("eye_disease_code")
    private String eyeDiseaseCode;

    @SerializedName("date_referred")
    private String dateReferred;

    @SerializedName("remarks")
    private String remarks;

    public EyesScreeningsData() {}

    // Getters and Setters...
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public String getDateScreening() { return dateScreening; }
    public void setDateScreening(String dateScreening) { this.dateScreening = dateScreening; }

    public String getFamilySerial() { return familySerial; }
    public void setFamilySerial(String familySerial) { this.familySerial = familySerial; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public int getScreened() { return screened; }
    public void setScreened(int screened) { this.screened = screened; }

    public String getEyeDiseaseCode() { return eyeDiseaseCode; }
    public void setEyeDiseaseCode(String eyeDiseaseCode) { this.eyeDiseaseCode = eyeDiseaseCode; }

    public String getDateReferred() { return dateReferred; }
    public void setDateReferred(String dateReferred) { this.dateReferred = dateReferred; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}