package com.android.hfsis.child;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.child.ChildNutritionRecord;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ChildNutritionFragment extends Fragment {

    // Record tracking properties
    private long selectedProfileId = -1;
    private long editingRecordId = -1;
    private boolean isEditMode = false;
    private DatabaseHelper database;

    // --- Section 1: Basic Information ---
    private TextInputLayout tilDateRegistration, tilFamilySerialNumber, tilChildName, tilDateOfBirth, tilAge, tilMotherName, tilAddress;
    private TextInputEditText etDateRegistration, etFamilySerialNumber, etDateOfBirth, etAge, etMotherName, etAddress;
    private AutoCompleteTextView etChildName;
    private RadioGroup rgSex;

    // --- Section 2: Newborn Assessment ---
    private TextInputLayout tilLengthAtBirth, tilWeightAtBirth, tilBirthWeightStatus, tilBreastfeedingDate, tilPlaceOfDelivery;
    private TextInputEditText etLengthAtBirth, etWeightAtBirth, etBreastfeedingDate, etPlaceOfDelivery;
    private AutoCompleteTextView etBirthWeightStatus;

    // --- Section 3: Iron Supplementation ---
    private TextInputLayout tilIron1Month, tilIron2Months, tilIron3Months, tilIronCompletedDate;
    private TextInputEditText etIron1Month, etIron2Months, etIron3Months, etIronCompletedDate;
    private RadioGroup rgIronCompleted;

    // --- Section 4: Vitamin A Supplementation ---
    private TextInputLayout tilVitaA6to11, tilVitaA200Y1D1, tilVitaA200Y1D2, tilVitaA200Y2D1, tilVitaA200Y2D2, tilVitaA200Y3D1, tilVitaA200Y3D2, tilVitaA200Y4D1, tilVitaA200Y4D2;
    private TextInputEditText etVitaA6to11, etVitaA200Y1D1, etVitaA200Y1D2, etVitaA200Y2D1, etVitaA200Y2D2, etVitaA200Y3D1, etVitaA200Y3D2, etVitaA200Y4D1, etVitaA200Y4D2;

    // --- Section 5: MNP Supplementation ---
    private TextInputLayout tilMnp6to11Provided, tilMnp6to11Completed, tilMnp6to11Remarks;
    private TextInputLayout tilMnp12to23Provided, tilMnp12to23Completed, tilMnp12to23Remarks;
    private TextInputEditText etMnp6to11Provided, etMnp6to11Completed, etMnp6to11Remarks;
    private TextInputEditText etMnp12to23Provided, etMnp12to23Completed, etMnp12to23Remarks;

    // --- Section 6: LNS-SQ Supplementation ---
    private TextInputLayout tilLns6to11Provided, tilLns6to11Completed, tilLns6to11Remarks;
    private TextInputLayout tilLns12to23Provided, tilLns12to23Completed, tilLns12to23Remarks;
    private TextInputEditText etLns6to11Provided, etLns6to11Completed, etLns6to11Remarks;
    private TextInputEditText etLns12to23Provided, etLns12to23Completed, etLns12to23Remarks;

    // --- Section 7: MAM — SFP ---
    private RadioGroup rgMamIdentified, rgMamEnrolled, rgMamCured, rgMamNonCured, rgMamDefaulted, rgMamDied;

    // --- Section 8: SAM — OTC ---
    private RadioGroup rgSamIdentified, rgSamAdmitted, rgSamCured, rgSamNonCured, rgSamDefaulted, rgSamDied;

    // --- Section 9: Remarks ---
    private TextInputLayout tilRemarks;
    private TextInputEditText etRemarks;

    // Form Action Buttons
    private MaterialButton btnClear, btnSave;
    private TextView tvFormTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_nutrition, container, false);
        database = DatabaseHelper.getInstance(requireContext().getApplicationContext());

        initViews(view);
        setupDropdowns();
        setupDatePickers();

        if (getArguments() != null) {
            long incomingId = getArguments().getLong("recordId", -1);
            if (incomingId == -1) {
                incomingId = getArguments().getLong("EDIT_RECORD_ID", -1);
            }
            if (incomingId != -1) {
                editingRecordId = incomingId;
                isEditMode = true;
                tvFormTitle.setText("UPDATE CHILD NUTRITION ENTRY");
                btnSave.setText("UPDATE ENTRY");
                loadExistingRecord(editingRecordId);
            }
        }else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etDateRegistration.setText(sdf.format(new Date()));
        }

        btnSave.setOnClickListener(v -> saveOrUpdateRecord());
        btnClear.setOnClickListener(v -> clearAllFields());

        return view;
    }

    private void initViews(View view) {
        tvFormTitle = view.findViewById(R.id.tvFormTitle);

        // Section 1
        tilDateRegistration = view.findViewById(R.id.tilDateRegistration);
        tilFamilySerialNumber = view.findViewById(R.id.tilFamilySerialNumber);
        tilChildName = view.findViewById(R.id.tilChildName);
        tilDateOfBirth = view.findViewById(R.id.tilDateOfBirth);
        tilAge = view.findViewById(R.id.tilAge);
        tilMotherName = view.findViewById(R.id.tilMotherName);
        tilAddress = view.findViewById(R.id.tilAddress);

        etDateRegistration = view.findViewById(R.id.etDateRegistration);
        etFamilySerialNumber = view.findViewById(R.id.etFamilySerialNumber);
        etChildName = view.findViewById(R.id.etChildName);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        etAge = view.findViewById(R.id.etAge);
        etMotherName = view.findViewById(R.id.etMotherName);
        etAddress = view.findViewById(R.id.etAddress);
        rgSex = view.findViewById(R.id.rgSex);

        // Section 2
        tilLengthAtBirth = view.findViewById(R.id.tilLengthAtBirth);
        tilWeightAtBirth = view.findViewById(R.id.tilWeightAtBirth);
        tilBirthWeightStatus = view.findViewById(R.id.tilBirthWeightStatus);
        tilBreastfeedingDate = view.findViewById(R.id.tilBreastfeedingDate);
        tilPlaceOfDelivery = view.findViewById(R.id.tilPlaceOfDelivery);

        etLengthAtBirth = view.findViewById(R.id.etLengthAtBirth);
        etWeightAtBirth = view.findViewById(R.id.etWeightAtBirth);
        etBirthWeightStatus = view.findViewById(R.id.actBirthWeightStatus);
        etBreastfeedingDate = view.findViewById(R.id.etBreastfeedingDate);
        etPlaceOfDelivery = view.findViewById(R.id.etPlaceOfDelivery);

        // Section 3
        tilIron1Month = view.findViewById(R.id.tilIron1Month);
        tilIron2Months = view.findViewById(R.id.tilIron2Months);
        tilIron3Months = view.findViewById(R.id.tilIron3Months);
        tilIronCompletedDate = view.findViewById(R.id.tilIronCompletedDate);

        etIron1Month = view.findViewById(R.id.etIron1Month);
        etIron2Months = view.findViewById(R.id.etIron2Months);
        etIron3Months = view.findViewById(R.id.etIron3Months);
        etIronCompletedDate = view.findViewById(R.id.etIronCompletedDate);
        rgIronCompleted = view.findViewById(R.id.rgIronCompleted);

        // Section 4
        tilVitaA6to11 = view.findViewById(R.id.tilVitaA6to11);
        tilVitaA200Y1D1 = view.findViewById(R.id.tilVitaA200Y1D1);
        tilVitaA200Y1D2 = view.findViewById(R.id.tilVitaA200Y1D2);
        tilVitaA200Y2D1 = view.findViewById(R.id.tilVitaA200Y2D1);
        tilVitaA200Y2D2 = view.findViewById(R.id.tilVitaA200Y2D2);
        tilVitaA200Y3D1 = view.findViewById(R.id.tilVitaA200Y3D1);
        tilVitaA200Y3D2 = view.findViewById(R.id.tilVitaA200Y3D2);
        tilVitaA200Y4D1 = view.findViewById(R.id.tilVitaA200Y4D1);
        tilVitaA200Y4D2 = view.findViewById(R.id.tilVitaA200Y4D2);

        etVitaA6to11 = view.findViewById(R.id.etVitaA6to11);
        etVitaA200Y1D1 = view.findViewById(R.id.etVitaA200Y1D1);
        etVitaA200Y1D2 = view.findViewById(R.id.etVitaA200Y1D2);
        etVitaA200Y2D1 = view.findViewById(R.id.etVitaA200Y2D1);
        etVitaA200Y2D2 = view.findViewById(R.id.etVitaA200Y2D2);
        etVitaA200Y3D1 = view.findViewById(R.id.etVitaA200Y3D1);
        etVitaA200Y3D2 = view.findViewById(R.id.etVitaA200Y3D2);
        etVitaA200Y4D1 = view.findViewById(R.id.etVitaA200Y4D1);
        etVitaA200Y4D2 = view.findViewById(R.id.etVitaA200Y4D2);

        // Section 5
        tilMnp6to11Provided = view.findViewById(R.id.tilMnp6to11Provided);
        tilMnp6to11Completed = view.findViewById(R.id.tilMnp6to11Completed);
        tilMnp6to11Remarks = view.findViewById(R.id.tilMnp6to11Remarks);
        tilMnp12to23Provided = view.findViewById(R.id.tilMnp12to23Provided);
        tilMnp12to23Completed = view.findViewById(R.id.tilMnp12to23Completed);
        tilMnp12to23Remarks = view.findViewById(R.id.tilMnp12to23Remarks);

        etMnp6to11Provided = view.findViewById(R.id.etMnp6to11Provided);
        etMnp6to11Completed = view.findViewById(R.id.etMnp6to11Completed);
        etMnp6to11Remarks = view.findViewById(R.id.etMnp6to11Remarks);
        etMnp12to23Provided = view.findViewById(R.id.etMnp12to23Provided);
        etMnp12to23Completed = view.findViewById(R.id.etMnp12to23Completed);
        etMnp12to23Remarks = view.findViewById(R.id.etMnp12to23Remarks);

        // Section 6
        tilLns6to11Provided = view.findViewById(R.id.tilLns6to11Provided);
        tilLns6to11Completed = view.findViewById(R.id.tilLns6to11Completed);
        tilLns6to11Remarks = view.findViewById(R.id.tilLns6to11Remarks);
        tilLns12to23Provided = view.findViewById(R.id.tilLns12to23Provided);
        tilLns12to23Completed = view.findViewById(R.id.tilLns12to23Completed);
        tilLns12to23Remarks = view.findViewById(R.id.tilLns12to23Remarks);

        etLns6to11Provided = view.findViewById(R.id.etLns6to11Provided);
        etLns6to11Completed = view.findViewById(R.id.etLns6to11Completed);
        etLns6to11Remarks = view.findViewById(R.id.etLns6to11Remarks);
        etLns12to23Provided = view.findViewById(R.id.etLns12to23Provided);
        etLns12to23Completed = view.findViewById(R.id.etLns12to23Completed);
        etLns12to23Remarks = view.findViewById(R.id.etLns12to23Remarks);

        // Section 7
        rgMamIdentified = view.findViewById(R.id.rgMamIdentified);
        rgMamEnrolled = view.findViewById(R.id.rgMamEnrolled);
        rgMamCured = view.findViewById(R.id.rgMamCured);
        rgMamNonCured = view.findViewById(R.id.rgMamNonCured);
        rgMamDefaulted = view.findViewById(R.id.rgMamDefaulted);
        rgMamDied = view.findViewById(R.id.rgMamDied);

        // Section 8
        rgSamIdentified = view.findViewById(R.id.rgSamIdentified);
        rgSamAdmitted = view.findViewById(R.id.rgSamAdmitted);
        rgSamCured = view.findViewById(R.id.rgSamCured);
        rgSamNonCured = view.findViewById(R.id.rgSamNonCured);
        rgSamDefaulted = view.findViewById(R.id.rgSamDefaulted);
        rgSamDied = view.findViewById(R.id.rgSamDied);

        // Section 9
        tilRemarks = view.findViewById(R.id.tilRemarks);
        etRemarks = view.findViewById(R.id.etRemarks);

        btnClear = view.findViewById(R.id.btnClear);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupDropdowns() {
        // 1. Existing Birth Weight Status
        String[] weightStatuses = {"L — Low Birth Weight", "N — Normal", "U — Unknown"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, weightStatuses);
        etBirthWeightStatus.setAdapter(adapter);

        // 2. Bind etChildName Autocomplete using HouseholdProfileDao
        if (database != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                List<String> householdNames = database.householdProfileDao().getAllHouseholdNames();

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                householdNames
                        );
                        etChildName.setAdapter(nameAdapter);

                        etChildName.setOnItemClickListener((parent, view, position, id) -> {
                            String selectedName = (String) parent.getItemAtPosition(position);

                            Executors.newSingleThreadExecutor().execute(() -> {
                                HouseholdProfile profile = database.householdProfileDao().getProfileByCalculatedName(selectedName);

                                if (profile != null && isAdded() && getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        selectedProfileId = profile.id;
                                        etFamilySerialNumber.setText(profile.hhNumber);

                                        // Auto-fill DOB and auto-calculate Age
                                        if (TextUtils.isEmpty(getText(etDateOfBirth)) && !TextUtils.isEmpty(profile.dob)) {
                                            etDateOfBirth.setText(profile.dob);
                                            calculateAgeInMonths(profile.dob);
                                        }

                                        if (rgSex.getCheckedRadioButtonId() == -1 && !TextUtils.isEmpty(profile.sex)) {
                                            if (profile.sex.equalsIgnoreCase("Male") || profile.sex.equalsIgnoreCase("M")) {
                                                rgSex.check(R.id.rbMale);
                                            } else if (profile.sex.equalsIgnoreCase("Female") || profile.sex.equalsIgnoreCase("F")) {
                                                rgSex.check(R.id.rbFemale);
                                            }
                                        }

                                        String sitioPart = (profile.sitio != null && !profile.sitio.isEmpty()) ? profile.sitio : "";
                                        String brgyPart = (profile.barangay != null) ? ", " + profile.barangay : "";
                                        String munPart = (profile.municipality != null) ? ", " + profile.municipality : "";
                                        String provPart = (profile.province != null) ? ", " + profile.province : "";
                                        String regPart = (profile.region != null) ? ", " + profile.region : "";
                                        etAddress.setText(sitioPart + brgyPart + munPart + provPart + regPart);

                                    });
                                }
                            });
                        });
                    });
                }
            });
        }
    }

    private void setupDatePickers() {
        View.OnClickListener dateClick = v -> {
            final EditText target = (EditText) v;
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(requireContext(), (view, year1, month1, dayOfMonth) -> {
                String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                target.setText(formattedDate);

                // Instantly calculate age if the target field is Date of Birth
                if (target.getId() == R.id.etDateOfBirth) {
                    calculateAgeInMonths(formattedDate);
                }
            }, year, month, day).show();
        };

        etDateRegistration.setOnClickListener(dateClick);
        etDateOfBirth.setOnClickListener(dateClick);
        etBreastfeedingDate.setOnClickListener(dateClick);
        etIronCompletedDate.setOnClickListener(dateClick);
        etIron1Month.setOnClickListener(dateClick);
        etIron2Months.setOnClickListener(dateClick);
        etIron3Months.setOnClickListener(dateClick);
        etVitaA6to11.setOnClickListener(dateClick);
        etVitaA200Y1D1.setOnClickListener(dateClick);
        etVitaA200Y1D2.setOnClickListener(dateClick);
        etVitaA200Y2D1.setOnClickListener(dateClick);
        etVitaA200Y2D2.setOnClickListener(dateClick);
        etVitaA200Y3D1.setOnClickListener(dateClick);
        etVitaA200Y3D2.setOnClickListener(dateClick);
        etVitaA200Y4D1.setOnClickListener(dateClick);
        etVitaA200Y4D2.setOnClickListener(dateClick);
        etMnp6to11Provided.setOnClickListener(dateClick);
        etMnp6to11Completed.setOnClickListener(dateClick);
        etMnp12to23Provided.setOnClickListener(dateClick);
        etMnp12to23Completed.setOnClickListener(dateClick);
        etLns6to11Provided.setOnClickListener(dateClick);
        etLns6to11Completed.setOnClickListener(dateClick);
        etLns12to23Provided.setOnClickListener(dateClick);
        etLns12to23Completed.setOnClickListener(dateClick);
    }

    private void calculateAgeInMonths(String dobString) {
        if (TextUtils.isEmpty(dobString)) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthDate = sdf.parse(dobString);
            if (birthDate != null) {
                Calendar birth = Calendar.getInstance();
                birth.setTime(birthDate);
                Calendar today = Calendar.getInstance();

                int months = (today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)) * 12
                        + today.get(Calendar.MONTH) - birth.get(Calendar.MONTH);

                // Adjust if the current day of the month is before the birth day of the month
                if (today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)) {
                    months--;
                }
                etAge.setText(String.valueOf(Math.max(0, months)));
            }
        } catch (Exception ignored) {}
    }

    private void loadExistingRecord(long id) {
        if (database == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            ChildNutritionRecord record = database.childNutritionDao().getAllRecords().stream()
                    .filter(r -> r.getId() == id).findFirst().orElse(null);

            if (record != null && isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    selectedProfileId = record.getProfileId();
                    etDateRegistration.setText(record.getDateRegistration());
                    etFamilySerialNumber.setText(record.getFamilySerialNumber());
                    etChildName.setText(record.getChildName());
                    etDateOfBirth.setText(record.getDateOfBirth());
                    etAge.setText(record.getAgeMonths());
                    etMotherName.setText(record.getMotherName());
                    etAddress.setText(record.getAddress());

                    if ("M".equalsIgnoreCase(record.getSex())) rgSex.check(R.id.rbMale);
                    else if ("F".equalsIgnoreCase(record.getSex())) rgSex.check(R.id.rbFemale);

                    etLengthAtBirth.setText(record.getLengthAtBirth());
                    etWeightAtBirth.setText(record.getWeightAtBirth());

                    String bwStatus = record.getBirthWeightStatus();
                    if (!TextUtils.isEmpty(bwStatus)) {
                        if ("L".equalsIgnoreCase(bwStatus)) etBirthWeightStatus.setText("L — Low Birth Weight", false);
                        else if ("N".equalsIgnoreCase(bwStatus)) etBirthWeightStatus.setText("N — Normal", false);
                        else if ("U".equalsIgnoreCase(bwStatus)) etBirthWeightStatus.setText("U — Unknown", false);
                    }

                    etBreastfeedingDate.setText(record.getBreastfeedingDate());
                    etPlaceOfDelivery.setText(record.getPlaceOfDelivery());

                    etIron1Month.setText(record.getIron1Month());
                    etIron2Months.setText(record.getIron2Months());
                    etIron3Months.setText(record.getIron3Months());
                    setRadioValue(rgIronCompleted, record.getIronCompleted());
                    etIronCompletedDate.setText(record.getIronCompletedDate());

                    etVitaA6to11.setText(record.getVitaA6to11());
                    etVitaA200Y1D1.setText(record.getVitaA200Y1D1());
                    etVitaA200Y1D2.setText(record.getVitaA200Y1D2());
                    etVitaA200Y2D1.setText(record.getVitaA200Y2D1());
                    etVitaA200Y2D2.setText(record.getVitaA200Y2D2());
                    etVitaA200Y3D1.setText(record.getVitaA200Y3D1());
                    etVitaA200Y3D2.setText(record.getVitaA200Y3D2());
                    etVitaA200Y4D1.setText(record.getVitaA200Y4D1());
                    etVitaA200Y4D2.setText(record.getVitaA200Y4D2());

                    etMnp6to11Provided.setText(record.getMnp6to11Provided());
                    etMnp6to11Completed.setText(record.getMnp6to11Completed());
                    etMnp6to11Remarks.setText(record.getMnp6to11Remarks());
                    etMnp12to23Provided.setText(record.getMnp12to23Provided());
                    etMnp12to23Completed.setText(record.getMnp12to23Completed());
                    etMnp12to23Remarks.setText(record.getMnp12to23Remarks());

                    etLns6to11Provided.setText(record.getLns6to11Provided());
                    etLns6to11Completed.setText(record.getLns6to11Completed());
                    etLns6to11Remarks.setText(record.getLns6to11Remarks());
                    etLns12to23Provided.setText(record.getLns12to23Provided());
                    etLns12to23Completed.setText(record.getLns12to23Completed());
                    etLns12to23Remarks.setText(record.getLns12to23Remarks());

                    setRadioValue(rgMamIdentified, record.getMamIdentified());
                    setRadioValue(rgMamEnrolled, record.getMamEnrolled());
                    setRadioValue(rgMamCured, record.getMamCured());
                    setRadioValue(rgMamNonCured, record.getMamNonCured());
                    setRadioValue(rgMamDefaulted, record.getMamDefaulted());
                    setRadioValue(rgMamDied, record.getMamDied());

                    setRadioValue(rgSamIdentified, record.getSamIdentified());
                    setRadioValue(rgSamAdmitted, record.getSamAdmitted());
                    setRadioValue(rgSamCured, record.getSamCured());
                    setRadioValue(rgSamNonCured, record.getSamNonCured());
                    setRadioValue(rgSamDefaulted, record.getSamDefaulted());
                    setRadioValue(rgSamDied, record.getSamDied());

                    etRemarks.setText(record.getRemarks());
                });
            }
        });
    }

    private void saveOrUpdateRecord() {
        if (TextUtils.isEmpty(getText(etChildName))) {
            tilChildName.setError("Child Name is required");
            return;
        }
        tilChildName.setError(null);

        Executors.newSingleThreadExecutor().execute(() -> {
            ChildNutritionRecord record = new ChildNutritionRecord();
            if (isEditMode) {
                record.setId(editingRecordId);
            }

            String PREFS_NAME = "AppPrefs";
            SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            record.userId = userId;
            record.setProfileId(selectedProfileId);
            record.setDateRegistration(getText(etDateRegistration));
            record.setFamilySerialNumber(getText(etFamilySerialNumber));
            record.setChildName(getText(etChildName));
            record.setDateOfBirth(getText(etDateOfBirth));
            record.setAgeMonths(getText(etAge));
            record.setMotherName(getText(etMotherName));
            record.setAddress(getText(etAddress));

            int sexId = rgSex.getCheckedRadioButtonId();
            if (sexId == R.id.rbMale) record.setSex("M");
            else if (sexId == R.id.rbFemale) record.setSex("F");

            record.setLengthAtBirth(getText(etLengthAtBirth));
            record.setWeightAtBirth(getText(etWeightAtBirth));
            record.setBirthWeightStatus(getBirthWeightCode(getText(etBirthWeightStatus)));
            record.setBreastfeedingDate(getText(etBreastfeedingDate));
            record.setPlaceOfDelivery(getText(etPlaceOfDelivery));

            record.setIron1Month(getText(etIron1Month));
            record.setIron2Months(getText(etIron2Months));
            record.setIron3Months(getText(etIron3Months));
            record.setIronCompleted(getRadioValue(rgIronCompleted));
            record.setIronCompletedDate(getText(etIronCompletedDate));

            record.setVitaA6to11(getText(etVitaA6to11));
            record.setVitaA200Y1D1(getText(etVitaA200Y1D1));
            record.setVitaA200Y1D2(getText(etVitaA200Y1D2));
            record.setVitaA200Y2D1(getText(etVitaA200Y2D1));
            record.setVitaA200Y2D2(getText(etVitaA200Y2D2));
            record.setVitaA200Y3D1(getText(etVitaA200Y3D1));
            record.setVitaA200Y3D2(getText(etVitaA200Y3D2));
            record.setVitaA200Y4D1(getText(etVitaA200Y4D1));
            record.setVitaA200Y4D2(getText(etVitaA200Y4D2));

            record.setMnp6to11Provided(getText(etMnp6to11Provided));
            record.setMnp6to11Completed(getText(etMnp6to11Completed));
            record.setMnp6to11Remarks(getText(etMnp6to11Remarks));
            record.setMnp12to23Provided(getText(etMnp12to23Provided));
            record.setMnp12to23Completed(getText(etMnp12to23Completed));
            record.setMnp12to23Remarks(getText(etMnp12to23Remarks));

            record.setLns6to11Provided(getText(etLns6to11Provided));
            record.setLns6to11Completed(getText(etLns6to11Completed));
            record.setLns6to11Remarks(getText(etLns6to11Remarks));
            record.setLns12to23Provided(getText(etLns12to23Provided));
            record.setLns12to23Completed(getText(etLns12to23Completed));
            record.setLns12to23Remarks(getText(etLns12to23Remarks));

            record.setMamIdentified(getRadioValue(rgMamIdentified));
            record.setMamEnrolled(getRadioValue(rgMamEnrolled));
            record.setMamCured(getRadioValue(rgMamCured));
            record.setMamNonCured(getRadioValue(rgMamNonCured));
            record.setMamDefaulted(getRadioValue(rgMamDefaulted));
            record.setMamDied(getRadioValue(rgMamDied));

            record.setSamIdentified(getRadioValue(rgSamIdentified));
            record.setSamAdmitted(getRadioValue(rgSamAdmitted));
            record.setSamCured(getRadioValue(rgSamCured));
            record.setSamNonCured(getRadioValue(rgSamNonCured));
            record.setSamDefaulted(getRadioValue(rgSamDefaulted));
            record.setSamDied(getRadioValue(rgSamDied));

            record.setRemarks(getText(etRemarks));

            if (database != null) {
                if (isEditMode) {
                    database.childNutritionDao().update(record);
                } else {
                    database.childNutritionDao().insert(record);
                }

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), isEditMode ? "Record updated successfully!" : "Record saved successfully!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            }
        });
    }

    private void clearAllFields() {
        clearField(etDateRegistration, tilDateRegistration);
        clearField(etFamilySerialNumber, tilFamilySerialNumber);
        clearField(etChildName, tilChildName);
        clearField(etDateOfBirth, tilDateOfBirth);
        clearField(etAge, tilAge);
        clearField(etMotherName, tilMotherName);
        clearField(etAddress, tilAddress);
        rgSex.clearCheck();

        clearField(etLengthAtBirth, tilLengthAtBirth);
        clearField(etWeightAtBirth, tilWeightAtBirth);
        clearField(etBirthWeightStatus, tilBirthWeightStatus);
        clearField(etBreastfeedingDate, tilBreastfeedingDate);
        clearField(etPlaceOfDelivery, tilPlaceOfDelivery);

        clearField(etIron1Month, tilIron1Month);
        clearField(etIron2Months, tilIron2Months);
        clearField(etIron3Months, tilIron3Months);
        rgIronCompleted.clearCheck();
        clearField(etIronCompletedDate, tilIronCompletedDate);

        clearField(etVitaA6to11, tilVitaA6to11);
        clearField(etVitaA200Y1D1, tilVitaA200Y1D1);
        clearField(etVitaA200Y1D2, tilVitaA200Y1D2);
        clearField(etVitaA200Y2D1, tilVitaA200Y2D1);
        clearField(etVitaA200Y2D2, tilVitaA200Y2D2);
        clearField(etVitaA200Y3D1, tilVitaA200Y3D1);
        clearField(etVitaA200Y3D2, tilVitaA200Y3D2);
        clearField(etVitaA200Y4D1, tilVitaA200Y4D1);
        clearField(etVitaA200Y4D2, tilVitaA200Y4D2);

        clearField(etMnp6to11Provided, tilMnp6to11Provided);
        clearField(etMnp6to11Completed, tilMnp6to11Completed);
        clearField(etMnp6to11Remarks, tilMnp6to11Remarks);
        clearField(etMnp12to23Provided, tilMnp12to23Provided);
        clearField(etMnp12to23Completed, tilMnp12to23Completed);
        clearField(etMnp12to23Remarks, tilMnp12to23Remarks);

        clearField(etLns6to11Provided, tilLns6to11Provided);
        clearField(etLns6to11Completed, tilLns6to11Completed);
        clearField(etLns6to11Remarks, tilLns6to11Remarks);
        clearField(etLns12to23Provided, tilLns12to23Provided);
        clearField(etLns12to23Completed, tilLns12to23Completed);
        clearField(etLns12to23Remarks, tilLns12to23Remarks);

        rgMamIdentified.clearCheck();
        rgMamEnrolled.clearCheck();
        rgMamCured.clearCheck();
        rgMamNonCured.clearCheck();
        rgMamDefaulted.clearCheck();
        rgMamDied.clearCheck();

        rgSamIdentified.clearCheck();
        rgSamAdmitted.clearCheck();
        rgSamCured.clearCheck();
        rgSamNonCured.clearCheck();
        rgSamDefaulted.clearCheck();
        rgSamDied.clearCheck();

        clearField(etRemarks, tilRemarks);
    }

    private String getText(EditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void clearField(EditText field, TextInputLayout layout) {
        field.setText("");
        if (layout != null) layout.setError(null);
    }

    private int getRadioValue(RadioGroup group) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == -1) return -1;
        RadioButton rb = group.findViewById(checkedId);
        if (rb == null) return -1;
        String label = rb.getText().toString();
        try { return Integer.parseInt(String.valueOf(label.charAt(0))); }
        catch (NumberFormatException e) { return -1; }
    }

    private void setRadioValue(RadioGroup group, int value) {
        if (value == -1) {
            group.clearCheck();
            return;
        }
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                String label = rb.getText().toString();
                if (!label.isEmpty() && label.startsWith(String.valueOf(value))) {
                    rb.setChecked(true);
                    return;
                }
            }
        }
    }

    private String getBirthWeightCode(String displayText) {
        if (displayText == null || displayText.isEmpty()) return "";
        return String.valueOf(displayText.charAt(0));
    }
}