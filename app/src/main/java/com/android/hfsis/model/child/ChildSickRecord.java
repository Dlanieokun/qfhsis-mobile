package com.android.hfsis.model.child;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "child_sick_records")
public class ChildSickRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int userId;
    private long profileId;

    // Section 1 — Basic Information
    private String dateRegistration;
    private String familySerialNumber;
    private String childName;
    private String dateOfBirth;
    private String ageMonths;
    private String sex;
    private String motherName;
    private String address;

    // Section 2 — Vitamin A Supplementation
    private String vitaminADateGiven;
    private boolean vitaminA100IU;
    private boolean vitaminA200IU;

    // Section 3 — Diagnosis & Management
    private boolean diagnosisMeasles;
    private boolean diagnosisPersistentDiarrhea;
    private String diarrheaDateGiven;
    private boolean orsOnly;
    private boolean orsAndZinc;

    private String pneumoniaDateGiven;
    private boolean amoxicillinDrops;
    private boolean amoxicillinClavulanate;
    private boolean cefuroxime;
    private boolean pneumoniaOthers;
    private String pneumoniaOthersSpec;

    // Section 4 — Remarks
    private String remarks;

    // --- Sync Tracking ---
    private boolean isSynced = false;

    private boolean newInsert = true;
    private long updatedAt = System.currentTimeMillis();

    // Public empty constructor required by Room
    public ChildSickRecord() {}

    // ── Getters and Setters ──────────────────────────────────────────

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    // ---> ADDED GETTER AND SETTER FOR PROFILE ID <---
    public long getProfileId() { return profileId; }
    public void setProfileId(long profileId) { this.profileId = profileId; }

    public String getDateRegistration() { return dateRegistration; }
    public void setDateRegistration(String dateRegistration) { this.dateRegistration = dateRegistration; }

    public String getFamilySerialNumber() { return familySerialNumber; }
    public void setFamilySerialNumber(String familySerialNumber) { this.familySerialNumber = familySerialNumber; }

    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAgeMonths() { return ageMonths; }
    public void setAgeMonths(String ageMonths) { this.ageMonths = ageMonths; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getVitaminADateGiven() { return vitaminADateGiven; }
    public void setVitaminADateGiven(String vitaminADateGiven) { this.vitaminADateGiven = vitaminADateGiven; }

    public boolean isVitaminA100IU() { return vitaminA100IU; }
    public void setVitaminA100IU(boolean vitaminA100IU) { this.vitaminA100IU = vitaminA100IU; }

    public boolean isVitaminA200IU() { return vitaminA200IU; }
    public void setVitaminA200IU(boolean vitaminA200IU) { this.vitaminA200IU = vitaminA200IU; }

    public boolean isDiagnosisMeasles() { return diagnosisMeasles; }
    public void setDiagnosisMeasles(boolean diagnosisMeasles) { this.diagnosisMeasles = diagnosisMeasles; }

    public boolean isDiagnosisPersistentDiarrhea() { return diagnosisPersistentDiarrhea; }
    public void setDiagnosisPersistentDiarrhea(boolean diagnosisPersistentDiarrhea) { this.diagnosisPersistentDiarrhea = diagnosisPersistentDiarrhea; }

    public String getDiarrheaDateGiven() { return diarrheaDateGiven; }
    public void setDiarrheaDateGiven(String diarrheaDateGiven) { this.diarrheaDateGiven = diarrheaDateGiven; }

    public boolean isOrsOnly() { return orsOnly; }
    public void setOrsOnly(boolean orsOnly) { this.orsOnly = orsOnly; }

    public boolean isOrsAndZinc() { return orsAndZinc; }
    public void setOrsAndZinc(boolean orsAndZinc) { this.orsAndZinc = orsAndZinc; }

    public String getPneumoniaDateGiven() { return pneumoniaDateGiven; }
    public void setPneumoniaDateGiven(String pneumoniaDateGiven) { this.pneumoniaDateGiven = pneumoniaDateGiven; }

    public boolean isAmoxicillinDrops() { return amoxicillinDrops; }
    public void setAmoxicillinDrops(boolean amoxicillinDrops) { this.amoxicillinDrops = amoxicillinDrops; }

    public boolean isAmoxicillinClavulanate() { return amoxicillinClavulanate; }
    public void setAmoxicillinClavulanate(boolean amoxicillinClavulanate) { this.amoxicillinClavulanate = amoxicillinClavulanate; }

    public boolean isCefuroxime() { return cefuroxime; }
    public void setCefuroxime(boolean cefuroxime) { this.cefuroxime = cefuroxime; }

    public boolean isPneumoniaOthers() { return pneumoniaOthers; }
    public void setPneumoniaOthers(boolean pneumoniaOthers) { this.pneumoniaOthers = pneumoniaOthers; }

    public String getPneumoniaOthersSpec() { return pneumoniaOthersSpec; }
    public void setPneumoniaOthersSpec(String pneumoniaOthersSpec) { this.pneumoniaOthersSpec = pneumoniaOthersSpec; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { this.isSynced = synced; }

    public boolean isNewInsert() { return newInsert; }
    public void setNewInsert(boolean newInsert) { this.newInsert = newInsert; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}