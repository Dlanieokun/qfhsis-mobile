package com.android.hfsis.model.ncdpcs;

public class CervicalCancerScreeningData {
    public long id;
    public int profileId;
    public String dateAssessment;
    public String familySerial;
    public String name;
    public String address;
    public String dateOfBirth;
    public String age;

    // Cervical Cancer Screenings Dropdowns
    public int cervicalScreeningDone;
    public int cervicalResult;
    public int cervicalLinkedToCare;

    // Breast Mass Examination Dropdowns
    public int breastRiskAssessment;
    public String breastAgeRiskClass;
    public String breastExamType;
    public int breastResult;
    public int breastLinkedToCare;

    public String remarks;
}