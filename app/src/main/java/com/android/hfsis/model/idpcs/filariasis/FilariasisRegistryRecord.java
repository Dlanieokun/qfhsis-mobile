package com.android.hfsis.model.idpcs.filariasis;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "filariasis_registry_table")
public class FilariasisRegistryRecord {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private long id;

    @SerializedName("userId")
    private long userId;

    @SerializedName("profile_id")
    private int profileId;

    @SerializedName("date_of_registration")
    private String dateOfRegistration;

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

    @SerializedName("age_group")
    private String ageGroup;

    @SerializedName("sex")
    private String sex;

    @SerializedName("nbe_performed")
    private boolean nbePerformed;

    @SerializedName("rdt_performed")
    private boolean rdtPerformed;

    @SerializedName("date_nbe_rdt")
    private String dateNbeRdt;

    @SerializedName("blood_test_result")
    private String bloodTestResult;

    @SerializedName("lymphedema_examined_first_time")
    private String lymphedemaExaminedFirstTime;

    @SerializedName("has_lymphedema")
    private boolean hasLymphedema;

    @SerializedName("elephantiasis_examined_first_time")
    private String elephantiasisExaminedFirstTime;

    @SerializedName("has_elephantiasis")
    private boolean hasElephantiasis;

    @SerializedName("hydrocele_examined_first_time")
    private String hydroceleExaminedFirstTime;

    @SerializedName("has_hydrocele")
    private boolean hasHydrocele;

    @SerializedName("albendazole_date_given")
    private String albendazoleDateGiven;

    @SerializedName("dec_date_given")
    private String decDateGiven;

    @SerializedName("ivermectin_date_given")
    private String ivermectinDateGiven;

    @SerializedName("remarks")
    private String remarks;

    // --- Sync Tracking ---
    @SerializedName("isSynced")
    private boolean isSynced = false;

    @SerializedName("newInsert")
    private boolean newInsert = true;

    @SerializedName("updated_at")
    private long updatedAt = System.currentTimeMillis();

    public FilariasisRegistryRecord() {}

    // Getters and Setters...
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    // ---> ADDED GETTER AND SETTER FOR PROFILE ID <---
    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public String getDateOfRegistration() { return dateOfRegistration; }
    public void setDateOfRegistration(String dateOfRegistration) { this.dateOfRegistration = dateOfRegistration; }

    public String getFamilySerialNumber() { return familySerialNumber; }
    public void setFamilySerialNumber(String familySerialNumber) { this.familySerialNumber = familySerialNumber; }

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

    public boolean isNbePerformed() { return nbePerformed; }
    public void setNbePerformed(boolean nbePerformed) { this.nbePerformed = nbePerformed; }

    public boolean isRdtPerformed() { return rdtPerformed; }
    public void setRdtPerformed(boolean rdtPerformed) { this.rdtPerformed = rdtPerformed; }

    public String getDateNbeRdt() { return dateNbeRdt; }
    public void setDateNbeRdt(String dateNbeRdt) { this.dateNbeRdt = dateNbeRdt; }

    public String getBloodTestResult() { return bloodTestResult; }
    public void setBloodTestResult(String bloodTestResult) { this.bloodTestResult = bloodTestResult; }

    public String getLymphedemaExaminedFirstTime() { return lymphedemaExaminedFirstTime; }
    public void setLymphedemaExaminedFirstTime(String lymphedemaExaminedFirstTime) { this.lymphedemaExaminedFirstTime = lymphedemaExaminedFirstTime; }

    public boolean isHasLymphedema() { return hasLymphedema; }
    public void setHasLymphedema(boolean hasLymphedema) { this.hasLymphedema = hasLymphedema; }

    public String getElephantiasisExaminedFirstTime() { return elephantiasisExaminedFirstTime; }
    public void setElephantiasisExaminedFirstTime(String elephantiasisExaminedFirstTime) { this.elephantiasisExaminedFirstTime = elephantiasisExaminedFirstTime; }

    public boolean isHasElephantiasis() { return hasElephantiasis; }
    public void setHasElephantiasis(boolean hasElephantiasis) { this.hasElephantiasis = hasElephantiasis; }

    public String getHydroceleExaminedFirstTime() { return hydroceleExaminedFirstTime; }
    public void setHydroceleExaminedFirstTime(String hydroceleExaminedFirstTime) { this.hydroceleExaminedFirstTime = hydroceleExaminedFirstTime; }

    public boolean isHasHydrocele() { return hasHydrocele; }
    public void setHasHydrocele(boolean hasHydrocele) { this.hasHydrocele = hasHydrocele; }

    public String getAlbendazoleDateGiven() { return albendazoleDateGiven; }
    public void setAlbendazoleDateGiven(String albendazoleDateGiven) { this.albendazoleDateGiven = albendazoleDateGiven; }

    public String getDecDateGiven() { return decDateGiven; }
    public void setDecDateGiven(String decDateGiven) { this.decDateGiven = decDateGiven; }

    public String getIvermectinDateGiven() { return ivermectinDateGiven; }
    public void setIvermectinDateGiven(String ivermectinDateGiven) { this.ivermectinDateGiven = ivermectinDateGiven; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { this.isSynced = synced; }

    public boolean isNewInsert() { return newInsert; }
    public void setNewInsert(boolean newInsert) { this.newInsert = newInsert; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}