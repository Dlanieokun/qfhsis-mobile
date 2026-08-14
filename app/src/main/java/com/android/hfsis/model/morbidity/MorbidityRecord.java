package com.android.hfsis.model.morbidity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "morbidity_records")
public class MorbidityRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String householdId;
    private String barangay;
    private String municipality;
    private String province;
    private String reportMonth;
    private String reportYear;

    // Disease name and ICD code
    private String diseaseName;
    private String icdCode;

    // Age group: 0-6 days
    private int age0to6daysMale;
    private int age0to6daysFemale;

    // Age group: 7-28 days
    private int age7to28daysMale;
    private int age7to28daysFemale;

    // Age group: 29 days to 11 months
    private int age29daysto11moMale;
    private int age29daysto11moFemale;

    // Age group: 1-4 years old
    private int age1to4yrsMale;
    private int age1to4yrsFemale;

    // Age group: 5-9 years old
    private int age5to9yrsMale;
    private int age5to9yrsFemale;

    // Age group: 10-14 years old
    private int age10to14yrsMale;
    private int age10to14yrsFemale;

    // Age group: 15-19 years old
    private int age15to19yrsMale;
    private int age15to19yrsFemale;

    // Age group: 20-24 years old
    private int age20to24yrsMale;
    private int age20to24yrsFemale;

    // Age group: 25-29 years old
    private int age25to29yrsMale;
    private int age25to29yrsFemale;

    // Age group: 30-34 years old
    private int age30to34yrsMale;
    private int age30to34yrsFemale;

    // Age group: 35-39 years old
    private int age35to39yrsMale;
    private int age35to39yrsFemale;

    // Age group: 40-44 years old
    private int age40to44yrsMale;
    private int age40to44yrsFemale;

    // Age group: 45-49 years old
    private int age45to49yrsMale;
    private int age45to49yrsFemale;

    // Age group: 50-54 years old
    private int age50to54yrsMale;
    private int age50to54yrsFemale;

    // Age group: 55-59 years old
    private int age55to59yrsMale;
    private int age55to59yrsFemale;

    // Age group: 60 years and above
    private int age60plusMale;
    private int age60plusFemale;

    private boolean isSynced;
    private long createdAt;
    private long updatedAt;

    public MorbidityRecord() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isSynced = false;
    }

    // Computed totals (not stored, calculated on the fly)
    public int getTotalMale() {
        return age0to6daysMale + age7to28daysMale + age29daysto11moMale +
                age1to4yrsMale + age5to9yrsMale + age10to14yrsMale +
                age15to19yrsMale + age20to24yrsMale + age25to29yrsMale +
                age30to34yrsMale + age35to39yrsMale + age40to44yrsMale +
                age45to49yrsMale + age50to54yrsMale + age55to59yrsMale +
                age60plusMale;
    }

    public int getTotalFemale() {
        return age0to6daysFemale + age7to28daysFemale + age29daysto11moFemale +
                age1to4yrsFemale + age5to9yrsFemale + age10to14yrsFemale +
                age15to19yrsFemale + age20to24yrsFemale + age25to29yrsFemale +
                age30to34yrsFemale + age35to39yrsFemale + age40to44yrsFemale +
                age45to49yrsFemale + age50to54yrsFemale + age55to59yrsFemale +
                age60plusFemale;
    }

    public int getGrandTotal() {
        return getTotalMale() + getTotalFemale();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getHouseholdId() { return householdId; }
    public void setHouseholdId(String householdId) { this.householdId = householdId; }

    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }

    public String getMunicipality() { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getReportMonth() { return reportMonth; }
    public void setReportMonth(String reportMonth) { this.reportMonth = reportMonth; }

    public String getReportYear() { return reportYear; }
    public void setReportYear(String reportYear) { this.reportYear = reportYear; }

    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }

    public String getIcdCode() { return icdCode; }
    public void setIcdCode(String icdCode) { this.icdCode = icdCode; }

    public int getAge0to6daysMale() { return age0to6daysMale; }
    public void setAge0to6daysMale(int v) { this.age0to6daysMale = v; }

    public int getAge0to6daysFemale() { return age0to6daysFemale; }
    public void setAge0to6daysFemale(int v) { this.age0to6daysFemale = v; }

    public int getAge7to28daysMale() { return age7to28daysMale; }
    public void setAge7to28daysMale(int v) { this.age7to28daysMale = v; }

    public int getAge7to28daysFemale() { return age7to28daysFemale; }
    public void setAge7to28daysFemale(int v) { this.age7to28daysFemale = v; }

    public int getAge29daysto11moMale() { return age29daysto11moMale; }
    public void setAge29daysto11moMale(int v) { this.age29daysto11moMale = v; }

    public int getAge29daysto11moFemale() { return age29daysto11moFemale; }
    public void setAge29daysto11moFemale(int v) { this.age29daysto11moFemale = v; }

    public int getAge1to4yrsMale() { return age1to4yrsMale; }
    public void setAge1to4yrsMale(int v) { this.age1to4yrsMale = v; }

    public int getAge1to4yrsFemale() { return age1to4yrsFemale; }
    public void setAge1to4yrsFemale(int v) { this.age1to4yrsFemale = v; }

    public int getAge5to9yrsMale() { return age5to9yrsMale; }
    public void setAge5to9yrsMale(int v) { this.age5to9yrsMale = v; }

    public int getAge5to9yrsFemale() { return age5to9yrsFemale; }
    public void setAge5to9yrsFemale(int v) { this.age5to9yrsFemale = v; }

    public int getAge10to14yrsMale() { return age10to14yrsMale; }
    public void setAge10to14yrsMale(int v) { this.age10to14yrsMale = v; }

    public int getAge10to14yrsFemale() { return age10to14yrsFemale; }
    public void setAge10to14yrsFemale(int v) { this.age10to14yrsFemale = v; }

    public int getAge15to19yrsMale() { return age15to19yrsMale; }
    public void setAge15to19yrsMale(int v) { this.age15to19yrsMale = v; }

    public int getAge15to19yrsFemale() { return age15to19yrsFemale; }
    public void setAge15to19yrsFemale(int v) { this.age15to19yrsFemale = v; }

    public int getAge20to24yrsMale() { return age20to24yrsMale; }
    public void setAge20to24yrsMale(int v) { this.age20to24yrsMale = v; }

    public int getAge20to24yrsFemale() { return age20to24yrsFemale; }
    public void setAge20to24yrsFemale(int v) { this.age20to24yrsFemale = v; }

    public int getAge25to29yrsMale() { return age25to29yrsMale; }
    public void setAge25to29yrsMale(int v) { this.age25to29yrsMale = v; }

    public int getAge25to29yrsFemale() { return age25to29yrsFemale; }
    public void setAge25to29yrsFemale(int v) { this.age25to29yrsFemale = v; }

    public int getAge30to34yrsMale() { return age30to34yrsMale; }
    public void setAge30to34yrsMale(int v) { this.age30to34yrsMale = v; }

    public int getAge30to34yrsFemale() { return age30to34yrsFemale; }
    public void setAge30to34yrsFemale(int v) { this.age30to34yrsFemale = v; }

    public int getAge35to39yrsMale() { return age35to39yrsMale; }
    public void setAge35to39yrsMale(int v) { this.age35to39yrsMale = v; }

    public int getAge35to39yrsFemale() { return age35to39yrsFemale; }
    public void setAge35to39yrsFemale(int v) { this.age35to39yrsFemale = v; }

    public int getAge40to44yrsMale() { return age40to44yrsMale; }
    public void setAge40to44yrsMale(int v) { this.age40to44yrsMale = v; }

    public int getAge40to44yrsFemale() { return age40to44yrsFemale; }
    public void setAge40to44yrsFemale(int v) { this.age40to44yrsFemale = v; }

    public int getAge45to49yrsMale() { return age45to49yrsMale; }
    public void setAge45to49yrsMale(int v) { this.age45to49yrsMale = v; }

    public int getAge45to49yrsFemale() { return age45to49yrsFemale; }
    public void setAge45to49yrsFemale(int v) { this.age45to49yrsFemale = v; }

    public int getAge50to54yrsMale() { return age50to54yrsMale; }
    public void setAge50to54yrsMale(int v) { this.age50to54yrsMale = v; }

    public int getAge50to54yrsFemale() { return age50to54yrsFemale; }
    public void setAge50to54yrsFemale(int v) { this.age50to54yrsFemale = v; }

    public int getAge55to59yrsMale() { return age55to59yrsMale; }
    public void setAge55to59yrsMale(int v) { this.age55to59yrsMale = v; }

    public int getAge55to59yrsFemale() { return age55to59yrsFemale; }
    public void setAge55to59yrsFemale(int v) { this.age55to59yrsFemale = v; }

    public int getAge60plusMale() { return age60plusMale; }
    public void setAge60plusMale(int v) { this.age60plusMale = v; }

    public int getAge60plusFemale() { return age60plusFemale; }
    public void setAge60plusFemale(int v) { this.age60plusFemale = v; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}