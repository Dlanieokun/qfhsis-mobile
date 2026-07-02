package com.android.hfsis.database.syncing;

import com.android.hfsis.model.ClassificationEntity;
import com.android.hfsis.model.DropOutEntity;
import com.android.hfsis.model.FamilyPlanningRecord;
import com.android.hfsis.model.FollowUpEntity;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.MaternalCareRecord;
import com.android.hfsis.model.address.Barangay;
import com.android.hfsis.model.address.Municipality;
import com.android.hfsis.model.address.Province;
import com.android.hfsis.model.address.Region;
import com.android.hfsis.model.child.ChildImmunizationRecord;
import com.android.hfsis.model.child.ChildImmunizationSchoolRecord;
import com.android.hfsis.model.child.ChildNutritionRecord;
import com.android.hfsis.model.child.ChildSickRecord;
import com.android.hfsis.model.maternal_care_record.IntrapartumEntity;
import com.android.hfsis.model.maternal_care_record.PostpartumEntity;
import com.android.hfsis.model.maternal_care_record.Prenatal8AncEntity;
import com.android.hfsis.model.maternal_care_record.PrenatalImmunizationEntity;
import com.android.hfsis.model.maternal_care_record.PrenatalLabScreeningEntity;
import com.android.hfsis.model.maternal_care_record.PrenatalSupplementationEntity;

import com.android.hfsis.model.ohc.OralHealthCareEntity;
import com.android.hfsis.model.ncdpcs.PhilPENAssessmentEntity;
import com.android.hfsis.model.ncdpcs.EyesScreeningsData;
import com.android.hfsis.model.ncdpcs.CervicalCancerScreeningEntity;
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

public class SyncPullPayload {

    // ── Philippine Locations (Reference Data) ───────────────────────────────────
    public List<Region>       regions       = new ArrayList<>();
    public List<Province>     provinces     = new ArrayList<>();
    public List<Municipality> municipalities = new ArrayList<>();
    public List<Barangay>     barangays     = new ArrayList<>();

    // ── Household & Family Planning ───────────────────────────────────────────
    public List<HouseholdProfile>      householdProfiles               = new ArrayList<>();
    public List<FamilyPlanningRecord>  familyPlanningRecords           = new ArrayList<>();
    public List<ClassificationEntity>  classificationEntities          = new ArrayList<>();
    public List<FollowUpEntity>        followUpEntities                = new ArrayList<>();
    public List<DropOutEntity>         dropOutEntities                 = new ArrayList<>();

    // ── Maternal Care ─────────────────────────────────────────────────────────
    public List<MaternalCareRecord>          maternalCareRecords             = new ArrayList<>();
    public List<Prenatal8AncEntity>          prenatal8AncEntities            = new ArrayList<>();
    public List<PrenatalImmunizationEntity>  prenatalImmunizationEntities    = new ArrayList<>();
    public List<PrenatalSupplementationEntity> prenatalSupplementationEntities = new ArrayList<>();
    public List<PrenatalLabScreeningEntity>  prenatalLabScreeningEntities    = new ArrayList<>();
    public List<IntrapartumEntity>           intrapartumEntities             = new ArrayList<>();
    public List<PostpartumEntity>            postpartumEntities              = new ArrayList<>();

    // ── Child Health ──────────────────────────────────────────────────────────
    public List<ChildImmunizationRecord>       childImmunizationRecords       = new ArrayList<>();
    public List<ChildImmunizationSchoolRecord> childImmunizationSchoolRecords = new ArrayList<>();
    public List<ChildNutritionRecord>          childNutritionRecords          = new ArrayList<>();
    public List<ChildSickRecord>               childSickRecords               = new ArrayList<>();

    // ── Oral Health Care & NCD ────────────────────────────────────────────────
    public List<OralHealthCareEntity>          oralHealthCareRecords          = new ArrayList<>();
    public List<PhilPENAssessmentEntity>       philpenRiskAssessments         = new ArrayList<>();
    public List<EyesScreeningsData>            eyesScreenings                 = new ArrayList<>();
    public List<CervicalCancerScreeningEntity> cervicalCancerScreenings       = new ArrayList<>();
    public List<GeriatricScreeningRecord>      geriatricScreeningRecords      = new ArrayList<>();

    // ── Infectious Disease (IDPCS) ────────────────────────────────────────────
    public List<FilariasisRegistryRecord>      filariasisRegistryRecords      = new ArrayList<>();
    public List<LeprosyRegistryRecord>         leprosyRegistryRecords         = new ArrayList<>();
    public List<RabiesRecord>                  rabiesRecords                  = new ArrayList<>();
    public List<SchistosomiasisRegistryRecord> schistosomiasisRegistryRecords = new ArrayList<>();
    public List<SoilTransmittedHelminthiasisRegistryRecord> sthRegistryRecords = new ArrayList<>();

    // --- NEW: Mental & Environmental Health ---
    public List<MentalHealthRecord>            mentalHealthRecords            = new ArrayList<>();
    public List<EnvironmentalHealthModel>      environmentalHealthRecords     = new ArrayList<>();
}