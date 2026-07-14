package com.android.hfsis;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.android.hfsis.child.ChildCareServicesFragment;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.database.syncing.SyncApiService;
import com.android.hfsis.database.syncing.SyncPayload;
import com.android.hfsis.database.syncing.SyncPullPayload;
import com.android.hfsis.database.syncing.SyncPullResponse;
import com.android.hfsis.database.syncing.SyncPushResponse;
import com.android.hfsis.vital_satatistics.environmental.MasterlistEnvironmentalHealthFragment;
import com.android.hfsis.geriatric.ViewGeriatricScreeningFragment;
import com.android.hfsis.idpcs.IDPCSFragment;
import com.android.hfsis.ncdpcs.NCDPCSFragment;
import com.android.hfsis.model.*;
import com.android.hfsis.model.child.ChildImmunizationRecord;
import com.android.hfsis.model.child.ChildImmunizationSchoolRecord;
import com.android.hfsis.model.child.ChildNutritionRecord;
import com.android.hfsis.model.child.ChildSickRecord;
import com.android.hfsis.model.maternal_care_record.*;
import com.android.hfsis.maternal_care_record.ViewMaternalCareRecordsFragment;
import com.android.hfsis.ohc.ViewOralHealthCareFragment;
import com.android.hfsis.vital_satatistics.environmental.ViewMasterlistEnvironmentalHealthFragment;

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

// --- NEW MODULE IMPORTS ---
import com.android.hfsis.model.ncdpcs.mental.MentalHealthRecord;
import com.android.hfsis.model.environmental.EnvironmentalHealthModel;

