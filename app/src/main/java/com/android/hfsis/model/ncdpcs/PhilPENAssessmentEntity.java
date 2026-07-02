package com.android.hfsis.model.ncdpcs;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.android.hfsis.model.ncdpcs.converter.MonthMedConverter;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "philpen_risk_assessments")
@TypeConverters(MonthMedConverter.class)
public class PhilPENAssessmentEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    // Link to Household Profile Table
    @SerializedName("profile_id")
    public int profileId;

    @SerializedName("date_assessment")
    public String dateAssessment;

    @SerializedName("family_serial")
    public String familySerial;

    public String name;
    public String address;

    @SerializedName("date_of_birth")
    public String dateOfBirth;

    public String age;

    @SerializedName("age_group")
    public String ageGroup;

    public String sex;

    // Risk factors
    @SerializedName("current_smoker")
    public int currentSmoker;

    @SerializedName("bti_ask")
    public int btiAsk;

    @SerializedName("bti_advise")
    public int btiAdvise;

    @SerializedName("bti_assess")
    public int btiAssess;

    @SerializedName("bti_assist")
    public int btiAssist;

    @SerializedName("bti_arrange")
    public int btiArrange;

    @SerializedName("provided_bti")
    public int providedBTI;

    @SerializedName("binge_alcohol")
    public int bingeAlcohol;

    @SerializedName("insufficient_pa")
    public int insufficientPA;

    @SerializedName("unhealthy_diet")
    public int unhealthyDiet;

    @SerializedName("bmi_category")
    public int bmiCategory;

    // Hypertension Screening
    @SerializedName("screening_date1")
    public String screeningDate1;

    @SerializedName("screening_date2")
    public String screeningDate2;

    @SerializedName("bp_systolic1")
    public int bpSystolic1;

    @SerializedName("bp_diastolic1")
    public int bpDiastolic1;

    @SerializedName("bp_systolic2")
    public int bpSystolic2;

    @SerializedName("bp_diastolic2")
    public int bpDiastolic2;

    @SerializedName("hypertension_result")
    public int hypertensionResult;

    @SerializedName("meds_initial")
    public int medsInitial;

    @SerializedName("meds_changed")
    public int medsChanged;

    @SerializedName("monthly_meds")
    public MonthMed[] monthlyMeds;

    // Type 2 Diabetes Mellitus Screening
    @SerializedName("diabetes_result")
    public int diabetesResult;

    @SerializedName("antidiabetic_meds")
    public int antidiabeticMeds;

    @SerializedName("monthly_diabetic_meds")
    public MonthMed[] monthlyDiabeticMeds;

    // General remarks (applies to the assessment row)
    public String remarks;


    public static class MonthMed {
        public String month;
        public int pbf;       // Provided by Facility
        public int oop;       // Out-of-Pocket
        public boolean both;  // PBF >= 60% threshold

        public MonthMed(String month, int pbf, int oop, boolean both) {
            this.month = month;
            this.pbf = pbf;
            this.oop = oop;
            this.both = both;
        }
    }
}