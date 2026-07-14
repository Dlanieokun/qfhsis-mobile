package com.android.hfsis.model.child;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "child_immunization_records")
public class ChildImmunizationRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    // Foreign link linking record back to selected Household Member Profile
    private long profileId = -1;

    public long userId;

    // Demographics
    private String registrationDate;
    private String familySerialNumber;
    private String childName;
    private String dateOfBirth;
    private String ageMonths;
    private String sex; // Stores "Male" or "Female"
    private String motherName;
    private String address;

    // CPAB
    private boolean td2Mother;
    private boolean td3To5Mother;

    // BCG
    private String bcgWithin24hAge;
    private String bcgWithin24hDate;
    private String bcgLateAge;
    private String bcgLateDate;

    // Hepatitis B
    private String hepaBWithin24hAge;
    private String hepaBWithin24hDate;
    private String hepaBLateAge;
    private String hepaBLateDate;

    // DPT-HiB-HepB
    private String dpt1Age;
    private String dpt1Date;
    private String dpt2Age;
    private String dpt2Date;
    private String dpt3Age;
    private String dpt3Date;

    // OPV
    private String opv1Age;
    private String opv1Date;
    private String opv2Age;
    private String opv2Date;
    private String opv3Age;
    private String opv3Date;

    // IPV (Inactivated Polio Vaccine)
    private String ipv1Age;
    private String ipv1Date;
    private String ipv2Age;  // <-- ADDED FIELD
    private String ipv2Date; // <-- ADDED FIELD

    // PCV
    private String pcv1Age;
    private String pcv1Date;
    private String pcv2Age;
    private String pcv2Date;
    private String pcv3Age;
    private String pcv3Date;

    // MMR
    private String mmr1Age;
    private String mmr1Date;
    private String mmr2Age;
    private String mmr2Date;

    // FIC
    private boolean ficBcg;
    private boolean ficDpt3;
    private boolean ficOpv3;
    private boolean ficMmr2;
    private String ficDate;

    // CIC
    private boolean cicBcg;
    private boolean cicDpt3;
    private boolean cicOpv3;
    private boolean cicMmr2;
    private String cicDate;

    private String remarks;

    // --- Sync Tracking ---
    private boolean isSynced = false;

    private boolean newInsert = true;
    private long updatedAt = System.currentTimeMillis();

    // Constructor
    public ChildImmunizationRecord() {}

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getProfileId() { return profileId; }
    public void setProfileId(long profileId) { this.profileId = profileId; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

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

    public boolean isTd2Mother() { return td2Mother; }
    public void setTd2Mother(boolean td2Mother) { this.td2Mother = td2Mother; }

    public boolean isTd3To5Mother() { return td3To5Mother; }
    public void setTd3To5Mother(boolean td3To5Mother) { this.td3To5Mother = td3To5Mother; }

    public String getBcgWithin24hAge() { return bcgWithin24hAge; }
    public void setBcgWithin24hAge(String bcgWithin24hAge) { this.bcgWithin24hAge = bcgWithin24hAge; }

    public String getBcgWithin24hDate() { return bcgWithin24hDate; }
    public void setBcgWithin24hDate(String bcgWithin24hDate) { this.bcgWithin24hDate = bcgWithin24hDate; }

    public String getBcgLateAge() { return bcgLateAge; }
    public void setBcgLateAge(String bcgLateAge) { this.bcgLateAge = bcgLateAge; }

    public String getBcgLateDate() { return bcgLateDate; }
    public void setBcgLateDate(String bcgLateDate) { this.bcgLateDate = bcgLateDate; }

    public String getHepaBWithin24hAge() { return hepaBWithin24hAge; }
    public void setHepaBWithin24hAge(String hepaBWithin24hAge) { this.hepaBWithin24hAge = hepaBWithin24hAge; }

    public String getHepaBWithin24hDate() { return hepaBWithin24hDate; }
    public void setHepaBWithin24hDate(String hepaBWithin24hDate) { this.hepaBWithin24hDate = hepaBWithin24hDate; }

    public String getHepaBLateAge() { return hepaBLateAge; }
    public void setHepaBLateAge(String hepaBLateAge) { this.hepaBLateAge = hepaBLateAge; }

    public String getHepaBLateDate() { return hepaBLateDate; }
    public void setHepaBLateDate(String hepaBLateDate) { this.hepaBLateDate = hepaBLateDate; }

    public String getDpt1Age() { return dpt1Age; }
    public void setDpt1Age(String dpt1Age) { this.dpt1Age = dpt1Age; }

    public String getDpt1Date() { return dpt1Date; }
    public void setDpt1Date(String dpt1Date) { this.dpt1Date = dpt1Date; }

    public String getDpt2Age() { return dpt2Age; }
    public void setDpt2Age(String dpt2Age) { this.dpt2Age = dpt2Age; }

    public String getDpt2Date() { return dpt2Date; }
    public void setDpt2Date(String dpt2Date) { this.dpt2Date = dpt2Date; }

    public String getDpt3Age() { return dpt3Age; }
    public void setDpt3Age(String dpt3Age) { this.dpt3Age = dpt3Age; }

    public String getDpt3Date() { return dpt3Date; }
    public void setDpt3Date(String dpt3Date) { this.dpt3Date = dpt3Date; }

    public String getOpv1Age() { return opv1Age; }
    public void setOpv1Age(String opv1Age) { this.opv1Age = opv1Age; }

    public String getOpv1Date() { return opv1Date; }
    public void setOpv1Date(String opv1Date) { this.opv1Date = opv1Date; }

    public String getOpv2Age() { return opv2Age; }
    public void setOpv2Age(String opv2Age) { this.opv2Age = opv2Age; }

    public String getOpv2Date() { return opv2Date; }
    public void setOpv2Date(String opv2Date) { this.opv2Date = opv2Date; }

    public String getOpv3Age() { return opv3Age; }
    public void setOpv3Age(String opv3Age) { this.opv3Age = opv3Age; }

    public String getOpv3Date() { return opv3Date; }
    public void setOpv3Date(String opv3Date) { this.opv3Date = opv3Date; }

    public String getIpv1Age() { return ipv1Age; }
    public void setIpv1Age(String ipv1Age) { this.ipv1Age = ipv1Age; }

    public String getIpv1Date() { return ipv1Date; }
    public void setIpv1Date(String ipv1Date) { this.ipv1Date = ipv1Date; }

    // IPV2 Getters and Setters <-- ADDED METHODS
    public String getIpv2Age() { return ipv2Age; }
    public void setIpv2Age(String ipv2Age) { this.ipv2Age = ipv2Age; }

    public String getIpv2Date() { return ipv2Date; }
    public void setIpv2Date(String ipv2Date) { this.ipv2Date = ipv2Date; }

    public String getPcv1Age() { return pcv1Age; }
    public void setPcv1Age(String pcv1Age) { this.pcv1Age = pcv1Age; }

    public String getPcv1Date() { return pcv1Date; }
    public void setPcv1Date(String pcv1Date) { this.pcv1Date = pcv1Date; }

    public String getPcv2Age() { return pcv2Age; }
    public void setPcv2Age(String pcv2Age) { this.pcv2Age = pcv2Age; }

    public String getPcv2Date() { return pcv2Date; }
    public void setPcv2Date(String pcv2Date) { this.pcv2Date = pcv2Date; }

    public String getPcv3Age() { return pcv3Age; }
    public void setPcv3Age(String pcv3Age) { this.pcv3Age = pcv3Age; }

    public String getPcv3Date() { return pcv3Date; }
    public void setPcv3Date(String pcv3Date) { this.pcv3Date = pcv3Date; }

    public String getMmr1Age() { return mmr1Age; }
    public void setMmr1Age(String mmr1Age) { this.mmr1Age = mmr1Age; }

    public String getMmr1Date() { return mmr1Date; }
    public void setMmr1Date(String mmr1Date) { this.mmr1Date = mmr1Date; }

    public String getMmr2Age() { return mmr2Age; }
    public void setMmr2Age(String mmr2Age) { this.mmr2Age = mmr2Age; }

    public String getMmr2Date() { return mmr2Date; }
    public void setMmr2Date(String mmr2Date) { this.mmr2Date = mmr2Date; }

    public boolean isFicBcg() { return ficBcg; }
    public void setFicBcg(boolean ficBcg) { this.ficBcg = ficBcg; }

    public boolean isFicDpt3() { return ficDpt3; }
    public void setFicDpt3(boolean ficDpt3) { this.ficDpt3 = ficDpt3; }

    public boolean isFicOpv3() { return ficOpv3; }
    public void setFicOpv3(boolean ficOpv3) { this.ficOpv3 = ficOpv3; }

    public boolean isFicMmr2() { return ficMmr2; }
    public void setFicMmr2(boolean ficMmr2) { this.ficMmr2 = ficMmr2; }

    public String getFicDate() { return ficDate; }
    public void setFicDate(String ficDate) { this.ficDate = ficDate; }

    public boolean isCicBcg() { return cicBcg; }
    public void setCicBcg(boolean cicBcg) { this.cicBcg = cicBcg; }

    public boolean isCicDpt3() { return cicDpt3; }
    public void setCicDpt3(boolean cicDpt3) { this.cicDpt3 = cicDpt3; }

    public boolean isCicOpv3() { return cicOpv3; }
    public void setCicOpv3(boolean cicOpv3) { this.cicOpv3 = cicOpv3; }

    public boolean isCicMmr2() { return cicMmr2; }
    public void setCicMmr2(boolean cicMmr2) { this.cicMmr2 = cicMmr2; }

    public String getCicDate() { return cicDate; }
    public void setCicDate(String cicDate) { this.cicDate = cicDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { this.isSynced = synced; }

    public boolean isNewInsert() { return newInsert; }
    public void setNewInsert(boolean newInsert) { this.newInsert = newInsert; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}