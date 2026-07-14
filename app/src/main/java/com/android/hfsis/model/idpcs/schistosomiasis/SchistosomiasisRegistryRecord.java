package com.android.hfsis.model.idpcs.schistosomiasis;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.android.hfsis.database.idpcs.schistosomiasis.SchistosomiasisTypeConverters;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "schistosomiasis_registry")
@TypeConverters({SchistosomiasisTypeConverters.class})
public class SchistosomiasisRegistryRecord {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private long id;

    @SerializedName("userId")
    private long userId;

    @SerializedName("profile_id")
    private String profileId;

    @SerializedName("date_of_registration")
    private String dateOfRegistration;

    @SerializedName("family_serial_number")
    private String familySerialNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("residency")
    private String residency;

    @SerializedName("date_of_birth")
    private String dateOfBirth;

    @SerializedName("age")
    private int age;

    @SerializedName("age_group")
    private String ageGroup;

    @SerializedName("sex")
    private String sex;

    @SerializedName("history_of_exposure")
    private String historyOfExposure;

    @SerializedName("screened")
    private String screened;

    @SerializedName("date_screened")
    private String dateScreened;

    @SerializedName("with_signs_symptoms")
    private String withSignsSymptoms;

    @SerializedName("signs_symptoms")
    private List<String> signsSymptoms = new ArrayList<>();

    @SerializedName("signs_symptoms_other_specify")
    private String signsSymptomsOtherSpecify;

    @SerializedName("clinical_first_treatment_given")
    private String clinicalFirstTreatmentGiven;

    @SerializedName("clinical_first_treatment_date")
    private String clinicalFirstTreatmentDate;

    @SerializedName("clinical_retreatment")
    private String clinicalRetreatment;

    @SerializedName("clinical_retreatment_date")
    private String clinicalRetreatmentDate;

    @SerializedName("clinical_cured")
    private String clinicalCured;

    @SerializedName("clinical_cured_date")
    private String clinicalCuredDate;

    @SerializedName("diagnostic_test")
    private String diagnosticTest;

    @SerializedName("date_of_diagnosis")
    private String dateOfDiagnosis;

    @SerializedName("diagnostic_result")
    private String diagnosticResult;

    @SerializedName("date_confirmed")
    private String dateConfirmed;

    @SerializedName("complicated")
    private String complicated;

    @SerializedName("confirmed_first_treatment_given")
    private String confirmedFirstTreatmentGiven;

    @SerializedName("confirmed_first_treatment_date")
    private String confirmedFirstTreatmentDate;

    @SerializedName("confirmed_retreatment")
    private String confirmedRetreatment;

    @SerializedName("confirmed_retreatment_date")
    private String confirmedRetreatmentDate;

    @SerializedName("confirmed_cured")
    private String confirmedCured;

    @SerializedName("confirmed_cured_date")
    private String confirmedCuredDate;

    @SerializedName("date_referred_to_hospital")
    private String dateReferredToHospital;

    @SerializedName("mda_given")
    private String mdaGiven;

    @SerializedName("mda_date_given")
    private String mdaDateGiven;

    @SerializedName("remarks")
    private String remarks;

    // --- Sync Tracking ---
    @SerializedName("isSynced")
    private boolean isSynced = false;

    @SerializedName("newInsert")
    private boolean newInsert = true;

    @SerializedName("updated_at")
    private long updatedAt = System.currentTimeMillis();

    public SchistosomiasisRegistryRecord() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
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

    public String getHistoryOfExposure() {
        return historyOfExposure;
    }

    public void setHistoryOfExposure(String historyOfExposure) {
        this.historyOfExposure = historyOfExposure;
    }

    public String getScreened() {
        return screened;
    }

    public void setScreened(String screened) {
        this.screened = screened;
    }

    public String getDateScreened() {
        return dateScreened;
    }

    public void setDateScreened(String dateScreened) {
        this.dateScreened = dateScreened;
    }

    public String getWithSignsSymptoms() {
        return withSignsSymptoms;
    }

    public void setWithSignsSymptoms(String withSignsSymptoms) {
        this.withSignsSymptoms = withSignsSymptoms;
    }

    public List<String> getSignsSymptoms() {
        return signsSymptoms;
    }

    public void setSignsSymptoms(List<String> signsSymptoms) {
        this.signsSymptoms = signsSymptoms;
    }

    public String getSignsSymptomsOtherSpecify() {
        return signsSymptomsOtherSpecify;
    }

    public void setSignsSymptomsOtherSpecify(String signsSymptomsOtherSpecify) {
        this.signsSymptomsOtherSpecify = signsSymptomsOtherSpecify;
    }

    public String getClinicalFirstTreatmentGiven() {
        return clinicalFirstTreatmentGiven;
    }

    public void setClinicalFirstTreatmentGiven(String clinicalFirstTreatmentGiven) {
        this.clinicalFirstTreatmentGiven = clinicalFirstTreatmentGiven;
    }

    public String getClinicalFirstTreatmentDate() {
        return clinicalFirstTreatmentDate;
    }

    public void setClinicalFirstTreatmentDate(String clinicalFirstTreatmentDate) {
        this.clinicalFirstTreatmentDate = clinicalFirstTreatmentDate;
    }

    public String getClinicalRetreatment() {
        return clinicalRetreatment;
    }

    public void setClinicalRetreatment(String clinicalRetreatment) {
        this.clinicalRetreatment = clinicalRetreatment;
    }

    public String getClinicalRetreatmentDate() {
        return clinicalRetreatmentDate;
    }

    public void setClinicalRetreatmentDate(String clinicalRetreatmentDate) {
        this.clinicalRetreatmentDate = clinicalRetreatmentDate;
    }

    public String getClinicalCured() {
        return clinicalCured;
    }

