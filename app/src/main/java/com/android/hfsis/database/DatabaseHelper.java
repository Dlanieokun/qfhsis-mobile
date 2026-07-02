package com.android.hfsis.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.android.hfsis.database.HouseholdProfileDao;
import com.android.hfsis.database.FamilyPlanningDao;
import com.android.hfsis.database.ClassificationDao;
import com.android.hfsis.database.FollowUpDao;
import com.android.hfsis.database.DropOutDao;
import com.android.hfsis.database.MaternalCareDao;

import com.android.hfsis.database.child.ChildImmunizationDao;
import com.android.hfsis.database.child.ChildImmunizationSchoolDao;
import com.android.hfsis.database.child.ChildNutritionDao;
import com.android.hfsis.database.child.ChildSickDao;
import com.android.hfsis.database.dao.BarangayDao;
import com.android.hfsis.database.dao.MunicipalityDao;
import com.android.hfsis.database.dao.ProvinceDao;
import com.android.hfsis.database.dao.RegionDao;
import com.android.hfsis.database.environmental.EnvironmentalHealthDao;
import com.android.hfsis.database.geriatric.GeriatricScreeningDao;
import com.android.hfsis.database.idpcs.filariasis.FilariasisDao;
import com.android.hfsis.database.idpcs.leprosy.LeprosyRegistryDao;
import com.android.hfsis.database.idpcs.rabies.RabiesDao;
import com.android.hfsis.database.idpcs.schistosomiasis.SchistosomiasisDao;
import com.android.hfsis.database.idpcs.sthpc.SoilTransmittedHelminthiasisDao;
import com.android.hfsis.database.maternal_care_record.IntrapartumDao;
import com.android.hfsis.database.maternal_care_record.PostpartumDao;
import com.android.hfsis.database.maternal_care_record.Prenatal8AncDao;
import com.android.hfsis.database.maternal_care_record.PrenatalImmunizationDao;
import com.android.hfsis.database.maternal_care_record.PrenatalLabScreeningDao;
import com.android.hfsis.database.maternal_care_record.PrenatalSupplementationDao;
import com.android.hfsis.database.ncdps.CervicalCancerScreeningDao;
import com.android.hfsis.database.ncdps.EyesScreeningDao;
import com.android.hfsis.database.ncdps.PhilPENDao;
import com.android.hfsis.database.ohc.OralHealthCareDao;
import com.android.hfsis.database.ncdps.MentalHealthDao;
import com.android.hfsis.model.DropOutEntity;
import com.android.hfsis.model.FamilyPlanningRecord;
import com.android.hfsis.model.FollowUpEntity;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.ClassificationEntity;
import com.android.hfsis.model.MaternalCareRecord;
import com.android.hfsis.model.address.Barangay;
import com.android.hfsis.model.address.Municipality;
import com.android.hfsis.model.address.Province;
import com.android.hfsis.model.address.Region;
import com.android.hfsis.model.child.ChildImmunizationRecord;
import com.android.hfsis.model.child.ChildImmunizationSchoolRecord;
import com.android.hfsis.model.child.ChildNutritionRecord;
import com.android.hfsis.model.child.ChildSickRecord;
import com.android.hfsis.model.environmental.EnvironmentalHealthModel;
import com.android.hfsis.model.geriatric.GeriatricScreeningRecord;
import com.android.hfsis.model.idpcs.filariasis.FilariasisRegistryRecord;
import com.android.hfsis.model.idpcs.leprosy.LeprosyRegistryRecord;
import com.android.hfsis.model.idpcs.rabies.RabiesRecord;
import com.android.hfsis.model.idpcs.schistosomiasis.SchistosomiasisRegistryRecord;
import com.android.hfsis.model.idpcs.sthpc.SoilTransmittedHelminthiasisRegistryRecord;
import com.android.hfsis.model.maternal_care_record.IntrapartumEntity;
import com.android.hfsis.model.maternal_care_record.PostpartumEntity;
import com.android.hfsis.model.maternal_care_record.Prenatal8AncEntity;
import com.android.hfsis.model.maternal_care_record.PrenatalImmunizationEntity;
import com.android.hfsis.model.maternal_care_record.PrenatalLabScreeningEntity;
import com.android.hfsis.model.maternal_care_record.PrenatalSupplementationEntity;
import com.android.hfsis.model.ncdpcs.CervicalCancerScreeningEntity;
import com.android.hfsis.model.ncdpcs.EyesScreeningsData;
import com.android.hfsis.model.ncdpcs.PhilPENAssessmentEntity;
import com.android.hfsis.model.ncdpcs.mental.MentalHealthRecord;
import com.android.hfsis.model.ohc.OralHealthCareEntity;


