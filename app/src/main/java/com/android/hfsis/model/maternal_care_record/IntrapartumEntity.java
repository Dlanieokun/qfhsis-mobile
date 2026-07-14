package com.android.hfsis.model.maternal_care_record;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName; // Import required for accurate network serialization
import com.android.hfsis.model.MaternalCareRecord;

@Entity(tableName = "intrapartum_records",
        foreignKeys = @ForeignKey(entity = MaternalCareRecord.class,
                parentColumns = "id",
                childColumns = "maternalRecordId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("maternalRecordId")})
public class IntrapartumEntity {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    @SerializedName("maternalRecordId")
    public int maternalRecordId; // Links directly to MaternalCareRecord.id

    // UI Delivery Form Fields
    @SerializedName("deliveryOutcome")
    public String deliveryOutcome;

    @SerializedName("deliveryType")
    public String deliveryType;

    @SerializedName("sex")
    public String sex;

    @SerializedName("birthWeight")
    public String birthWeight;

    @SerializedName("weightClassification")
    public String weightClassification;

    @SerializedName("placeOfDelivery")
    public String placeOfDelivery;

    @SerializedName("attendantAtBirth")
    public String attendantAtBirth;

    @SerializedName("deliveryDate")
    public String deliveryDate;

    @SerializedName("deliveryTime")
    public String deliveryTime;

    @SerializedName("remarks")
    public String remarks;

    // --- Sync Tracking ---
    @SerializedName("isSynced")
    public boolean isSynced = false;

    @SerializedName("newInsert")
    public boolean newInsert = true;

    @SerializedName("updated_at")
    public long updatedAt = System.currentTimeMillis();
}