    public void setClinicalCured(String clinicalCured) {
        this.clinicalCured = clinicalCured;
    }

    public String getClinicalCuredDate() {
        return clinicalCuredDate;
    }

    public void setClinicalCuredDate(String clinicalCuredDate) {
        this.clinicalCuredDate = clinicalCuredDate;
    }

    public String getDiagnosticTest() {
        return diagnosticTest;
    }

    public void setDiagnosticTest(String diagnosticTest) {
        this.diagnosticTest = diagnosticTest;
    }

    public String getDateOfDiagnosis() {
        return dateOfDiagnosis;
    }

    public void setDateOfDiagnosis(String dateOfDiagnosis) {
        this.dateOfDiagnosis = dateOfDiagnosis;
    }

    public String getDiagnosticResult() {
        return diagnosticResult;
    }

    public void setDiagnosticResult(String diagnosticResult) {
        this.diagnosticResult = diagnosticResult;
    }

    public String getDateConfirmed() {
        return dateConfirmed;
    }

    public void setDateConfirmed(String dateConfirmed) {
        this.dateConfirmed = dateConfirmed;
    }

    public String getComplicated() {
        return complicated;
    }

    public void setComplicated(String complicated) {
        this.complicated = complicated;
    }

    public String getConfirmedFirstTreatmentGiven() {
        return confirmedFirstTreatmentGiven;
    }

    public void setConfirmedFirstTreatmentGiven(String confirmedFirstTreatmentGiven) {
        this.confirmedFirstTreatmentGiven = confirmedFirstTreatmentGiven;
    }

    public String getConfirmedFirstTreatmentDate() {
        return confirmedFirstTreatmentDate;
    }

    public void setConfirmedFirstTreatmentDate(String confirmedFirstTreatmentDate) {
        this.confirmedFirstTreatmentDate = confirmedFirstTreatmentDate;
    }

    public String getConfirmedRetreatment() {
        return confirmedRetreatment;
    }

    public void setConfirmedRetreatment(String confirmedRetreatment) {
        this.confirmedRetreatment = confirmedRetreatment;
    }

    public String getConfirmedRetreatmentDate() {
        return confirmedRetreatmentDate;
    }

    public void setConfirmedRetreatmentDate(String confirmedRetreatmentDate) {
        this.confirmedRetreatmentDate = confirmedRetreatmentDate;
    }

    public String getConfirmedCured() {
        return confirmedCured;
    }

    public void setConfirmedCured(String confirmedCured) {
        this.confirmedCured = confirmedCured;
    }

    public String getConfirmedCuredDate() {
        return confirmedCuredDate;
    }

    public void setConfirmedCuredDate(String confirmedCuredDate) {
        this.confirmedCuredDate = confirmedCuredDate;
    }

    public String getDateReferredToHospital() {
        return dateReferredToHospital;
    }

    public void setDateReferredToHospital(String dateReferredToHospital) {
        this.dateReferredToHospital = dateReferredToHospital;
    }

    public String getMdaGiven() {
        return mdaGiven;
    }

    public void setMdaGiven(String mdaGiven) {
        this.mdaGiven = mdaGiven;
    }

    public String getMdaDateGiven() {
        return mdaDateGiven;
    }

    public void setMdaDateGiven(String mdaDateGiven) {
        this.mdaDateGiven = mdaDateGiven;
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

    @Override
    public String toString() {
        return "SchistosomiasisRegistryRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", profileId='" + profileId + '\'' +
                ", dateOfRegistration='" + dateOfRegistration + '\'' +
                ", familySerialNumber='" + familySerialNumber + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", residency='" + residency + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", age=" + age +
                ", ageGroup='" + ageGroup + '\'' +
                ", sex='" + sex + '\'' +
                ", historyOfExposure='" + historyOfExposure + '\'' +
                ", screened='" + screened + '\'' +
                ", dateScreened='" + dateScreened + '\'' +
                ", withSignsSymptoms='" + withSignsSymptoms + '\'' +
                ", signsSymptoms=" + signsSymptoms +
                ", signsSymptomsOtherSpecify='" + signsSymptomsOtherSpecify + '\'' +
                ", clinicalFirstTreatmentGiven='" + clinicalFirstTreatmentGiven + '\'' +
                ", clinicalFirstTreatmentDate='" + clinicalFirstTreatmentDate + '\'' +
                ", clinicalRetreatment='" + clinicalRetreatment + '\'' +
                ", clinicalRetreatmentDate='" + clinicalRetreatmentDate + '\'' +
                ", clinicalCured='" + clinicalCured + '\'' +
                ", clinicalCuredDate='" + clinicalCuredDate + '\'' +
                ", diagnosticTest='" + diagnosticTest + '\'' +
                ", dateOfDiagnosis='" + dateOfDiagnosis + '\'' +
                ", diagnosticResult='" + diagnosticResult + '\'' +
                ", dateConfirmed='" + dateConfirmed + '\'' +
                ", complicated='" + complicated + '\'' +
                ", confirmedFirstTreatmentGiven='" + confirmedFirstTreatmentGiven + '\'' +
                ", confirmedFirstTreatmentDate='" + confirmedFirstTreatmentDate + '\'' +
                ", confirmedRetreatment='" + confirmedRetreatment + '\'' +
                ", confirmedRetreatmentDate='" + confirmedRetreatmentDate + '\'' +
                ", confirmedCured='" + confirmedCured + '\'' +
                ", confirmedCuredDate='" + confirmedCuredDate + '\'' +
                ", dateReferredToHospital='" + dateReferredToHospital + '\'' +
                ", mdaGiven='" + mdaGiven + '\'' +
                ", mdaDateGiven='" + mdaDateGiven + '\'' +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}