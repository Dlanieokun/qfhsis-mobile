package com.android.hfsis.model.vital_statistics;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "infant_deaths")
public class InfantDeathRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long profileId;                  // FK → HouseholdProfile.id

    public String dateOfRegistration;      // Date (mm/dd/yy format)
    public String fullName;                 // LastName, FirstName, MI
    public String completeAddress;          // Address with Province/HUC/ICC
    public int age;                         // Age in years/months
    public String sex;                      // M (Male), F (Female)
    public String remarks;                  // Additional remarks
    public boolean synced;                  // Sync status
    public long syncTimestamp;              // Last sync time

    public InfantDeathRecord() {
        this.synced = false;
        this.syncTimestamp = 0;
    }

    public InfantDeathRecord(String dateOfRegistration, String fullName, String completeAddress,
                             int age, String sex, String remarks, long profileId) {
        this.dateOfRegistration = dateOfRegistration;
        this.fullName = fullName;
        this.completeAddress = completeAddress;
        this.age = age;
        this.sex = sex;
        this.remarks = remarks;
        this.profileId = profileId;
        this.synced = false;
        this.syncTimestamp = 0;
    }

    public int getId() {
        return id;
    }
}