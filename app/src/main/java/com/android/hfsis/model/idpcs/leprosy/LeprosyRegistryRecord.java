package com.android.hfsis.model.idpcs.leprosy;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "leprosy_registry")
public class LeprosyRegistryRecord {@PrimaryKey(autoGenerate = true)
@SerializedName("id")
private long id;

    @SerializedName("date_of_registration")
    private String dateOfRegistration;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("date_of_birth")
    private String dateOfBirth;

    @SerializedName("age")
    private int age;

    @SerializedName("age_group")
    private String ageGroup;

    @SerializedName("sex")
    private String sex;

    @SerializedName("confirmed_case")
    private String confirmedCase;

    @SerializedName("date_of_diagnosis")
    private String dateOfDiagnosis;

    @SerializedName("case_history")
    private String caseHistory;

    @SerializedName("previous_facility")
    private String previousFacility;

    @SerializedName("clinical_classification")
    private String clinicalClassification;

    @SerializedName("treatment_start_date")
    private String treatmentStartDate;

    @SerializedName("months_treated_prior")
    private String monthsTreatedPrior;

    @SerializedName("reclassified")
    private String reclassified;

    @SerializedName("date_of_reclassification")
    private String dateOfReclassification;

    @SerializedName("updated_classification")
    private String updatedClassification;

    @SerializedName("treatment_outcome")
    private String treatmentOutcome;

    @SerializedName("completed_fixed_mdt")
    private String completedFixedMdt;

    @SerializedName("fixed_mdt_completed_date")
    private String fixedMdtCompletedDate;

    @SerializedName("beyond_fixed_mdt")
    private String beyondFixedMdt;

    @SerializedName("beyond_fixed_mdt_completed_date")
    private String beyondFixedMdtCompletedDate;

    @SerializedName("grade2_disability")
    private String grade2Disability;

    @SerializedName("remarks")
    private String remarks;

    public LeprosyRegistryRecord() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDateOfRegistration() { return dateOfRegistration; }
    public void setDateOfRegistration(String dateOfRegistration) { this.dateOfRegistration = dateOfRegistration; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getConfirmedCase() { return confirmedCase; }
    public void setConfirmedCase(String confirmedCase) { this.confirmedCase = confirmedCase; }

    public String getDateOfDiagnosis() { return dateOfDiagnosis; }
    public void setDateOfDiagnosis(String dateOfDiagnosis) { this.dateOfDiagnosis = dateOfDiagnosis; }

    public String getCaseHistory() { return caseHistory; }
    public void setCaseHistory(String caseHistory) { this.caseHistory = caseHistory; }

    public String getPreviousFacility() { return previousFacility; }
    public void setPreviousFacility(String previousFacility) { this.previousFacility = previousFacility; }

    public String getClinicalClassification() { return clinicalClassification; }
    public void setClinicalClassification(String clinicalClassification) { this.clinicalClassification = clinicalClassification; }

    public String getTreatmentStartDate() { return treatmentStartDate; }
    public void setTreatmentStartDate(String treatmentStartDate) { this.treatmentStartDate = treatmentStartDate; }

    public String getMonthsTreatedPrior() { return monthsTreatedPrior; }
    public void setMonthsTreatedPrior(String monthsTreatedPrior) { this.monthsTreatedPrior = monthsTreatedPrior; }

    public String getReclassified() { return reclassified; }
    public void setReclassified(String reclassified) { this.reclassified = reclassified; }

    public String getDateOfReclassification() { return dateOfReclassification; }
    public void setDateOfReclassification(String dateOfReclassification) { this.dateOfReclassification = dateOfReclassification; }

    public String getUpdatedClassification() { return updatedClassification; }
    public void setUpdatedClassification(String updatedClassification) { this.updatedClassification = updatedClassification; }

    public String getTreatmentOutcome() { return treatmentOutcome; }
    public void setTreatmentOutcome(String treatmentOutcome) { this.treatmentOutcome = treatmentOutcome; }

    public String getCompletedFixedMdt() { return completedFixedMdt; }
    public void setCompletedFixedMdt(String completedFixedMdt) { this.completedFixedMdt = completedFixedMdt; }

    public String getFixedMdtCompletedDate() { return fixedMdtCompletedDate; }
    public void setFixedMdtCompletedDate(String fixedMdtCompletedDate) { this.fixedMdtCompletedDate = fixedMdtCompletedDate; }

    public String getBeyondFixedMdt() { return beyondFixedMdt; }
    public void setBeyondFixedMdt(String beyondFixedMdt) { this.beyondFixedMdt = beyondFixedMdt; }

    public String getBeyondFixedMdtCompletedDate() { return beyondFixedMdtCompletedDate; }
    public void setBeyondFixedMdtCompletedDate(String beyondFixedMdtCompletedDate) { this.beyondFixedMdtCompletedDate = beyondFixedMdtCompletedDate; }

    public String getGrade2Disability() { return grade2Disability; }
    public void setGrade2Disability(String grade2Disability) { this.grade2Disability = grade2Disability; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}