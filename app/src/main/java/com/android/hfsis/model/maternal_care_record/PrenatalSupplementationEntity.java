package com.android.hfsis.model.maternal_care_record;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "prenatal_supplementation_records")
public class PrenatalSupplementationEntity {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public int id;

    // Foreign structural relational reference linking to parent MaternalCareRecord
    @SerializedName("maternal_record_id")
    public int maternalRecordId;

    // Deworming Status parameters tracking
    @SerializedName("received_deworming")
    public boolean receivedDeworming;

    @SerializedName("deworming_date")
    public String dewormingDate;

    // Section 2: Iron Folic Acid (IFA) Tablets (#) and Dates (d)
    @SerializedName("ifa_v1_num")
    public String ifaV1Num;

    @SerializedName("ifa_v1_date")
    public String ifaV1Date;

    @SerializedName("ifa_v2_num")
    public String ifaV2Num;

    @SerializedName("ifa_v2_date")
    public String ifaV2Date;

    @SerializedName("ifa_v3_num")
    public String ifaV3Num;

    @SerializedName("ifa_v3_date")
    public String ifaV3Date;

    @SerializedName("ifa_v4_num")
    public String ifaV4Num;

    @SerializedName("ifa_v4_date")
    public String ifaV4Date;

    @SerializedName("ifa_v5_num")
    public String ifaV5Num;

    @SerializedName("ifa_v5_date")
    public String ifaV5Date;

    @SerializedName("ifa_v6_num")
    public String ifaV6Num;

    @SerializedName("ifa_v6_date")
    public String ifaV6Date;

    @SerializedName("completed_ifa")
    public boolean completedIfa;

    @SerializedName("ifa_completed_date")
    public String ifaCompletedDate;

    // Section 3: Multiple Micronutrients (MM) Capsules (#) and Dates (d)
    @SerializedName("mm_v1_num")
    public String mmV1Num;

    @SerializedName("mm_v1_date")
    public String mmV1Date;

    @SerializedName("mm_v2_num")
    public String mmV2Num;

    @SerializedName("mm_v2_date")
    public String mmV2Date;

    @SerializedName("mm_v3_num")
    public String mmV3Num;

    @SerializedName("mm_v3_date")
    public String mmV3Date;

    @SerializedName("mm_v4_num")
    public String mmV4Num;

    @SerializedName("mm_v4_date")
    public String mmV4Date;

    @SerializedName("mm_v5_num")
    public String mmV5Num;

    @SerializedName("mm_v5_date")
    public String mmV5Date;

    @SerializedName("mm_v6_num")
    public String mmV6Num;

    @SerializedName("mm_v6_date")
    public String mmV6Date;

    @SerializedName("completed_mm")
    public boolean completedMm;

    @SerializedName("mm_completed_date")
    public String mmCompletedDate;

    // Section 4: Calcium Carbonate (CC) Tablets (#) and Dates (d)
    @SerializedName("cc_v2_num")
    public String ccV2Num;

    @SerializedName("cc_v2_date")
    public String ccV2Date;

    @SerializedName("cc_v3_num")
    public String ccV3Num;

    @SerializedName("cc_v3_date")
    public String ccV3Date;

    @SerializedName("cc_v4_num")
    public String ccV4Num;

    @SerializedName("cc_v4_date")
    public String ccV4Date;

    @SerializedName("completed_cc")
    public boolean completedCc;

    @SerializedName("cc_completed_date") // Maps flawlessly to your custom table blueprint column!
    public String ccCompletedDate;

    // --- Sync Tracking ---
    @SerializedName("isSynced")
    @ColumnInfo(name = "isSynced")
    public boolean isSynced = false;

    @SerializedName("newInsert")
    @ColumnInfo(name = "newInsert")
    public boolean newInsert = true;

    @SerializedName("updated_at")
    @ColumnInfo(name = "updated_at")
    public long updatedAt = System.currentTimeMillis();

    // Public empty constructor required for Room database engine operations
    public PrenatalSupplementationEntity() {}
}