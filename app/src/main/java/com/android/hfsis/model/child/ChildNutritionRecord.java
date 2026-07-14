package com.android.hfsis.model.child;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "child_nutrition_records")
public class ChildNutritionRecord implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long profileId;
    public long userId;

    // ---------- Section 1: Client Identification ----------
    private String dateRegistration;
    private String familySerialNumber;
    private String childName;
    private String dateOfBirth;
    private String ageMonths;
    private String sex;
    private String motherName;
    private String address;

    // ---------- Section 2: Newborn Assessment ----------
    private String lengthAtBirth;
    private String weightAtBirth;
    private String birthWeightStatus;
    private String breastfeedingDate;
    private String placeOfDelivery;

    // ---------- Section 3: Iron Supplementation ----------
    private String iron1Month;
    private String iron2Months;
    private String iron3Months;
    private int ironCompleted;
    private String ironCompletedDate;

    // ---------- Section 4: Vitamin A Supplementation ----------
    private String vitaA6to11;
    private String vitaA200Y1D1; private String vitaA200Y1D2;
    private String vitaA200Y2D1; private String vitaA200Y2D2;
    private String vitaA200Y3D1; private String vitaA200Y3D2;
    private String vitaA200Y4D1; private String vitaA200Y4D2;

    // ---------- Section 5: MNP Supplementation ----------
    private String mnp6to11Provided; private String mnp6to11Completed; private String mnp6to11Remarks;
    private String mnp12to23Provided; private String mnp12to23Completed; private String mnp12to23Remarks;

    // ---------- Section 6: LNS-SQ Supplementation ----------
    private String lns6to11Provided; private String lns6to11Completed; private String lns6to11Remarks;
    private String lns12to23Provided; private String lns12to23Completed; private String lns12to23Remarks;

    // ---------- Section 7: MAM (SFP) ----------
    private int mamIdentified; private int mamEnrolled;  private int mamCured;
    private int mamNonCured;   private int mamDefaulted; private int mamDied;

    // ---------- Section 8: SAM (OTC) ----------
    private int samIdentified; private int samAdmitted;  private int samCured;
    private int samNonCured;   private int samDefaulted; private int samDied;

    // ---------- Section 9: Remarks ----------
    private String remarks;

    // --- Sync Tracking ---
    private boolean isSynced = false;

    private boolean newInsert = true;
    private long updatedAt = System.currentTimeMillis();

    // Required Constructor
    public ChildNutritionRecord() {}

    // ── Getters and Setters ──────────────────────────────────────────────────

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

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

    public String getLengthAtBirth() { return lengthAtBirth; }
    public void setLengthAtBirth(String lengthAtBirth) { this.lengthAtBirth = lengthAtBirth; }

    public String getWeightAtBirth() { return weightAtBirth; }
    public void setWeightAtBirth(String weightAtBirth) { this.weightAtBirth = weightAtBirth; }

    public String getBirthWeightStatus() { return birthWeightStatus; }
    public void setBirthWeightStatus(String birthWeightStatus) { this.birthWeightStatus = birthWeightStatus; }

    public String getBreastfeedingDate() { return breastfeedingDate; }
    public void setBreastfeedingDate(String breastfeedingDate) { this.breastfeedingDate = breastfeedingDate; }

    public String getPlaceOfDelivery() { return placeOfDelivery; }
    public void setPlaceOfDelivery(String placeOfDelivery) { this.placeOfDelivery = placeOfDelivery; }

    public String getIron1Month() { return iron1Month; }
    public void setIron1Month(String iron1Month) { this.iron1Month = iron1Month; }

    public String getIron2Months() { return iron2Months; }
    public void setIron2Months(String iron2Months) { this.iron2Months = iron2Months; }

    public String getIron3Months() { return iron3Months; }
    public void setIron3Months(String iron3Months) { this.iron3Months = iron3Months; }

    public int getIronCompleted() { return ironCompleted; }
    public void setIronCompleted(int ironCompleted) { this.ironCompleted = ironCompleted; }

    public String getIronCompletedDate() { return ironCompletedDate; }
    public void setIronCompletedDate(String ironCompletedDate) { this.ironCompletedDate = ironCompletedDate; }

    public String getVitaA6to11() { return vitaA6to11; }
    public void setVitaA6to11(String vitaA6to11) { this.vitaA6to11 = vitaA6to11; }

    public String getVitaA200Y1D1() { return vitaA200Y1D1; }
    public void setVitaA200Y1D1(String vitaA200Y1D1) { this.vitaA200Y1D1 = vitaA200Y1D1; }

    public String getVitaA200Y1D2() { return vitaA200Y1D2; }
    public void setVitaA200Y1D2(String vitaA200Y1D2) { this.vitaA200Y1D2 = vitaA200Y1D2; }

    public String getVitaA200Y2D1() { return vitaA200Y2D1; }
    public void setVitaA200Y2D1(String vitaA200Y2D1) { this.vitaA200Y2D1 = vitaA200Y2D1; }

    public String getVitaA200Y2D2() { return vitaA200Y2D2; }
    public void setVitaA200Y2D2(String vitaA200Y2D2) { this.vitaA200Y2D2 = vitaA200Y2D2; }

    public String getVitaA200Y3D1() { return vitaA200Y3D1; }
    public void setVitaA200Y3D1(String vitaA200Y3D1) { this.vitaA200Y3D1 = vitaA200Y3D1; }

    public String getVitaA200Y3D2() { return vitaA200Y3D2; }
    public void setVitaA200Y3D2(String vitaA200Y3D2) { this.vitaA200Y3D2 = vitaA200Y3D2; }

    public String getVitaA200Y4D1() { return vitaA200Y4D1; }
    public void setVitaA200Y4D1(String vitaA200Y4D1) { this.vitaA200Y4D1 = vitaA200Y4D1; }

    public String getVitaA200Y4D2() { return vitaA200Y4D2; }
    public void setVitaA200Y4D2(String vitaA200Y4D2) { this.vitaA200Y4D2 = vitaA200Y4D2; }

    public String getMnp6to11Provided() { return mnp6to11Provided; }
    public void setMnp6to11Provided(String mnp6to11Provided) { this.mnp6to11Provided = mnp6to11Provided; }

    public String getMnp6to11Completed() { return mnp6to11Completed; }
    public void setMnp6to11Completed(String mnp6to11Completed) { this.mnp6to11Completed = mnp6to11Completed; }

    public String getMnp6to11Remarks() { return mnp6to11Remarks; }
    public void setMnp6to11Remarks(String mnp6to11Remarks) { this.mnp6to11Remarks = mnp6to11Remarks; }

    public String getMnp12to23Provided() { return mnp12to23Provided; }
    public void setMnp12to23Provided(String mnp12to23Provided) { this.mnp12to23Provided = mnp12to23Provided; }

    public String getMnp12to23Completed() { return mnp12to23Completed; }
    public void setMnp12to23Completed(String mnp12to23Completed) { this.mnp12to23Completed = mnp12to23Completed; }

    public String getMnp12to23Remarks() { return mnp12to23Remarks; }
    public void setMnp12to23Remarks(String mnp12to23Remarks) { this.mnp12to23Remarks = mnp12to23Remarks; }

    public String getLns6to11Provided() { return lns6to11Provided; }
    public void setLns6to11Provided(String lns6to11Provided) { this.lns6to11Provided = lns6to11Provided; }

    public String getLns6to11Completed() { return lns6to11Completed; }
    public void setLns6to11Completed(String lns6to11Completed) { this.lns6to11Completed = lns6to11Completed; }

    public String getLns6to11Remarks() { return lns6to11Remarks; }
    public void setLns6to11Remarks(String lns6to11Remarks) { this.lns6to11Remarks = lns6to11Remarks; }

    public String getLns12to23Provided() { return lns12to23Provided; }
    public void setLns12to23Provided(String lns12to23Provided) { this.lns12to23Provided = lns12to23Provided; }

    public String getLns12to23Completed() { return lns12to23Completed; }
    public void setLns12to23Completed(String lns12to23Completed) { this.lns12to23Completed = lns12to23Completed; }

    public String getLns12to23Remarks() { return lns12to23Remarks; }
    public void setLns12to23Remarks(String lns12to23Remarks) { this.lns12to23Remarks = lns12to23Remarks; }

    public int getMamIdentified() { return mamIdentified; }
    public void setMamIdentified(int mamIdentified) { this.mamIdentified = mamIdentified; }

    public int getMamEnrolled() { return mamEnrolled; }
    public void setMamEnrolled(int mamEnrolled) { this.mamEnrolled = mamEnrolled; }

    public int getMamCured() { return mamCured; }
    public void setMamCured(int mamCured) { this.mamCured = mamCured; }

    public int getMamNonCured() { return mamNonCured; }
    public void setMamNonCured(int mamNonCured) { this.mamNonCured = mamNonCured; }

    public int getMamDefaulted() { return mamDefaulted; }
    public void setMamDefaulted(int mamDefaulted) { this.mamDefaulted = mamDefaulted; }

    public int getMamDied() { return mamDied; }
    public void setMamDied(int mamDied) { this.mamDied = mamDied; }

    public int getSamIdentified() { return samIdentified; }
    public void setSamIdentified(int samIdentified) { this.samIdentified = samIdentified; }

    public int getSamAdmitted() { return samAdmitted; }
    public void setSamAdmitted(int samAdmitted) { this.samAdmitted = samAdmitted; }

    public int getSamCured() { return samCured; }
    public void setSamCured(int samCured) { this.samCured = samCured; }

    public int getSamNonCured() { return samNonCured; }
    public void setSamNonCured(int samNonCured) { this.samNonCured = samNonCured; }

    public int getSamDefaulted() { return samDefaulted; }
    public void setSamDefaulted(int samDefaulted) { this.samDefaulted = samDefaulted; }

    public int getSamDied() { return samDied; }
    public void setSamDied(int samDied) { this.samDied = samDied; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { this.isSynced = synced; }

    public boolean isNewInsert() { return newInsert; }
    public void setNewInsert(boolean newInsert) { this.newInsert = newInsert; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}