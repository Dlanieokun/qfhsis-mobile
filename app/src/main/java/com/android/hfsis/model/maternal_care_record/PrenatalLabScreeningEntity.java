package com.android.hfsis.model.maternal_care_record;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "prenatal_lab_screening_records")
public class PrenatalLabScreeningEntity {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    @SerializedName("maternalRecordId")
    public int maternalRecordId;

    // CBC Fields
    @SerializedName("cbcDate")
    public String cbcDate;
    @SerializedName("cbcResult")
    public String cbcResult;
    @SerializedName("cbcRemarks")
    public String cbcRemarks;

    // GDM Fields
    @SerializedName("gdmDate")
    public String gdmDate;
    @SerializedName("gdmResult")
    public String gdmResult;
    @SerializedName("gdmRemarks")
    public String gdmRemarks;

    // Hepatitis B Fields
    @SerializedName("hepBDate")
    public String hepBDate;
    @SerializedName("hepBResult")
    public String hepBResult;
    @SerializedName("hepBRemarks")
    public String hepBRemarks;

    // HIV Fields
    @SerializedName("hivDate")
    public String hivDate;
    @SerializedName("hivResult")
    public String hivResult;
    @SerializedName("hivRemarks")
    public String hivRemarks;

    // Syphilis Core Fields
    @SerializedName("syphilisDate")
    public String syphilisDate;
    @SerializedName("syphilisResult")
    public String syphilisResult;
    @SerializedName("syphilisRemarks")
    public String syphilisRemarks;

    // Added Syphilis Extra Metadata Fields
    @SerializedName("syphilisConfirmatoryDate")
    public String syphilisConfirmatoryDate;

    @SerializedName("syphilisConfirmatoryResult")
    public String syphilisConfirmatoryResult;

    @SerializedName("syphilisTreatment")
    public String syphilisTreatment;
}