package com.android.hfsis.model.maternal_care_record;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName; // Required for precise JSON mapping

@Entity(tableName = "postpartum_records")
public class PostpartumEntity {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    @SerializedName("maternalRecordId")
    public int maternalRecordId;

    // Visit Data Timestamps
    @SerializedName("visit24hDate")
    public String visit24hDate;

    @SerializedName("visit1wDate")
    public String visit1wDate;

    @SerializedName("visit2_4wDate")
    public String visit2_4wDate;

    @SerializedName("visit4_6wDate")
    public String visit4_6wDate;

    @SerializedName("classificationDate")
    public String classificationDate;

    // Enforces accurate mapping to match the exact casing in your Laravel migration file
    @SerializedName("PostpartumClassification")
    public String PostpartumClassification;

    // Blood Pressure Metric Pairs
    @SerializedName("bpSys24h")
    public String bpSys24h;
    @SerializedName("bpDias24h")
    public String bpDias24h;
    @SerializedName("bpSys1w")
    public String bpSys1w;
    @SerializedName("bpDias1w")
    public String bpDias1w;
    @SerializedName("bpSys2_4w")
    public String bpSys2_4w;
    @SerializedName("bpDias2_4w")
    public String bpDias2_4w;
    @SerializedName("bpSys4_6w")
    public String bpSys4_6w;
    @SerializedName("bpDias4_6w")
    public String bpDias4_6w;

    // General Assessment Metrics Parameters
    @SerializedName("highBpGeneral")
    public String highBpGeneral;

    @SerializedName("dangerSignsGeneral")
    public String dangerSignsGeneral;

    @SerializedName("referredGeneral")
    public String referredGeneral;

    // Danger Sign Boolean Indicators
    @SerializedName("dsBleeding")
    public boolean dsBleeding;

    @SerializedName("dsVision")
    public boolean dsVision;

    @SerializedName("dsAbdominal")
    public boolean dsAbdominal;

    @SerializedName("dsFever")
    public boolean dsFever;

    @SerializedName("dsBreathing")
    public boolean dsBreathing;

    @SerializedName("referralDateGeneral")
    public String referralDateGeneral;

    // Postpartum Supplementation logs
    @SerializedName("completedIfa")
    public String completedIfa;

    @SerializedName("ifaCompletionDate")
    public String ifaCompletionDate;

    @SerializedName("completedVitA")
    public String completedVitA;

    @SerializedName("vitACompletionDate")
    public String vitACompletionDate;

    @SerializedName("breastfeedingInitiationDate")
    public String breastfeedingInitiationDate;

    // Iron Tablet Longitudinal Checkpoints
    @SerializedName("ironTabs1st")
    public String ironTabs1st;
    @SerializedName("ironDate1st")
    public String ironDate1st;
    @SerializedName("ironTabs2nd")
    public String ironTabs2nd;
    @SerializedName("ironDate2nd")
    public String ironDate2nd;
    @SerializedName("ironTabs3rd")
    public String ironTabs3rd;
    @SerializedName("ironDate3rd")
    public String ironDate3rd;
}