import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OtherServicesFragment extends Fragment {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_API_BASE_URL = "api_base_url";
    private static final String KEY_LAST_SYNCED_AT = "last_synced_at";
    private static final String DEFAULT_BASE_URL = "http://192.168.0.174:8000/";

    // Buttons Setup
    private Button btnSync;
    private Button btnPull;
    private Button btnUpload;
    private Button btnProfiling, btnFamilyPlanning, btnMaternalCare;
    private Button btnChildCare, btnOralHealth, btnNonCommunicable, btnGeriatricHealth;
    private Button btnInfectiousDisease, btnWash, btnDemographics, btnVitalStatistics;

    // Overlay Progress Widgets
    private ConstraintLayout progressOverlay;
    private ProgressBar syncProgressBar;
    private TextView tvProgressPercent;

    public OtherServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_other_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize All UI Buttons
        btnSync = view.findViewById(R.id.btnSync);
        btnPull = view.findViewById(R.id.btnPull);
        btnUpload = view.findViewById(R.id.btnUpload);
        btnProfiling = view.findViewById(R.id.btnProfiling);
        btnFamilyPlanning = view.findViewById(R.id.btnFamilyPlanning);
        btnMaternalCare = view.findViewById(R.id.btnMaternalCare);
        btnChildCare = view.findViewById(R.id.btnChildCare);
        btnOralHealth = view.findViewById(R.id.btnOralHealth);
        btnNonCommunicable = view.findViewById(R.id.btnNonCommunicable);
        btnGeriatricHealth = view.findViewById(R.id.btnGeriatricHealth);
        btnInfectiousDisease = view.findViewById(R.id.btnInfectiousDisease);
        btnWash = view.findViewById(R.id.btnWash);
        btnDemographics = view.findViewById(R.id.btnDemographics);
        btnVitalStatistics = view.findViewById(R.id.btnVitalStatistics);

        // Initialize Progress Elements
        progressOverlay = view.findViewById(R.id.progressOverlay);
        syncProgressBar = view.findViewById(R.id.syncProgressBar);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);

        // Push (Android -> Server) Trigger
        btnSync.setOnClickListener(v -> {
            progressOverlay.setVisibility(View.VISIBLE);
            syncProgressBar.setProgress(0);
            tvProgressPercent.setText("Syncing: 0%\nStarting Sync Session...");
            triggerDatabaseSync();
        });

        // Pull (Server -> Android) Trigger
        if (btnPull != null) {
            btnPull.setOnClickListener(v -> {
                progressOverlay.setVisibility(View.VISIBLE);
                syncProgressBar.setProgress(0);
                tvProgressPercent.setText("Pulling: 0%\nStarting Pull Session...");
                triggerDatabasePull();
            });
        }

        // Upload Trigger
        if (btnUpload != null) {
            btnUpload.setOnClickListener(v -> {
                progressOverlay.setVisibility(View.VISIBLE);
                syncProgressBar.setProgress(0);
                tvProgressPercent.setText("Uploading: 0%\nStarting Upload Session...");
                triggerUpload();
            });
        }

        // Active Transactions
        btnProfiling.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ViewHouseholdProfileFragment())
                        .addToBackStack(null).commit();
            }
        });

        btnFamilyPlanning.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ViewFamilyPlanningFragment())
                        .addToBackStack(null).commit();
            }
        });

        btnMaternalCare.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ViewMaternalCareRecordsFragment())
                        .addToBackStack(null).commit();
            }
        });

        // Placeholders
        btnChildCare.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ChildCareServicesFragment())
                        .addToBackStack(null).commit();
            }
        });
        btnOralHealth.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ViewOralHealthCareFragment())
                        .addToBackStack(null).commit();
            }
        });
        btnNonCommunicable.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new NCDPCSFragment())
                        .addToBackStack(null).commit();
            }
        });
        btnGeriatricHealth.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ViewGeriatricScreeningFragment())
                        .addToBackStack(null).commit();
            }
        });
        btnInfectiousDisease.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new IDPCSFragment())
                        .addToBackStack(null).commit();
            }
        });
        btnWash.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ViewMasterlistEnvironmentalHealthFragment())
                        .addToBackStack(null).commit();
            }
        });
        btnDemographics.setOnClickListener(v -> Toast.makeText(getContext(), "Demographics Clicked", Toast.LENGTH_SHORT).show());
        btnVitalStatistics.setOnClickListener(v -> Toast.makeText(getContext(), "Vital Statistics Clicked", Toast.LENGTH_SHORT).show());
    }

    // ─────────────────────────────────────────────────────────────
    //  PUSH  –  Android  →  Laravel
    // ─────────────────────────────────────────────────────────────
    private void triggerDatabaseSync() {
        new Thread(() -> {
            try {
                updateProgress(5, "Opening Database Connection...");
                DatabaseHelper db = DatabaseHelper.getInstance(getContext());

                updateProgress(15, "Extracting Household Profiles...");
                List<HouseholdProfile> profiles = db.householdProfileDao().getAllProfiles();

                updateProgress(20, "Extracting Family Planning Records...");
                List<FamilyPlanningRecord> familyPlans = db.familyPlanningDao().getAllRecords();

                updateProgress(28, "Extracting Classification Data...");
                List<ClassificationEntity> classes = db.classificationDao().getAll();
                List<FollowUpEntity> followUps = db.followUpDao().getAll();
                List<DropOutEntity> dropOuts = db.dropOutDao().getAll();

                updateProgress(40, "Extracting Maternal Care Forms...");
                List<MaternalCareRecord> maternal = db.maternalCareDao().getAll();
                List<Prenatal8AncEntity> anc = db.prenatal8AncDao().getAll();
                List<PrenatalImmunizationEntity> immunizations = db.prenatalImmunizationDao().getAll();
                List<PrenatalSupplementationEntity> supplements = db.prenatalSupplementationDao().getAll();
                List<PrenatalLabScreeningEntity> labs = db.prenatalLabScreeningDao().getAll();
                List<IntrapartumEntity> intra = db.intrapartumDao().getAll();
                List<PostpartumEntity> post = db.postpartumDao().getAll();

                updateProgress(55, "Extracting Child Health Records...");
                List<ChildImmunizationRecord> childImmunizations = db.childImmunizationDao().getAll();
                List<ChildImmunizationSchoolRecord> childImmunizationsSchool = db.childImmunizationSchoolDao().getAll();
                List<ChildNutritionRecord> childNutrition = db.childNutritionDao().getAllRecords();
                List<ChildSickRecord> childSick = db.childSickDao().getAllRecords();

                updateProgress(65, "Extracting OHC, NCD, and IDPCS Records...");
                List<OralHealthCareEntity> ohc = db.oralHealthCareDao().getAll();
                List<PhilPENAssessmentEntity> philpen = db.philPENDao().getAll();
                List<EyesScreeningsData> eyes = db.eyesScreeningDao().getAll();
                List<CervicalCancerScreeningEntity> cervical = db.cervicalCancerScreeningDao().getAll();
                List<GeriatricScreeningRecord> geriatric = db.geriatricScreeningDao().getAll();

                List<FilariasisRegistryRecord> filariasis = db.filariasisDao().getAll();
                List<LeprosyRegistryRecord> leprosy = db.leprosyRegistryDao().getAll();
                List<RabiesRecord> rabies = db.rabiesDao().getAll();
                List<SchistosomiasisRegistryRecord> schisto = db.schistosomiasisDao().getAll();
                List<SoilTransmittedHelminthiasisRegistryRecord> sth = db.soilTransmittedHelminthiasisDao().getAll();

                // --- NEW EXTRACTS ---
                List<MentalHealthRecord> mentalHealth = db.mentalHealthDao().getAll();
                List<EnvironmentalHealthModel> envHealth = db.environmentalHealthDao().getAll();

                updateProgress(70, "Packaging JSON Data Payload...");

                SyncPayload payload = new SyncPayload(
                        profiles, familyPlans, classes, followUps, dropOuts,
                        maternal, anc, immunizations, supplements, labs, intra, post,
                        childImmunizations, childImmunizationsSchool, childNutrition, childSick,
                        ohc, philpen, eyes, cervical, geriatric,
                        filariasis, leprosy, rabies, schisto, sth,
                        mentalHealth, envHealth // NEW
                );

                updateProgress(85, "Uploading payload to Laravel Server...");

                Retrofit retrofit = buildRetrofit();
                SyncApiService apiService = retrofit.create(SyncApiService.class);
                Response<SyncPushResponse> response = apiService.pushToServer(payload).execute();

                new Handler(Looper.getMainLooper()).post(() -> {
                    progressOverlay.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null
                            && "success".equalsIgnoreCase(response.body().status)) {
                        Toast.makeText(getContext(), "Sync Success! Server Database Updated.", Toast.LENGTH_LONG).show();
                    } else if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(getContext(), "Sync Failed: " + response.body().message, Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            if (response.errorBody() != null) {
                                String errorJson = response.errorBody().string();
                                android.util.Log.e("SYNC_ERROR", "Server Push Crash: " + errorJson);
                            }
                        } catch (Exception ignored) {}
                        Toast.makeText(getContext(), "Sync Failed: Server HTTP Error " + response.code() + ". Check Logcat!", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Network Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────
    //  PULL  –  Laravel  →  Android
    // ─────────────────────────────────────────────────────────────
    private void triggerDatabasePull() {
        new Thread(() -> {
            try {
                updateProgress(10, "Opening Database Connection...");
                DatabaseHelper db = DatabaseHelper.getInstance(getContext());

                final String finalLastSyncedAt;
                android.content.SharedPreferences prefs = null;
                if (getContext() != null) {
                    prefs = getContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
                    finalLastSyncedAt = prefs.getString(KEY_LAST_SYNCED_AT, null);
                } else {
                    finalLastSyncedAt = null;
                }

                // Pull is now scoped server-side to the logged-in user's
                // assigned location, so it requires a valid bearer token.
                String authToken = prefs != null ? prefs.getString("auth_bearer_token", null) : null;
                if (authToken == null || authToken.isEmpty()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Please log in again before pulling.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                updateProgress(25, finalLastSyncedAt == null
                        ? "Requesting Full Pull From Server..."
                        : "Requesting Delta Pull Since " + finalLastSyncedAt + "...");

                Retrofit retrofit = buildRetrofit();
                SyncApiService apiService = retrofit.create(SyncApiService.class);
                Response<SyncPullResponse> response = apiService.pullFromServer("Bearer " + authToken, finalLastSyncedAt).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    final int code = response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            android.util.Log.e("SYNC_ERROR", "Server Pull Crash: " + errorJson);
                        }
                    } catch (Exception ignored) {}

                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Pull Failed (HTTP " + code + "). Check Logcat!", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                SyncPullResponse pullResponse = response.body();
                if (!"success".equalsIgnoreCase(pullResponse.status) || pullResponse.data == null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Pull Failed: Server returned an error.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                SyncPullPayload data = pullResponse.data;

                // WRAP DATABASE INSERTS IN A TRANSACTION FOR MASSIVE PERFORMANCE INCREASE
                db.runInTransaction(() -> {

                    // Clear out lookups during a full pull to avoid duplicates or indexing conflict anomalies
                    if (finalLastSyncedAt == null) {
                        db.regionDao().deleteAll();
                        db.provinceDao().deleteAll();
                        db.municipalityDao().deleteAll();
                        db.barangayDao().deleteAll();
                    }

                    // Save Location Reference Tables safely with Null and Empty Checks
                    if (data.regions != null && !data.regions.isEmpty()) {
                        db.regionDao().insertAll(data.regions);
                    }
                    if (data.provinces != null && !data.provinces.isEmpty()) {
                        db.provinceDao().insertAll(data.provinces);
                    }
                    if (data.municipalities != null && !data.municipalities.isEmpty()) {
                        db.municipalityDao().insertAll(data.municipalities);
                    }
                    if (data.barangays != null && !data.barangays.isEmpty()) {
                        db.barangayDao().insertAll(data.barangays);
                    }

                    // Save Household & Family Planning Records
                    for (HouseholdProfile r : data.householdProfiles) db.householdProfileDao().insertProfile(r);
                    for (FamilyPlanningRecord r : data.familyPlanningRecords) db.familyPlanningDao().insertRecord(r);
                    for (ClassificationEntity r : data.classificationEntities) db.classificationDao().saveClassification(r);
                    for (FollowUpEntity r : data.followUpEntities) db.followUpDao().insertFollowUp(r);
                    for (DropOutEntity r : data.dropOutEntities) db.dropOutDao().insertDropOut(r);

                    // Save Maternal Care Records
                    for (MaternalCareRecord r : data.maternalCareRecords) db.maternalCareDao().insertMaternalRecord(r);
                    for (Prenatal8AncEntity r : data.prenatal8AncEntities) db.prenatal8AncDao().insert8AncRecord(r);
                    for (PrenatalImmunizationEntity r : data.prenatalImmunizationEntities) db.prenatalImmunizationDao().insertImmunizationRecord(r);
                    for (PrenatalSupplementationEntity r : data.prenatalSupplementationEntities) db.prenatalSupplementationDao().insertSupplementationRecord(r);
                    for (PrenatalLabScreeningEntity r : data.prenatalLabScreeningEntities) db.prenatalLabScreeningDao().insertLabScreening(r);
                    for (IntrapartumEntity r : data.intrapartumEntities) db.intrapartumDao().insertIntrapartum(r);
                    for (PostpartumEntity r : data.postpartumEntities) db.postpartumDao().insert(r);

                    // Save Child Health Records
                    for (ChildImmunizationRecord r : data.childImmunizationRecords) db.childImmunizationDao().insert(r);
                    for (ChildImmunizationSchoolRecord r : data.childImmunizationSchoolRecords) db.childImmunizationSchoolDao().insert(r);
                    for (ChildNutritionRecord r : data.childNutritionRecords) db.childNutritionDao().insert(r);
                    for (ChildSickRecord r : data.childSickRecords) db.childSickDao().insert(r);

                    // Save OHC, NCD, and Geriatric Records
                    for (OralHealthCareEntity r : data.oralHealthCareRecords) db.oralHealthCareDao().insert(r);
                    for (PhilPENAssessmentEntity r : data.philpenRiskAssessments) db.philPENDao().insert(r);
                    for (EyesScreeningsData r : data.eyesScreenings) db.eyesScreeningDao().insert(r);
                    for (CervicalCancerScreeningEntity r : data.cervicalCancerScreenings) db.cervicalCancerScreeningDao().insert(r);
                    for (GeriatricScreeningRecord r : data.geriatricScreeningRecords) db.geriatricScreeningDao().insert(r);

                    // Save IDPCS Records
                    for (FilariasisRegistryRecord r : data.filariasisRegistryRecords) db.filariasisDao().insertRecord(r);
                    for (LeprosyRegistryRecord r : data.leprosyRegistryRecords) db.leprosyRegistryDao().insert(r);
                    for (RabiesRecord r : data.rabiesRecords) db.rabiesDao().insertOrUpdate(r);
                    for (SchistosomiasisRegistryRecord r : data.schistosomiasisRegistryRecords) db.schistosomiasisDao().insert(r);
                    for (SoilTransmittedHelminthiasisRegistryRecord r : data.sthRegistryRecords) db.soilTransmittedHelminthiasisDao().insertRecord(r);

                    // --- NEW: Save Mental & Environmental Health ---
                    for (MentalHealthRecord r : data.mentalHealthRecords) db.mentalHealthDao().insert(r);
                    for (EnvironmentalHealthModel r : data.environmentalHealthRecords) db.environmentalHealthDao().insertRecord(r);

                });

                // Persist synced_at for the next delta pull
                if (prefs != null && pullResponse.syncedAt != null) {
                    prefs.edit()
                            .putString(KEY_LAST_SYNCED_AT, pullResponse.syncedAt)
                            .apply();
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    progressOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Pull Success! Local Database Updated.", Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Network Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────
    //  Shared Retrofit builder (reads dynamic base URL from prefs)
    // ─────────────────────────────────────────────────────────────
    private Retrofit buildRetrofit() {
        String baseUrl = DEFAULT_BASE_URL;
        if (getContext() != null) {
            android.content.SharedPreferences sharedPreferences =
                    getContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
            baseUrl = sharedPreferences.getString(KEY_API_BASE_URL, baseUrl);
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  UPLOAD  –  sends only NEW + UPDATED (unsynced) local records
    //  to the server, then marks them as synced once accepted.
    //
    //  NOTE: requires each Dao to expose two new methods:
    //    List<T> getUnsyncedRecords();
    //    void markAsSynced(List<ID> ids);
    //  See DAO_UPLOAD_CHANGES.md for the exact snippets to paste into
    //  each Dao interface, and confirm the "upload" endpoint below
    //  performs an UPSERT server-side (not a full replace like Sync).
    // ─────────────────────────────────────────────────────────────
    private void triggerUpload() {
        new Thread(() -> {
            try {
                updateProgress(5, "Opening Database Connection...");
                DatabaseHelper db = DatabaseHelper.getInstance(getContext());

                updateProgress(15, "Collecting Newly Inserted / Updated Records...");

                List<HouseholdProfile> profiles = db.householdProfileDao().getUnsyncedRecords();
                List<FamilyPlanningRecord> familyPlans = db.familyPlanningDao().getUnsyncedRecords();
                List<ClassificationEntity> classes = db.classificationDao().getUnsyncedRecords();
                List<FollowUpEntity> followUps = db.followUpDao().getUnsyncedRecords();
                List<DropOutEntity> dropOuts = db.dropOutDao().getUnsyncedRecords();

                List<MaternalCareRecord> maternal = db.maternalCareDao().getUnsyncedRecords();
                List<Prenatal8AncEntity> anc = db.prenatal8AncDao().getUnsyncedRecords();
                List<PrenatalImmunizationEntity> immunizations = db.prenatalImmunizationDao().getUnsyncedRecords();
                List<PrenatalSupplementationEntity> supplements = db.prenatalSupplementationDao().getUnsyncedRecords();
                List<PrenatalLabScreeningEntity> labs = db.prenatalLabScreeningDao().getUnsyncedRecords();
                List<IntrapartumEntity> intra = db.intrapartumDao().getUnsyncedRecords();
                List<PostpartumEntity> post = db.postpartumDao().getUnsyncedRecords();

                List<ChildImmunizationRecord> childImmunizations = db.childImmunizationDao().getUnsyncedRecords();
                List<ChildImmunizationSchoolRecord> childImmunizationsSchool = db.childImmunizationSchoolDao().getUnsyncedRecords();
                List<ChildNutritionRecord> childNutrition = db.childNutritionDao().getUnsyncedRecords();
                List<ChildSickRecord> childSick = db.childSickDao().getUnsyncedRecords();

                List<OralHealthCareEntity> ohc = db.oralHealthCareDao().getUnsyncedRecords();
                List<PhilPENAssessmentEntity> philpen = db.philPENDao().getUnsyncedRecords();
                List<EyesScreeningsData> eyes = db.eyesScreeningDao().getUnsyncedRecords();
                List<CervicalCancerScreeningEntity> cervical = db.cervicalCancerScreeningDao().getUnsyncedRecords();
                List<GeriatricScreeningRecord> geriatric = db.geriatricScreeningDao().getUnsyncedRecords();

                List<FilariasisRegistryRecord> filariasis = db.filariasisDao().getUnsyncedRecords();
                List<LeprosyRegistryRecord> leprosy = db.leprosyRegistryDao().getUnsyncedRecords();
                List<RabiesRecord> rabies = db.rabiesDao().getUnsyncedRecords();
                List<SchistosomiasisRegistryRecord> schisto = db.schistosomiasisDao().getUnsyncedRecords();
                List<SoilTransmittedHelminthiasisRegistryRecord> sth = db.soilTransmittedHelminthiasisDao().getUnsyncedRecords();

                List<MentalHealthRecord> mentalHealth = db.mentalHealthDao().getUnsyncedRecords();
                List<EnvironmentalHealthModel> envHealth = db.environmentalHealthDao().getUnsyncedRecords();

                int totalUnsynced = profiles.size() + familyPlans.size() + classes.size() + followUps.size()
                        + dropOuts.size() + maternal.size() + anc.size() + immunizations.size() + supplements.size()
                        + labs.size() + intra.size() + post.size() + childImmunizations.size() + childImmunizationsSchool.size()
                        + childNutrition.size() + childSick.size() + ohc.size() + philpen.size() + eyes.size()
                        + cervical.size() + geriatric.size() + filariasis.size() + leprosy.size() + rabies.size()
                        + schisto.size() + sth.size() + mentalHealth.size() + envHealth.size();

                if (totalUnsynced == 0) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Nothing to upload — everything is already synced.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                updateProgress(50, "Packaging " + totalUnsynced + " New/Updated Record(s)...");

                // Reuses the existing SyncPayload shape — just populated with
                // ONLY the unsynced records instead of the full table dumps.
                SyncPayload payload = new SyncPayload(
                        profiles, familyPlans, classes, followUps, dropOuts,
                        maternal, anc, immunizations, supplements, labs, intra, post,
                        childImmunizations, childImmunizationsSchool, childNutrition, childSick,
                        ohc, philpen, eyes, cervical, geriatric,
                        filariasis, leprosy, rabies, schisto, sth,
                        mentalHealth, envHealth
                );

                updateProgress(75, "Uploading Payload to Server...");

                Retrofit retrofit = buildRetrofit();
                SyncApiService apiService = retrofit.create(SyncApiService.class);
                // NOTE: uploadToServer(...) is a NEW method you add to SyncApiService,
                // pointed at a Laravel route that UPSERTS by id instead of replacing
                // the whole table (unlike pushToServer used by Sync).
                Response<SyncPushResponse> response = apiService.uploadToServer(payload).execute();

                if (response.isSuccessful() && response.body() != null
                        && "success".equalsIgnoreCase(response.body().status)) {

                    updateProgress(90, "Marking Records As Synced Locally...");

                    final int finalTotalUnsynced = totalUnsynced;

                    db.runInTransaction(() -> {
                        db.householdProfileDao().markAsSynced(idsOf(profiles, r -> r.id));
                        db.familyPlanningDao().markAsSynced(idsOf(familyPlans, r -> r.id));
                        db.classificationDao().markAsSynced(idsOf(classes, r -> r.id));
                        db.followUpDao().markAsSynced(idsOf(followUps, r -> r.id));
                        db.dropOutDao().markAsSynced(idsOf(dropOuts, r -> r.id));

                        db.maternalCareDao().markAsSynced(idsOf(maternal, r -> r.id));
                        db.prenatal8AncDao().markAsSynced(idsOf(anc, r -> r.id));
                        db.prenatalImmunizationDao().markAsSynced(idsOf(immunizations, r -> r.id));
                        db.prenatalSupplementationDao().markAsSynced(idsOf(supplements, r -> r.id));
                        db.prenatalLabScreeningDao().markAsSynced(idsOf(labs, r -> r.id));
                        db.intrapartumDao().markAsSynced(idsOf(intra, r -> r.id));
                        db.postpartumDao().markAsSynced(idsOf(post, r -> r.id));

                        db.childImmunizationDao().markAsSynced(idsOf(childImmunizations, ChildImmunizationRecord::getId));
                        db.childImmunizationSchoolDao().markAsSynced(idsOf(childImmunizationsSchool, ChildImmunizationSchoolRecord::getId));
                        db.childNutritionDao().markAsSynced(idsOf(childNutrition, ChildNutritionRecord::getId));
                        db.childSickDao().markAsSynced(idsOf(childSick, ChildSickRecord::getId));

                        db.oralHealthCareDao().markAsSynced(idsOf(ohc, r -> r.id));
                        db.philPENDao().markAsSynced(idsOf(philpen, r -> r.id));
                        db.eyesScreeningDao().markAsSynced(idsOf(eyes, EyesScreeningsData::getId));
                        db.cervicalCancerScreeningDao().markAsSynced(idsOf(cervical, CervicalCancerScreeningEntity::getId));
                        db.geriatricScreeningDao().markAsSynced(idsOf(geriatric, GeriatricScreeningRecord::getRecordNo));

                        db.filariasisDao().markAsSynced(idsOf(filariasis, FilariasisRegistryRecord::getId));
                        db.leprosyRegistryDao().markAsSynced(idsOf(leprosy, LeprosyRegistryRecord::getId));
                        db.rabiesDao().markAsSynced(idsOf(rabies, RabiesRecord::getId));
                        db.schistosomiasisDao().markAsSynced(idsOf(schisto, SchistosomiasisRegistryRecord::getId));
                        db.soilTransmittedHelminthiasisDao().markAsSynced(idsOf(sth, SoilTransmittedHelminthiasisRegistryRecord::getId));

                        db.mentalHealthDao().markAsSynced(idsOf(mentalHealth, MentalHealthRecord::getRecordNo));
                        db.environmentalHealthDao().markAsSynced(idsOf(envHealth, EnvironmentalHealthModel::getId));
                    });

                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Upload Success! " + finalTotalUnsynced + " record(s) uploaded.", Toast.LENGTH_LONG).show();
                    });

                } else if (response.isSuccessful() && response.body() != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Upload Failed: " + response.body().message, Toast.LENGTH_LONG).show();
                    });
                } else {
                    final int code = response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            android.util.Log.e("UPLOAD_ERROR", "Server Upload Crash: " + errorJson);
                        }
                    } catch (Exception ignored) {}
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Upload Failed: Server HTTP Error " + code + ". Check Logcat!", Toast.LENGTH_LONG).show();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Network Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // Small helper: pulls an ID out of each item in a list using any getter/field reference,
    // so markAsSynced(...) can be called without writing a manual loop for every entity.
    private <T, R> List<R> idsOf(List<T> list, java.util.function.Function<T, R> idGetter) {
        List<R> ids = new java.util.ArrayList<>();
        for (T item : list) ids.add(idGetter.apply(item));
        return ids;
    }

    private void updateProgress(int percent, String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (syncProgressBar != null && tvProgressPercent != null) {
                syncProgressBar.setProgress(percent);
                tvProgressPercent.setText(percent + "% — " + message);
            }
        });
    }
}