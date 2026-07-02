package com.android.hfsis.model.maternal_care_record;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName; // Import required for accurate mapping

@Entity(tableName = "prenatal_immunization_records")
public class PrenatalImmunizationEntity {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    // Relational tracking reference matching maternalRecordId paradigm from 8ANC entity
    @SerializedName("maternalRecordId")
    public int maternalRecordId;

    // Tetanus Diphtheria Immunization fields tracking parameters
    @SerializedName("td1Date")
    public String td1Date;

    @SerializedName("td2Date")
    public String td2Date;

    @SerializedName("td3Date")
    public String td3Date;

    @SerializedName("td4Date")
    public String td4Date;

    @SerializedName("td5Date")
    public String td5Date;
}