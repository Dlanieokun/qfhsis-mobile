package com.android.hfsis.model.maternal_care_record;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName; // Required for network payload accuracy

@Entity(tableName = "prenatal_8anc_records")
public class Prenatal8AncEntity {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    @SerializedName("maternalRecordId")
    public int maternalRecordId;

    // 1st & 2nd Trimesters (Visits 1-3)
    @SerializedName("visit1Date")
    public String visit1Date;
    @SerializedName("visit1Bp")
    public String visit1Bp;
    @SerializedName("visit2Date")
    public String visit2Date;
    @SerializedName("visit2Bp")
    public String visit2Bp;
    @SerializedName("visit3Date")
    public String visit3Date;
    @SerializedName("visit3Bp")
    public String visit3Bp;

    // 3rd Trimester (Visits 4-8)
    @SerializedName("visit4Date")
    public String visit4Date;
    @SerializedName("visit4Bp")
    public String visit4Bp;
    @SerializedName("visit5Date")
    public String visit5Date;
    @SerializedName("visit5Bp")
    public String visit5Bp;
    @SerializedName("visit6Date")
    public String visit6Date;
    @SerializedName("visit6Bp")
    public String visit6Bp;
    @SerializedName("visit7Date")
    public String visit7Date;
    @SerializedName("visit7Bp")
    public String visit7Bp;
    @SerializedName("visit8Date")
    public String visit8Date;
    @SerializedName("visit8Bp")
    public String visit8Bp;

    // Evaluation Status Flags
    @SerializedName("completed8Anc")
    public boolean completed8Anc;
    @SerializedName("highBp")
    public boolean highBp;
    @SerializedName("dangerSigns")
    public boolean dangerSigns;
    @SerializedName("dangerSignsDetail")
    public String dangerSignsDetail;

    // Referrals & Outcome Fields
    @SerializedName("highBpReferred")
    public boolean highBpReferred;
    @SerializedName("dateReferred")
    public String dateReferred;
    @SerializedName("classificationStatus")
    public String classificationStatus;
    @SerializedName("classificationDate")
    public String classificationDate;
}