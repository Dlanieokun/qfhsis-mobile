package com.android.hfsis.model.environmental;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "environmental_health_records")
public class EnvironmentalHealthModel {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private long id;

    @SerializedName("householdHeadName")
    private String householdHeadName;

    // Section 1 - Water Source Booleans
    @SerializedName("waterLevelI")
    private boolean waterLevelI;

    @SerializedName("waterLevelII")
    private boolean waterLevelII;

    @SerializedName("waterLevelIII")
    private boolean waterLevelIII;

    @SerializedName("waterSourceOthers")
    private String waterSourceOthers;

    // Section 1 - Operational Integer Flags/Booleans
    @SerializedName("waterLocatedInsideDwelling")
    private boolean waterLocatedInsideDwelling;

    @SerializedName("waterAvailable12Hours")
    private boolean waterAvailable12Hours;

    @SerializedName("microbiologicalTestDate")
    private String microbiologicalTestDate;

    @SerializedName("microbiologicalTestResult")
    private int microbiologicalTestResult; // 1 = Positive, 0 = Negative, -1 = Unchecked

    @SerializedName("waterSafetyPlanOperational")
    private int waterSafetyPlanOperational; // 1 = Yes, 0 = No, -1 = Unchecked

    // Section 2 - Sanitation Properties
    @SerializedName("sanitationStatus")
    private String sanitationStatus; // "Functional Sanitary", "Unsanitary", "No Toilet"

    @SerializedName("unsanitaryToiletType")
    private int unsanitaryToiletType; // 0, 1, 2, 3

    @SerializedName("toiletShared")
    private int toiletShared; // 1 = Yes, 0 = No

    @SerializedName("basicSanitationFacility")
    private int basicSanitationFacility; // 1 = Yes, 0 = No

    @SerializedName("disposalDate")
    private String disposalDate;

    @SerializedName("disposalInSitu")
    private boolean disposalInSitu;

    @SerializedName("disposalOffSiteDesludged")
    private boolean disposalOffSiteDesludged;

    @SerializedName("disposalOffSiteSewer")
    private boolean disposalOffSiteSewer;

    @SerializedName("safelyManagedSanitationService")
    private int safelyManagedSanitationService; // 1 = Yes, 0 = No

    @SerializedName("safelyManagedDrinkingWater")
    private int safelyManagedDrinkingWater;     // 1 = Yes, 0 = No

    @SerializedName("remarks")
    private String remarks;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getHouseholdHeadName() { return householdHeadName; }
    public void setHouseholdHeadName(String householdHeadName) { this.householdHeadName = householdHeadName; }

    public boolean isWaterLevelI() { return waterLevelI; }
    public void setWaterLevelI(boolean waterLevelI) { this.waterLevelI = waterLevelI; }

    public boolean isWaterLevelII() { return waterLevelII; }
    public void setWaterLevelII(boolean waterLevelII) { this.waterLevelII = waterLevelII; }

    public boolean isWaterLevelIII() { return waterLevelIII; }
    public void setWaterLevelIII(boolean waterLevelIII) { this.waterLevelIII = waterLevelIII; }

    public String getWaterSourceOthers() { return waterSourceOthers; }
    public void setWaterSourceOthers(String waterSourceOthers) { this.waterSourceOthers = waterSourceOthers; }

    public boolean isWaterLocatedInsideDwelling() { return waterLocatedInsideDwelling; }
    public void setWaterLocatedInsideDwelling(boolean waterLocatedInsideDwelling) { this.waterLocatedInsideDwelling = waterLocatedInsideDwelling; }

    public boolean isWaterAvailable12Hours() { return waterAvailable12Hours; }
    public void setWaterAvailable12Hours(boolean waterAvailable12Hours) { this.waterAvailable12Hours = waterAvailable12Hours; }

    public String getMicrobiologicalTestDate() { return microbiologicalTestDate; }
    public void setMicrobiologicalTestDate(String microbiologicalTestDate) { this.microbiologicalTestDate = microbiologicalTestDate; }

    public int getMicrobiologicalTestResult() { return microbiologicalTestResult; }
    public void setMicrobiologicalTestResult(int microbiologicalTestResult) { this.microbiologicalTestResult = microbiologicalTestResult; }

    public int getWaterSafetyPlanOperational() { return waterSafetyPlanOperational; }
    public void setWaterSafetyPlanOperational(int waterSafetyPlanOperational) { this.waterSafetyPlanOperational = waterSafetyPlanOperational; }

    public String getSanitationStatus() { return sanitationStatus; }
    public void setSanitationStatus(String sanitationStatus) { this.sanitationStatus = sanitationStatus; }

    public int getUnsanitaryToiletType() { return unsanitaryToiletType; }
    public void setUnsanitaryToiletType(int unsanitaryToiletType) { this.unsanitaryToiletType = unsanitaryToiletType; }

    public int getToiletShared() { return toiletShared; }
    public void setToiletShared(int toiletShared) { this.toiletShared = toiletShared; }

    public int getBasicSanitationFacility() { return basicSanitationFacility; }
    public void setBasicSanitationFacility(int basicSanitationFacility) { this.basicSanitationFacility = basicSanitationFacility; }

    public String getDisposalDate() { return disposalDate; }
    public void setDisposalDate(String disposalDate) { this.disposalDate = disposalDate; }

    public boolean isDisposalInSitu() { return disposalInSitu; }
    public void setDisposalInSitu(boolean disposalInSitu) { this.disposalInSitu = disposalInSitu; }

    public boolean isDisposalOffSiteDesludged() { return disposalOffSiteDesludged; }
    public void setDisposalOffSiteDesludged(boolean disposalOffSiteDesludged) { this.disposalOffSiteDesludged = disposalOffSiteDesludged; }

    public boolean isDisposalOffSiteSewer() { return disposalOffSiteSewer; }
    public void setDisposalOffSiteSewer(boolean disposalOffSiteSewer) { this.disposalOffSiteSewer = disposalOffSiteSewer; }

    public int getSafelyManagedSanitationService() { return safelyManagedSanitationService; }
    public void setSafelyManagedSanitationService(int safelyManagedSanitationService) { this.safelyManagedSanitationService = safelyManagedSanitationService; }

    public int getSafelyManagedDrinkingWater() { return safelyManagedDrinkingWater; }
    public void setSafelyManagedDrinkingWater(int safelyManagedDrinkingWater) { this.safelyManagedDrinkingWater = safelyManagedDrinkingWater; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}