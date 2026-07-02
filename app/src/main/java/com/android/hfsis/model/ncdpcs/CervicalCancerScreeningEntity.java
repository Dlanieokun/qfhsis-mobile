package com.android.hfsis.model.ncdpcs;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "cervical_cancer_screenings")
public class CervicalCancerScreeningEntity {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private long id;

    @ColumnInfo(name = "profile_id")
    @SerializedName("profile_id")
    private int profileId;

    @ColumnInfo(name = "date_assessment")
    @SerializedName("date_assessment")
    private String dateAssessment;

    @ColumnInfo(name = "family_serial")
    @SerializedName("family_serial")
    private String familySerial;

    @ColumnInfo(name = "client_name")
    @SerializedName("client_name") // Crucial: matches the migration table structure
    private String name;

    @ColumnInfo(name = "address")
    @SerializedName("address")
    private String address;

    @ColumnInfo(name = "date_of_birth")
    @SerializedName("date_of_birth")
    private String dateOfBirth;

    @ColumnInfo(name = "age")
    @SerializedName("age")
    private String age;

    @ColumnInfo(name = "cervical_screening_done")
    @SerializedName("cervical_screening_done")
    private int cervicalScreeningDone;

    @ColumnInfo(name = "cervical_result")
    @SerializedName("cervical_result")
    private int cervicalResult;

    @ColumnInfo(name = "cervical_linked_to_care")
    @SerializedName("cervical_linked_to_care")
    private int cervicalLinkedToCare;

    @ColumnInfo(name = "breast_risk_assessment")
    @SerializedName("breast_risk_assessment")
    private int breastRiskAssessment;

    @ColumnInfo(name = "breast_age_risk_class")
    @SerializedName("breast_age_risk_class")
    private String breastAgeRiskClass;

    @ColumnInfo(name = "breast_exam_type")
    @SerializedName("breast_exam_type")
    private String breastExamType;

    @ColumnInfo(name = "breast_result")
    @SerializedName("breast_result")
    private int breastResult;

    @ColumnInfo(name = "breast_linked_to_care")
    @SerializedName("breast_linked_to_care")
    private int breastLinkedToCare;

    @ColumnInfo(name = "remarks")
    @SerializedName("remarks")
    private String remarks;

    // Getters and Setters...
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public String getDateAssessment() { return dateAssessment; }
    public void setDateAssessment(String dateAssessment) { this.dateAssessment = dateAssessment; }

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

    public int getCervicalScreeningDone() { return cervicalScreeningDone; }
    public void setCervicalScreeningDone(int cervicalScreeningDone) { this.cervicalScreeningDone = cervicalScreeningDone; }

    public int getCervicalResult() { return cervicalResult; }
    public void setCervicalResult(int cervicalResult) { this.cervicalResult = cervicalResult; }

    public int getCervicalLinkedToCare() { return cervicalLinkedToCare; }
    public void setCervicalLinkedToCare(int cervicalLinkedToCare) { this.cervicalLinkedToCare = cervicalLinkedToCare; }

    public int getBreastRiskAssessment() { return breastRiskAssessment; }
    public void setBreastRiskAssessment(int breastRiskAssessment) { this.breastRiskAssessment = breastRiskAssessment; }

    public String getBreastAgeRiskClass() { return breastAgeRiskClass; }
    public void setBreastAgeRiskClass(String breastAgeRiskClass) { this.breastAgeRiskClass = breastAgeRiskClass; }

    public String getBreastExamType() { return breastExamType; }
    public void setBreastExamType(String breastExamType) { this.breastExamType = breastExamType; }

    public int getBreastResult() { return breastResult; }
    public void setBreastResult(int breastResult) { this.breastResult = breastResult; }

    public int getBreastLinkedToCare() { return breastLinkedToCare; }
    public void setBreastLinkedToCare(int breastLinkedToCare) { this.breastLinkedToCare = breastLinkedToCare; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}