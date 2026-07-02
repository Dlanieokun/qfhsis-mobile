package com.android.hfsis.model.idpcs.sthpc;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "sth_registry_records")
public class SoilTransmittedHelminthiasisRegistryRecord {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private int id = 0; // 0 represents a new record in Room

    @SerializedName("date_of_registration")
    private String dateOfRegistration;

    @SerializedName("family_serial_number")
    private String familySerialNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("residency")
    private String residency; // "1" Resident, "0" Non-Resident

    @SerializedName("date_of_birth")
    private String dateOfBirth;

    @SerializedName("age")
    private int age;

    @SerializedName("age_classification")
    private String ageClassification;

    @SerializedName("sex")
    private String sex;

    // Screening
    @SerializedName("screened")
    private String screened; // "1" Yes, "0" No

    @SerializedName("date_of_screening")
    private String dateOfScreening;

    // Screening Result
    @SerializedName("screening_result")
    private String screeningResult; // "0" Negative, "1" Suspected, "2" Positive

    @SerializedName("date_of_result")
    private String dateOfResult;

    // Treatment Given
    @SerializedName("treatment_given")
    private String treatmentGiven; // "0" None, "1" Albendazole, "2" Mebendazole

    @SerializedName("treatment_date_given")
    private String treatmentDateGiven;

    // Given Deworming Tablet (MDA)
    @SerializedName("january_mda_date")
    private String januaryMdaDate;

    @SerializedName("january_mda_modality")
    private String januaryMdaModality; // "1" School-based, "2" Community-based

    @SerializedName("july_mda_date")
    private String julyMdaDate;

    @SerializedName("july_mda_modality")
    private String julyMdaModality; // "1" School-based, "2" Community-based

    @SerializedName("remarks")
    private String remarks;

    public SoilTransmittedHelminthiasisRegistryRecord() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(String dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
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

    public String getResidency() {
        return residency;
    }

    public void setResidency(String residency) {
        this.residency = residency;
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

    public String getAgeClassification() {
        return ageClassification;
    }

    public void setAgeClassification(String ageClassification) {
        this.ageClassification = ageClassification;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getScreened() {
        return screened;
    }

    public void setScreened(String screened) {
        this.screened = screened;
    }

    public String getDateOfScreening() {
        return dateOfScreening;
    }

    public void setDateOfScreening(String dateOfScreening) {
        this.dateOfScreening = dateOfScreening;
    }

    public String getScreeningResult() {
        return screeningResult;
    }

    public void setScreeningResult(String screeningResult) {
        this.screeningResult = screeningResult;
    }

    public String getDateOfResult() {
        return dateOfResult;
    }

    public void setDateOfResult(String dateOfResult) {
        this.dateOfResult = dateOfResult;
    }

    public String getTreatmentGiven() {
        return treatmentGiven;
    }

    public void setTreatmentGiven(String treatmentGiven) {
        this.treatmentGiven = treatmentGiven;
    }

    public String getTreatmentDateGiven() {
        return treatmentDateGiven;
    }

    public void setTreatmentDateGiven(String treatmentDateGiven) {
        this.treatmentDateGiven = treatmentDateGiven;
    }

    public String getJanuaryMdaDate() {
        return januaryMdaDate;
    }

    public void setJanuaryMdaDate(String januaryMdaDate) {
        this.januaryMdaDate = januaryMdaDate;
    }

    public String getJanuaryMdaModality() {
        return januaryMdaModality;
    }

    public void setJanuaryMdaModality(String januaryMdaModality) {
        this.januaryMdaModality = januaryMdaModality;
    }

    public String getJulyMdaDate() {
        return julyMdaDate;
    }

    public void setJulyMdaDate(String julyMdaDate) {
        this.julyMdaDate = julyMdaDate;
    }

    public String getJulyMdaModality() {
        return julyMdaModality;
    }

    public void setJulyMdaModality(String julyMdaModality) {
        this.julyMdaModality = julyMdaModality;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "SoilTransmittedHelminthiasisRegistryRecord{" +
                "id=" + id +
                ", dateOfRegistration='" + dateOfRegistration + '\'' +
                ", familySerialNumber='" + familySerialNumber + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", residency='" + residency + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", age=" + age +
                ", ageClassification='" + ageClassification + '\'' +
                ", sex='" + sex + '\'' +
                ", screened='" + screened + '\'' +
                ", dateOfScreening='" + dateOfScreening + '\'' +
                ", screeningResult='" + screeningResult + '\'' +
                ", dateOfResult='" + dateOfResult + '\'' +
                ", treatmentGiven='" + treatmentGiven + '\'' +
                ", treatmentDateGiven='" + treatmentDateGiven + '\'' +
                ", januaryMdaDate='" + januaryMdaDate + '\'' +
                ", januaryMdaModality='" + januaryMdaModality + '\'' +
                ", julyMdaDate='" + julyMdaDate + '\'' +
                ", julyMdaModality='" + julyMdaModality + '\'' +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}