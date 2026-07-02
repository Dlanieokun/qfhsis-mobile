package com.android.hfsis.model.child;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "child_immunization_school_records")
public class ChildImmunizationSchoolRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    // Link back to the original selected Household Profile
    private long profileId = -1;

    // Demographics Tracking
    private String registrationDate;
    private String familySerialNumber;
    private String childName;
    private String dateOfBirth;
    private String ageYears;
    private String sex;           // "Male" or "Female"
    private String address;
    private String gradeLevel;    // "A", "B", "C", or "D"

    // School-Based Immunization (SBI) Vaccines
    private String tdDate;
    private String mrDate;
    private String hpv1SbiDate;

    // Community-Based Immunization (CBI) Vaccines
    private String hpv1CbiDate;
    private String hpv2CbiDate;

    // HPV Fully Immunized Female (FIF) Metrics
    private int hpvCompleted;     // 0 = No, 1 = Yes
    private String hpvCompletedDate;

    private String remarks;

    // Public empty constructor required by Room
    public ChildImmunizationSchoolRecord() {}

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getProfileId() { return profileId; }
    public void setProfileId(long profileId) { this.profileId = profileId; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public String getFamilySerialNumber() { return familySerialNumber; }
    public void setFamilySerialNumber(String familySerialNumber) { this.familySerialNumber = familySerialNumber; }

    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAgeYears() { return ageYears; }
    public void setAgeYears(String ageYears) { this.ageYears = ageYears; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }

    public String getTdDate() { return tdDate; }
    public void setTdDate(String tdDate) { this.tdDate = tdDate; }

    public String getMrDate() { return mrDate; }
    public void setMrDate(String mrDate) { this.mrDate = mrDate; }

    public String getHpv1SbiDate() { return hpv1SbiDate; }
    public void setHpv1SbiDate(String hpv1SbiDate) { this.hpv1SbiDate = hpv1SbiDate; }

    public String getHpv1CbiDate() { return hpv1CbiDate; }
    public void setHpv1CbiDate(String hpv1CbiDate) { this.hpv1CbiDate = hpv1CbiDate; }

    public String getHpv2CbiDate() { return hpv2CbiDate; }
    public void setHpv2CbiDate(String hpv2CbiDate) { this.hpv2CbiDate = hpv2CbiDate; }

    public int getHpvCompleted() { return hpvCompleted; }
    public void setHpvCompleted(int hpvCompleted) { this.hpvCompleted = hpvCompleted; }

    public String getHpvCompletedDate() { return hpvCompletedDate; }
    public void setHpvCompletedDate(String hpvCompletedDate) { this.hpvCompletedDate = hpvCompletedDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}