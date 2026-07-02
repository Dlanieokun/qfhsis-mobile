package com.android.hfsis.database.syncing;

import com.android.hfsis.model.ClassificationEntity;
import com.android.hfsis.model.DropOutEntity;
import com.android.hfsis.model.FamilyPlanningRecord;
import com.android.hfsis.model.FollowUpEntity;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.MaternalCareRecord;
import com.android.hfsis.model.child.ChildImmunizationRecord;
import com.android.hfsis.model.child.ChildImmunizationSchoolRecord;
import com.android.hfsis.model.child.ChildNutritionRecord;
import com.android.hfsis.model.child.ChildSickRecord;
import com.android.hfsis.model.maternal_care_record.*;

import com.android.hfsis.model.ohc.OralHealthCareEntity;
import com.android.hfsis.model.ncdpcs.*;
import com.android.hfsis.model.geriatric.GeriatricScreeningRecord;
import com.android.hfsis.model.idpcs.filariasis.FilariasisRegistryRecord;
import com.android.hfsis.model.idpcs.leprosy.LeprosyRegistryRecord;
import com.android.hfsis.model.idpcs.rabies.RabiesRecord;
import com.android.hfsis.model.idpcs.schistosomiasis.SchistosomiasisRegistryRecord;
import com.android.hfsis.model.idpcs.sthpc.SoilTransmittedHelminthiasisRegistryRecord;

// --- NEW IMPORTS ---
import com.android.hfsis.model.ncdpcs.mental.MentalHealthRecord;
import com.android.hfsis.model.environmental.EnvironmentalHealthModel;

import java.util.ArrayList;
import java.util.List;

public class SyncPayload {

    public List<HouseholdProfile> householdProfiles;
    public List<FamilyPlanningRecord> familyPlanningRecords;
    public List<ClassificationEntity> classificationEntities;
    public List<FollowUpEntity> followUpEntities;
    public List<DropOutEntity> dropOutEntities;
    public List<MaternalCareRecord> maternalCareRecords;
    public List<Prenatal8AncEntity> prenatal8AncEntities;
    public List<PrenatalImmunizationEntity> prenatalImmunizationEntities;
    public List<PrenatalSupplementationEntity> prenatalSupplementationEntities;
    public List<PrenatalLabScreeningEntity> prenatalLabScreeningEntities;
    public List<IntrapartumEntity> intrapartumEntities;
    public List<PostpartumEntity> postpartumEntities;
    public List<ChildImmunizationRecord> childImmunizationRecords;
    public List<ChildImmunizationSchoolRecord> childImmunizationSchoolRecords;
    public List<ChildNutritionRecord> childNutritionRecords;
    public List<ChildSickRecord> childSickRecords;

    public List<OralHealthCareEntity> oralHealthCareRecords = new ArrayList<>();
    public List<PhilPENAssessmentEntity> philpenRiskAssessments = new ArrayList<>();
    public List<EyesScreeningsData> eyesScreenings = new ArrayList<>();
    public List<CervicalCancerScreeningEntity> cervicalCancerScreenings = new ArrayList<>();
    public List<GeriatricScreeningRecord> geriatricScreeningRecords = new ArrayList<>();
    public List<FilariasisRegistryRecord> filariasisRegistryRecords = new ArrayList<>();
    public List<LeprosyRegistryRecord> leprosyRegistryRecords = new ArrayList<>();
    public List<RabiesRecord> rabiesRecords = new ArrayList<>();
    public List<SchistosomiasisRegistryRecord> schistosomiasisRegistryRecords = new ArrayList<>();
    public List<SoilTransmittedHelminthiasisRegistryRecord> sthRegistryRecords = new ArrayList<>();

    // --- NEW FIELDS ---
    public List<MentalHealthRecord> mentalHealthRecords = new ArrayList<>();
    public List<EnvironmentalHealthModel> environmentalHealthRecords = new ArrayList<>();

    public SyncPayload(
            List<HouseholdProfile> householdProfiles,
            List<FamilyPlanningRecord> familyPlanningRecords,
            List<ClassificationEntity> classificationEntities,
            List<FollowUpEntity> followUpEntities,
            List<DropOutEntity> dropOutEntities,
            List<MaternalCareRecord> maternalCareRecords,
            List<Prenatal8AncEntity> prenatal8AncEntities,
            List<PrenatalImmunizationEntity> prenatalImmunizationEntities,
            List<PrenatalSupplementationEntity> prenatalSupplementationEntities,
            List<PrenatalLabScreeningEntity> prenatalLabScreeningEntities,
            List<IntrapartumEntity> intrapartumEntities,
            List<PostpartumEntity> postpartumEntities,
            List<ChildImmunizationRecord> childImmunizationRecords,
            List<ChildImmunizationSchoolRecord> childImmunizationSchoolRecords,
            List<ChildNutritionRecord> childNutritionRecords,
            List<ChildSickRecord> childSickRecords,
            List<OralHealthCareEntity> ohc,
            List<PhilPENAssessmentEntity> philpen,
            List<EyesScreeningsData> eyes,
            List<CervicalCancerScreeningEntity> cervical,
            List<GeriatricScreeningRecord> geriatric,
            List<FilariasisRegistryRecord> filariasis,
            List<LeprosyRegistryRecord> leprosy,
            List<RabiesRecord> rabies,
            List<SchistosomiasisRegistryRecord> schisto,
            List<SoilTransmittedHelminthiasisRegistryRecord> sth,
            List<MentalHealthRecord> mentalHealthRecords, // NEW
            List<EnvironmentalHealthModel> environmentalHealthRecords // NEW
    ) {
        this.householdProfiles = householdProfiles;
        this.familyPlanningRecords = familyPlanningRecords;
        this.classificationEntities = classificationEntities;
        this.followUpEntities = followUpEntities;
        this.dropOutEntities = dropOutEntities;
        this.maternalCareRecords = maternalCareRecords;
        this.prenatal8AncEntities = prenatal8AncEntities;
        this.prenatalImmunizationEntities = prenatalImmunizationEntities;
        this.prenatalSupplementationEntities = prenatalSupplementationEntities;
        this.prenatalLabScreeningEntities = prenatalLabScreeningEntities;
        this.intrapartumEntities = intrapartumEntities;
        this.postpartumEntities = postpartumEntities;
        this.childImmunizationRecords = childImmunizationRecords;
        this.childImmunizationSchoolRecords = childImmunizationSchoolRecords;
        this.childNutritionRecords = childNutritionRecords;
        this.childSickRecords = childSickRecords;

        this.oralHealthCareRecords = ohc;
        this.philpenRiskAssessments = philpen;
        this.eyesScreenings = eyes;
        this.cervicalCancerScreenings = cervical;
        this.geriatricScreeningRecords = geriatric;
        this.filariasisRegistryRecords = filariasis;
        this.leprosyRegistryRecords = leprosy;
        this.rabiesRecords = rabies;
        this.schistosomiasisRegistryRecords = schisto;
        this.sthRegistryRecords = sth;

        // Assign new lists
        this.mentalHealthRecords = mentalHealthRecords;
        this.environmentalHealthRecords = environmentalHealthRecords;
    }
}