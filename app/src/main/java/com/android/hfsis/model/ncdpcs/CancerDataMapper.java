package com.android.hfsis.model.ncdpcs;

import com.android.hfsis.ncdpcs.cancer.CervicalCancerScreeningFragment.CervicalCancerScreeningData;

public class CancerDataMapper {

    public static CervicalCancerScreeningEntity toEntity(CervicalCancerScreeningData data) {
        if (data == null) return null;

        CervicalCancerScreeningEntity entity = new CervicalCancerScreeningEntity();

        // Retain the primary key ID for update actions (0 signals a fresh auto-generation insert)
        entity.setId(data.id);

        entity.setDateAssessment(data.dateAssessment);
        entity.setFamilySerial(data.familySerial);
        entity.setName(data.name);
        entity.setAddress(data.address);
        entity.setDateOfBirth(data.dateOfBirth);
        entity.setAge(data.age);

        entity.setCervicalScreeningDone(data.cervicalScreeningDone);
        entity.setCervicalResult(data.cervicalResult);
        entity.setCervicalLinkedToCare(data.cervicalLinkedToCare);

        entity.setBreastRiskAssessment(data.breastRiskAssessment);
        entity.setBreastAgeRiskClass(data.breastAgeRiskClass);
        entity.setBreastExamType(data.breastExamType);
        entity.setBreastResult(data.breastResult);
        entity.setBreastLinkedToCare(data.breastLinkedToCare);

        entity.setRemarks(data.remarks);
        return entity;
    }
}