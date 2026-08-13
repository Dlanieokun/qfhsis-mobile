package com.android.hfsis.model.vital_statistics;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "maternal_deaths")
public class MaternalDeathRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String dateOfRegistration;      // Date (mm/dd/yy format)
    public String fullName;                 // LastName, FirstName, MI
    public String completeAddress;          // Address with Province/HUC/ICC
    public int age;                         // Age in years
    public String ageGroup;                 // A, B, C (10-14, 15-19, 20-49)
    public String placeOfOccurrence;        // A (Resident), B (Non-Resident)
    public String causeOfDeath;             // A (Direct), B (Indirect)
    public String remarks;                  // Additional remarks
    public boolean synced;                  // Sync status
    public long syncTimestamp;              // Last sync time

    public MaternalDeathRecord() {
        this.synced = false;
        this.syncTimestamp = 0;
    }

    public MaternalDeathRecord(String dateOfRegistration, String fullName, String completeAddress,
                               int age, String ageGroup, String placeOfOccurrence,
                               String causeOfDeath, String remarks) {
        this.dateOfRegistration = dateOfRegistration;
        this.fullName = fullName;
        this.completeAddress = completeAddress;
        this.age = age;
        this.ageGroup = ageGroup;
        this.placeOfOccurrence = placeOfOccurrence;
        this.causeOfDeath = causeOfDeath;
        this.remarks = remarks;
        this.synced = false;
        this.syncTimestamp = 0;
    }
}