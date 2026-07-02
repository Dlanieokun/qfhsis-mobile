package com.android.hfsis.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName; // Ensure this is imported

@Entity(
        tableName = "classification_metrics",
        foreignKeys = @ForeignKey(
                entity = HouseholdProfile.class,
                parentColumns = "id",
                childColumns = "profile_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("profile_id")}
)
public class ClassificationEntity {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    public long id;

    @ColumnInfo(name = "profile_id")
    @SerializedName("profile_id")
    public long profileId;

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at_timestamp") // Custom key to handle gracefully in Laravel
    public long createdAt;

    @ColumnInfo(name = "q1_age")
    @SerializedName("q1_age")
    public String q1Age;

    @ColumnInfo(name = "q1_class")
    @SerializedName("q1_class")
    public String q1Class;

    @ColumnInfo(name = "q2_age")
    @SerializedName("q2_age")
    public String q2Age;

    @ColumnInfo(name = "q2_class")
    @SerializedName("q2_class")
    public String q2Class;

    @ColumnInfo(name = "q3_age")
    @SerializedName("q3_age")
    public String q3Age;

    @ColumnInfo(name = "q3_class")
    @SerializedName("q3_class")
    public String q3Class;

    @ColumnInfo(name = "q4_age")
    @SerializedName("q4_age")
    public String q4Age;

    @ColumnInfo(name = "q4_class")
    @SerializedName("q4_class")
    public String q4Class;
}