@Database(
        entities = {
                HouseholdProfile.class,
                FamilyPlanningRecord.class,
                ClassificationEntity.class,
                FollowUpEntity.class,
                DropOutEntity.class,
                MaternalCareRecord.class,
                Prenatal8AncEntity.class,
                PrenatalImmunizationEntity.class,
                PrenatalSupplementationEntity.class,
                PrenatalLabScreeningEntity.class,
                IntrapartumEntity.class,
                PostpartumEntity.class,
                ChildImmunizationRecord.class,
                ChildImmunizationSchoolRecord.class,
                ChildSickRecord.class,
                ChildNutritionRecord.class,
                Region.class,
                Province.class,
                Municipality.class,
                Barangay.class,
                OralHealthCareEntity.class,
                PhilPENAssessmentEntity.class,
                EyesScreeningsData.class,
                CervicalCancerScreeningEntity.class,
                MentalHealthRecord.class,
                GeriatricScreeningRecord.class,
                FilariasisRegistryRecord.class,
                RabiesRecord.class,
                SchistosomiasisRegistryRecord.class,
                SoilTransmittedHelminthiasisRegistryRecord.class,
                LeprosyRegistryRecord.class,
                EnvironmentalHealthModel.class
        },
        version = 2,
        exportSchema = false
)
public abstract class DatabaseHelper extends RoomDatabase {

    private static volatile DatabaseHelper instance;

    // Data Access Object References
    public abstract HouseholdProfileDao householdProfileDao();
    public abstract FamilyPlanningDao familyPlanningDao();
    public abstract ClassificationDao classificationDao();
    public abstract FollowUpDao followUpDao();
    public abstract DropOutDao dropOutDao();
    public abstract MaternalCareDao maternalCareDao();
    public abstract Prenatal8AncDao prenatal8AncDao();
    public abstract PrenatalImmunizationDao prenatalImmunizationDao();
    public abstract PrenatalSupplementationDao prenatalSupplementationDao();
    public abstract PrenatalLabScreeningDao prenatalLabScreeningDao();
    public abstract IntrapartumDao intrapartumDao();
    public abstract PostpartumDao postpartumDao();
    public abstract ChildImmunizationDao childImmunizationDao();
    public abstract ChildImmunizationSchoolDao childImmunizationSchoolDao();
    public abstract ChildSickDao childSickDao();
    public abstract ChildNutritionDao childNutritionDao();
    public abstract RegionDao regionDao();
    public abstract ProvinceDao provinceDao();
    public abstract MunicipalityDao municipalityDao();
    public abstract BarangayDao barangayDao();
    public abstract OralHealthCareDao oralHealthCareDao();
    public abstract PhilPENDao philPENDao();
    public abstract EyesScreeningDao eyesScreeningDao();
    public abstract CervicalCancerScreeningDao cervicalCancerScreeningDao();
    public abstract MentalHealthDao mentalHealthDao();
    public abstract GeriatricScreeningDao geriatricScreeningDao();
    public abstract FilariasisDao filariasisDao();
    public abstract RabiesDao rabiesDao();
    public abstract SchistosomiasisDao schistosomiasisDao();
    public abstract SoilTransmittedHelminthiasisDao soilTransmittedHelminthiasisDao();
    public abstract LeprosyRegistryDao leprosyRegistryDao();
    public abstract EnvironmentalHealthDao environmentalHealthDao();

    // Original method mapping
    public static DatabaseHelper getDatabase(final Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    DatabaseHelper.class, "hfsis_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    // Added to resolve 'cannot find symbol' error across your fragments
    public static DatabaseHelper getInstance(final Context context) {
        return getDatabase(context);
    }

}