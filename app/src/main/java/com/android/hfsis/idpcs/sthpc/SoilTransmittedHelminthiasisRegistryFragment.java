package com.android.hfsis.idpcs.sthpc;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.idpcs.sthpc.SoilTransmittedHelminthiasisRegistryRecord;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SoilTransmittedHelminthiasisRegistryFragment extends Fragment {

    private static final String ARG_RECORD_ID = "record_id";
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private EditText etDateOfRegistration, etFamilySerialNumber, etAddress, etDateOfBirth, etAge, etDateOfScreening, etDateOfResult, etTreatmentDateGiven, etJanuaryMdaDate, etJulyMdaDate, etRemarks;
    private TextInputLayout tilDateOfRegistration, tilDateOfBirth, tilDateOfScreening, tilDateOfResult, tilTreatmentDateGiven, tilJanuaryMdaDate, tilJulyMdaDate;
    private AutoCompleteTextView etName;
    private RadioGroup rgResidency, rgSex, rgScreened, rgJanuaryMdaModality, rgJulyMdaModality;
    private Spinner spinnerAgeClassification, spinnerScreeningResult, spinnerTreatmentGiven;
    private Button btnSave;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    private SoilTransmittedHelminthiasisRegistryRecord existingRecord = null;
    private int recordId = -1;
    private int selectedProfileId = 0;

    public SoilTransmittedHelminthiasisRegistryFragment() {}

    public static SoilTransmittedHelminthiasisRegistryFragment newInstance() {
        return new SoilTransmittedHelminthiasisRegistryFragment();
    }

    public static SoilTransmittedHelminthiasisRegistryFragment newInstance(int recordId) {
        SoilTransmittedHelminthiasisRegistryFragment fragment = new SoilTransmittedHelminthiasisRegistryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RECORD_ID, recordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recordId = getArguments().getInt(ARG_RECORD_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_soil_transmitted_helminthiasis_registry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupNameAutocomplete();
        setupAgeClassificationSpinner();
        setupScreeningResultSpinner();
        setupTreatmentGivenSpinner();
        setupDatePickers();
        setupDewormingEligibilitySync();
        setupSaveButton();

        if (recordId != -1) {
            loadRecordData(recordId);
        } else {
            // Automatically set the current date for a new registry record
            etDateOfRegistration.setText(dateFormat.format(new Date()));
        }
    }

    private void bindViews(View view) {
        tilDateOfRegistration = view.findViewById(R.id.tilDateOfRegistration);
        etDateOfRegistration = view.findViewById(R.id.etDateOfRegistration);
        etFamilySerialNumber = view.findViewById(R.id.etFamilySerialNumber);
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        rgResidency = view.findViewById(R.id.rgResidency);

        tilDateOfBirth = view.findViewById(R.id.tilDateOfBirth);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        etAge = view.findViewById(R.id.etAge);
        spinnerAgeClassification = view.findViewById(R.id.spinnerAgeClassification);
        rgSex = view.findViewById(R.id.rgSex);
        rgScreened = view.findViewById(R.id.rgScreened);

        tilDateOfScreening = view.findViewById(R.id.tilDateOfScreening);
        etDateOfScreening = view.findViewById(R.id.etDateOfScreening);
        spinnerScreeningResult = view.findViewById(R.id.spinnerScreeningResult);

        tilDateOfResult = view.findViewById(R.id.tilDateOfResult);
        etDateOfResult = view.findViewById(R.id.etDateOfResult);
        spinnerTreatmentGiven = view.findViewById(R.id.spinnerTreatmentGiven);

        tilTreatmentDateGiven = view.findViewById(R.id.tilTreatmentDateGiven);
        etTreatmentDateGiven = view.findViewById(R.id.etTreatmentDateGiven);

        tilJanuaryMdaDate = view.findViewById(R.id.tilJanuaryMdaDate);
        etJanuaryMdaDate = view.findViewById(R.id.etJanuaryMdaDate);
        rgJanuaryMdaModality = view.findViewById(R.id.rgJanuaryMdaModality);

        tilJulyMdaDate = view.findViewById(R.id.tilJulyMdaDate);
        etJulyMdaDate = view.findViewById(R.id.etJulyMdaDate);
        rgJulyMdaModality = view.findViewById(R.id.rgJulyMdaModality);

        etRemarks = view.findViewById(R.id.etRemarks);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupNameAutocomplete() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> namesList = dbHelper.householdProfileDao().getAllHouseholdNames();
            if (namesList != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            namesList
                    );
                    etName.setAdapter(adapter);
                });
            }
        });

        etName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            autoPopulateFromProfile(selectedName);
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    selectedProfileId = 0;
                }
            }
        });
    }

    private void autoPopulateFromProfile(String fullCalculatedName) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = dbHelper.householdProfileDao().getProfileByCalculatedName(fullCalculatedName);
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    selectedProfileId = profile.id; // Profile ID saved

                    StringBuilder fullAddress = new StringBuilder();
                    if (profile.sitio != null && !profile.sitio.trim().isEmpty()) {
                        fullAddress.append(profile.sitio.trim());
                    }
                    if (profile.barangay != null && !profile.barangay.trim().isEmpty()) {
                        fullAddress.append(", ").append(profile.barangay.trim());
                    }
                    if (profile.municipality != null && !profile.municipality.trim().isEmpty()) {
                        fullAddress.append(", ").append(profile.municipality.trim());
                    }
                    if (profile.province != null && !profile.province.trim().isEmpty()) {
                        fullAddress.append(", ").append(profile.province.trim());
                    }
                    if (profile.region != null && !profile.region.trim().isEmpty()) {
                        fullAddress.append(", ").append(profile.region.trim());
                    }
                    etAddress.setText(fullAddress);

                    if (!TextUtils.isEmpty(profile.hhNumber)) {
                        etFamilySerialNumber.setText(profile.hhNumber);
                    }

                    if ("M".equalsIgnoreCase(profile.sex) || "Male".equalsIgnoreCase(profile.sex)) {
                        rgSex.check(R.id.rbMale);
                    } else if ("F".equalsIgnoreCase(profile.sex) || "Female".equalsIgnoreCase(profile.sex)) {
                        rgSex.check(R.id.rbFemale);
                    }

                    if (!TextUtils.isEmpty(profile.dob)) {
                        try {
                            // Parse YYYY-MM-DD
                            String[] dobParts = profile.dob.split("-");
                            if (dobParts.length == 3) {
                                int year = Integer.parseInt(dobParts[0]);
                                int month = Integer.parseInt(dobParts[1]) - 1; // Calendar month is 0-indexed
                                int day = Integer.parseInt(dobParts[2]);

                                Calendar dobCal = Calendar.getInstance();
                                dobCal.set(year, month, day);
                                etDateOfBirth.setText(dateFormat.format(dobCal.getTime()));
                                updateAgeFromDateOfBirth(dobCal);
                            } else {
                                etDateOfBirth.setText(profile.dob);
                            }
                        } catch (Exception e) {
                            etDateOfBirth.setText(profile.dob);
                        }
                    }
                });
            }
        });
    }

    private void setupAgeClassificationSpinner() {
        List<String> ageGroups = new ArrayList<>();
        ageGroups.add("A: 1-4 years old");
        ageGroups.add("B: 5-14 years old");
        ageGroups.add("C: 15-19 years old");
        ageGroups.add("D: 20-59 years old");
        ageGroups.add("E: 60 years old and above");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ageGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgeClassification.setAdapter(adapter);
    }

    private void setupScreeningResultSpinner() {
        List<String> results = new ArrayList<>();
        results.add("— Select result —");
        results.add("0: Negative");
        results.add("1: Suspected");
        results.add("2: Positive");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, results);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerScreeningResult.setAdapter(adapter);
    }

    private void setupTreatmentGivenSpinner() {
        List<String> treatments = new ArrayList<>();
        treatments.add("— Select treatment —");
        treatments.add("0: None");
        treatments.add("1: Albendazole");
        treatments.add("2: Mebendazole");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, treatments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTreatmentGiven.setAdapter(adapter);
    }

    private void setupDatePickers() {
        tilDateOfRegistration.setStartIconOnClickListener(v -> showDatePicker(etDateOfRegistration));
        tilDateOfBirth.setStartIconOnClickListener(v -> showDatePicker(etDateOfBirth));
        tilDateOfScreening.setStartIconOnClickListener(v -> showDatePicker(etDateOfScreening));
        tilDateOfResult.setStartIconOnClickListener(v -> showDatePicker(etDateOfResult));
        tilTreatmentDateGiven.setStartIconOnClickListener(v -> showDatePicker(etTreatmentDateGiven));
        tilJanuaryMdaDate.setStartIconOnClickListener(v -> showDatePicker(etJanuaryMdaDate));
        tilJulyMdaDate.setStartIconOnClickListener(v -> showDatePicker(etJulyMdaDate));

        etDateOfRegistration.addTextChangedListener(new DateFormattingWatcher(etDateOfRegistration));
        etDateOfBirth.addTextChangedListener(new DateFormattingWatcher(etDateOfBirth));
        etDateOfScreening.addTextChangedListener(new DateFormattingWatcher(etDateOfScreening));
        etDateOfResult.addTextChangedListener(new DateFormattingWatcher(etDateOfResult));
        etTreatmentDateGiven.addTextChangedListener(new DateFormattingWatcher(etTreatmentDateGiven));
        etJanuaryMdaDate.addTextChangedListener(new DateFormattingWatcher(etJanuaryMdaDate));
        etJulyMdaDate.addTextChangedListener(new DateFormattingWatcher(etJulyMdaDate));

        etDateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String dateStr = s.toString();
                if (isValidDateFormat(dateStr)) {
                    try {
                        Calendar dobCal = Calendar.getInstance();
                        java.util.Date date = dateFormat.parse(dateStr);
                        if (date != null) {
                            dobCal.setTime(date);
                            updateAgeFromDateOfBirth(dobCal);
                        }
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    target.setText(dateFormat.format(selected.getTime()));
                    target.setError(null);

                    if (target == etDateOfBirth) {
                        updateAgeFromDateOfBirth(selected);
                    }
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void updateAgeFromDateOfBirth(Calendar dob) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        if (age < 0) age = 0;
        etAge.setText(String.valueOf(age));
        spinnerAgeClassification.setSelection(ageClassificationIndexFor(age));
    }

    private int ageClassificationIndexFor(int age) {
        if (age >= 1 && age <= 4) return 0;
        if (age <= 14) return 1;
        if (age <= 19) return 2;
        if (age <= 59) return 3;
        return 4;
    }

    private void setupDewormingEligibilitySync() {
        spinnerAgeClassification.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                setDewormingSectionEnabled(position <= 2);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setDewormingSectionEnabled(boolean enabled) {
        tilJanuaryMdaDate.setEnabled(enabled);
        tilJulyMdaDate.setEnabled(enabled);
        etJanuaryMdaDate.setEnabled(enabled);
        etJulyMdaDate.setEnabled(enabled);
        setRadioGroupEnabled(rgJanuaryMdaModality, enabled);
        setRadioGroupEnabled(rgJulyMdaModality, enabled);
        if (!enabled) {
            etJanuaryMdaDate.setText("");
            etJulyMdaDate.setText("");
            rgJanuaryMdaModality.clearCheck();
            rgJulyMdaModality.clearCheck();
        }
    }

    private void setRadioGroupEnabled(RadioGroup group, boolean enabled) {
        group.setEnabled(enabled);
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(enabled);
        }
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                SoilTransmittedHelminthiasisRegistryRecord record = (existingRecord != null) ? existingRecord : new SoilTransmittedHelminthiasisRegistryRecord();

                String PREFS_NAME = "AppPrefs";
                SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);
                record.setUserId(userId);

                populateRecordFromForm(record);
                saveRecordToDatabase(record);
            }
        });
    }

    private boolean isValidDateFormat(String date) {
        // Enforce YYYY-MM-DD
        return date.matches("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])");
    }

    private boolean validateForm() {
        String regDate = etDateOfRegistration.getText().toString().trim();
        if (TextUtils.isEmpty(regDate)) return toastError("Please select the date of registration");
        if (!isValidDateFormat(regDate)) { etDateOfRegistration.setError("Required format is YYYY-MM-DD"); etDateOfRegistration.requestFocus(); return false; }

        if (TextUtils.isEmpty(etFamilySerialNumber.getText())) { etFamilySerialNumber.setError("Required"); return false; }
        if (TextUtils.isEmpty(etName.getText())) { etName.setError("Required"); return false; }
        if (TextUtils.isEmpty(etAddress.getText())) { etAddress.setError("Required"); return false; }
        if (rgResidency.getCheckedRadioButtonId() == -1) return toastError("Please select residency");

        String dobDate = etDateOfBirth.getText().toString().trim();
        if (TextUtils.isEmpty(dobDate)) return toastError("Please select date of birth");
        if (!isValidDateFormat(dobDate)) { etDateOfBirth.setError("Required format is YYYY-MM-DD"); etDateOfBirth.requestFocus(); return false; }

        if (TextUtils.isEmpty(etAge.getText())) { etAge.setError("Required"); return false; }
        if (rgSex.getCheckedRadioButtonId() == -1) return toastError("Please select sex");
        if (rgScreened.getCheckedRadioButtonId() == -1) return toastError("Please indicate screening confirmation status");

        String screeningDate = etDateOfScreening.getText().toString().trim();
        if (!screeningDate.isEmpty() && !isValidDateFormat(screeningDate)) { etDateOfScreening.setError("Invalid format (YYYY-MM-DD)"); etDateOfScreening.requestFocus(); return false; }

        String resultDate = etDateOfResult.getText().toString().trim();
        if (!resultDate.isEmpty() && !isValidDateFormat(resultDate)) { etDateOfResult.setError("Invalid format (YYYY-MM-DD)"); etDateOfResult.requestFocus(); return false; }

        String treatmentDate = etTreatmentDateGiven.getText().toString().trim();
        if (!treatmentDate.isEmpty() && !isValidDateFormat(treatmentDate)) { etTreatmentDateGiven.setError("Invalid format (YYYY-MM-DD)"); etTreatmentDateGiven.requestFocus(); return false; }

        String janMdaDate = etJanuaryMdaDate.getText().toString().trim();
        if (!janMdaDate.isEmpty() && !isValidDateFormat(janMdaDate)) { etJanuaryMdaDate.setError("Invalid format (YYYY-MM-DD)"); etJanuaryMdaDate.requestFocus(); return false; }

        String julMdaDate = etJulyMdaDate.getText().toString().trim();
        if (!julMdaDate.isEmpty() && !isValidDateFormat(julMdaDate)) { etJulyMdaDate.setError("Invalid format (YYYY-MM-DD)"); etJulyMdaDate.requestFocus(); return false; }

        return true;
    }

    private boolean toastError(String text) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void populateRecordFromForm(SoilTransmittedHelminthiasisRegistryRecord record) {
        // Ensure profile ID is mapped
        record.setProfileId(selectedProfileId);
        record.setDateOfRegistration(etDateOfRegistration.getText().toString());
        record.setFamilySerialNumber(etFamilySerialNumber.getText().toString().trim());
        record.setName(etName.getText().toString().trim());
        record.setAddress(etAddress.getText().toString().trim());
        record.setResidency(rgResidency.getCheckedRadioButtonId() == R.id.rbResident ? "1" : "0");
        record.setDateOfBirth(etDateOfBirth.getText().toString());

        int age = 0;
        try { age = Integer.parseInt(etAge.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        record.setAge(age);

        String[] ageClassCodes = {"A", "B", "C", "D", "E"};
        record.setAgeClassification(ageClassCodes[spinnerAgeClassification.getSelectedItemPosition()]);
        record.setSex(rgSex.getCheckedRadioButtonId() == R.id.rbMale ? "M" : "F");
        record.setScreened(rgScreened.getCheckedRadioButtonId() == R.id.rbScreenedYes ? "1" : "0");
        record.setDateOfScreening(etDateOfScreening.getText().toString());
        record.setScreeningResult(spinnerSelectionCode(spinnerScreeningResult, new String[]{null, "0", "1", "2"}));
        record.setDateOfResult(etDateOfResult.getText().toString());
        record.setTreatmentGiven(spinnerSelectionCode(spinnerTreatmentGiven, new String[]{null, "0", "1", "2"}));
        record.setTreatmentDateGiven(etTreatmentDateGiven.getText().toString());
        record.setJanuaryMdaDate(etJanuaryMdaDate.getText().toString());
        record.setJanuaryMdaModality(modalityCode(rgJanuaryMdaModality, R.id.rbJanuarySchoolBased));
        record.setJulyMdaDate(etJulyMdaDate.getText().toString());
        record.setJulyMdaModality(modalityCode(rgJulyMdaModality, R.id.rbJulySchoolBased));
        record.setRemarks(etRemarks.getText().toString().trim());
    }

    private String spinnerSelectionCode(Spinner spinner, String[] codes) {
        int pos = spinner.getSelectedItemPosition();
        return (pos >= 0 && pos < codes.length) ? codes[pos] : null;
    }

    private String modalityCode(RadioGroup group, int schoolId) {
        int checkId = group.getCheckedRadioButtonId();
        if (checkId == -1) return null;
        return (checkId == schoolId) ? "1" : "2";
    }

    private void saveRecordToDatabase(SoilTransmittedHelminthiasisRegistryRecord record) {
        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
            if (record.getId() == 0) {
                db.soilTransmittedHelminthiasisDao().insertRecord(record);
            } else {
                db.soilTransmittedHelminthiasisDao().updateRecord(record);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Record saved successfully!", Toast.LENGTH_SHORT).show();
                    if (recordId == -1) resetForm();
                    else requireActivity().getSupportFragmentManager().popBackStack();
                });
            }
        });
    }

    private void loadRecordData(int id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            existingRecord = DatabaseHelper.getInstance(requireContext()).soilTransmittedHelminthiasisDao().getRecordById(id);
            if (existingRecord != null && isAdded()) {
                requireActivity().runOnUiThread(() -> setupUIWithRecord(existingRecord));
            }
        });
    }

    private void setupUIWithRecord(SoilTransmittedHelminthiasisRegistryRecord record) {
        // Load Profile ID from record
        selectedProfileId = record.getProfileId();

        etDateOfRegistration.setText(record.getDateOfRegistration());
        etFamilySerialNumber.setText(record.getFamilySerialNumber());
        etName.setText(record.getName(), false);
        etAddress.setText(record.getAddress());
        rgResidency.check("1".equals(record.getResidency()) ? R.id.rbResident : R.id.rbNonResident);
        etDateOfBirth.setText(record.getDateOfBirth());
        etAge.setText(String.valueOf(record.getAge()));

        String[] ageClassCodes = {"A", "B", "C", "D", "E"};
        for (int i = 0; i < ageClassCodes.length; i++) {
            if (ageClassCodes[i].equals(record.getAgeClassification())) {
                spinnerAgeClassification.setSelection(i);
                break;
            }
        }

        rgSex.check("M".equals(record.getSex()) ? R.id.rbMale : R.id.rbFemale);
        rgScreened.check("1".equals(record.getScreened()) ? R.id.rbScreenedYes : R.id.rbScreenedNo);
        etDateOfScreening.setText(record.getDateOfScreening());
        setSelectionFromCode(spinnerScreeningResult, record.getScreeningResult(), new String[]{null, "0", "1", "2"});
        etDateOfResult.setText(record.getDateOfResult());
        setSelectionFromCode(spinnerTreatmentGiven, record.getTreatmentGiven(), new String[]{null, "0", "1", "2"});
        etTreatmentDateGiven.setText(record.getTreatmentDateGiven());

        etJanuaryMdaDate.setText(record.getJanuaryMdaDate());
        if (record.getJanuaryMdaModality() != null) {
            rgJanuaryMdaModality.check("1".equals(record.getJanuaryMdaModality()) ? R.id.rbJanuarySchoolBased : R.id.rbJanuaryCommunityBased);
        }

        etJulyMdaDate.setText(record.getJulyMdaDate());
        if (record.getJulyMdaModality() != null) {
            rgJulyMdaModality.check("1".equals(record.getJulyMdaModality()) ? R.id.rbJulySchoolBased : R.id.rbJulyCommunityBased);
        }

        etRemarks.setText(record.getRemarks());
        btnSave.setText("Update Record");
    }

    private void setSelectionFromCode(Spinner spinner, String code, String[] codes) {
        for (int i = 0; i < codes.length; i++) {
            if (codes[i] != null && codes[i].equals(code)) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0);
    }

    private void resetForm() {
        existingRecord = null;
        recordId = -1;
        selectedProfileId = 0;
        btnSave.setText("Save Record");
        etDateOfRegistration.setText("");
        etFamilySerialNumber.setText("");
        etName.setText("");
        etAddress.setText("");
        rgResidency.clearCheck();
        etDateOfBirth.setText("");
        etAge.setText("");
        spinnerAgeClassification.setSelection(0);
        rgSex.clearCheck();
        rgScreened.clearCheck();
        etDateOfScreening.setText("");
        spinnerScreeningResult.setSelection(0);
        etDateOfResult.setText("");
        spinnerTreatmentGiven.setSelection(0);
        etTreatmentDateGiven.setText("");
        etJanuaryMdaDate.setText("");
        rgJanuaryMdaModality.clearCheck();
        etJulyMdaDate.setText("");
        rgJulyMdaModality.clearCheck();
        etRemarks.setText("");
    }

    private static class DateFormattingWatcher implements TextWatcher {
        private final EditText editText;
        private boolean isDeleting = false;

        public DateFormattingWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            isDeleting = count > after;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isDeleting) {
                // Remove intuitive hyphens when backspacing
                if (s.length() == 5 || s.length() == 8) {
                    s.delete(s.length() - 1, s.length());
                }
                return;
            }

            String digits = s.toString().replaceAll("[^\\d]", "");
            StringBuilder formatted = new StringBuilder();

            int len = digits.length();
            if (len > 8) len = 8; // YYYYMMDD is 8 digits

            for (int i = 0; i < len; i++) {
                formatted.append(digits.charAt(i));
                // Add hyphens after the 4th (YYYY) and 6th (YYYY-MM) digits
                if ((i == 3 && len > 4) || (i == 5 && len > 6)) {
                    formatted.append("-");
                }
            }

            editText.removeTextChangedListener(this);
            editText.setText(formatted.toString());
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }
}