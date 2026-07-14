package com.android.hfsis.idpcs.schistosomiasis;

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
import android.widget.CheckBox;
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
import com.android.hfsis.model.idpcs.schistosomiasis.SchistosomiasisRegistryRecord;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class SchistosomiasisRegistryFragment extends Fragment {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private long currentRecordId = 0;
    private String currentProfileId = null;

    // TextInputLayout containers for handling startIcon clicks
    private TextInputLayout tilDateOfRegistration, tilDateOfBirth, tilDateScreened,
            tilClinicalFirstTreatmentDate, tilClinicalRetreatmentDate, tilClinicalCuredDate,
            tilDateOfDiagnosis, tilDateConfirmed, tilConfirmedFirstTreatmentDate,
            tilConfirmedRetreatmentDate, tilConfirmedCuredDate, tilDateReferredToHospital, tilMdaDateGiven;

    private EditText etDateOfRegistration;
    private EditText etFamilySerialNumber;
    private TextInputLayout tilName;
    private AutoCompleteTextView etName;
    private EditText etAddress;
    private RadioGroup rgResidency;
    private EditText etDateOfBirth;
    private EditText etAge;
    private Spinner spinnerAgeGroup;
    private RadioGroup rgSex;

    private RadioGroup rgHistoryOfExposure;
    private RadioGroup rgScreened;
    private EditText etDateScreened;

    private RadioGroup rgSignsSymptoms;
    private CheckBox cbSymptomAbdominalPain;
    private CheckBox cbSymptomDiarrhea;
    private CheckBox cbSymptomBloodInStool;
    private CheckBox cbSymptomOthers;
    private EditText etSymptomOthersSpecify;
    private List<CheckBox> symptomCheckBoxes;

    private RadioGroup rgClinicalFirstTreatment;
    private EditText etClinicalFirstTreatmentDate;
    private RadioGroup rgClinicalRetreatment;
    private EditText etClinicalRetreatmentDate;
    private RadioGroup rgClinicalCured;
    private EditText etClinicalCuredDate;

    private EditText etDiagnosticTest;
    private EditText etDateOfDiagnosis;
    private RadioGroup rgDiagnosticResult;
    private EditText etDateConfirmed;
    private RadioGroup rgComplicated;
    private RadioGroup rgConfirmedFirstTreatment;
    private EditText etConfirmedFirstTreatmentDate;
    private RadioGroup rgConfirmedRetreatment;
    private EditText etConfirmedRetreatmentDate;
    private RadioGroup rgConfirmedCured;
    private EditText etConfirmedCuredDate;

    private EditText etDateReferredToHospital;

    private RadioGroup rgMdaGiven;
    private EditText etMdaDateGiven;

    private EditText etRemarks;
    private Button btnSave;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    public SchistosomiasisRegistryFragment() {}

    public static SchistosomiasisRegistryFragment newInstance() {
        return new SchistosomiasisRegistryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schistosomiasis_registry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupNameAutocomplete();
        setupAgeGroupSpinner();
        setupDatePickers();
        setupSaveButton();

        if (getArguments() != null && getArguments().containsKey("EDIT_RECORD_ID")) {
            long recordId = getArguments().getLong("EDIT_RECORD_ID");
            loadRecordForEditing(recordId);
        } else {
            // Automatically set the current date for a new record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etDateOfRegistration.setText(sdf.format(new Date()));
        }
    }

    private void bindViews(View view) {
        // Bind TextInputLayout frames
        tilDateOfRegistration = view.findViewById(R.id.tilDateOfRegistration);
        tilDateOfBirth = view.findViewById(R.id.tilDateOfBirth);
        tilDateScreened = view.findViewById(R.id.tilDateScreened);
        tilClinicalFirstTreatmentDate = view.findViewById(R.id.tilClinicalFirstTreatmentDate);
        tilClinicalRetreatmentDate = view.findViewById(R.id.tilClinicalRetreatmentDate);
        tilClinicalCuredDate = view.findViewById(R.id.tilClinicalCuredDate);
        tilDateOfDiagnosis = view.findViewById(R.id.tilDateOfDiagnosis);
        tilDateConfirmed = view.findViewById(R.id.tilDateConfirmed);
        tilConfirmedFirstTreatmentDate = view.findViewById(R.id.tilConfirmedFirstTreatmentDate);
        tilConfirmedRetreatmentDate = view.findViewById(R.id.tilConfirmedRetreatmentDate);
        tilConfirmedCuredDate = view.findViewById(R.id.tilConfirmedCuredDate);
        tilDateReferredToHospital = view.findViewById(R.id.tilDateReferredToHospital);
        tilMdaDateGiven = view.findViewById(R.id.tilMdaDateGiven);

        // Bind EditText views
        etDateOfRegistration = view.findViewById(R.id.etDateOfRegistration);
        etFamilySerialNumber = view.findViewById(R.id.etFamilySerialNumber);
        tilName = view.findViewById(R.id.tilName);
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        rgResidency = view.findViewById(R.id.rgResidency);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        etAge = view.findViewById(R.id.etAge);
        spinnerAgeGroup = view.findViewById(R.id.spinnerAgeGroup);
        rgSex = view.findViewById(R.id.rgSex);

        rgHistoryOfExposure = view.findViewById(R.id.rgHistoryOfExposure);
        rgScreened = view.findViewById(R.id.rgScreened);
        etDateScreened = view.findViewById(R.id.etDateScreened);

        rgSignsSymptoms = view.findViewById(R.id.rgSignsSymptoms);
        cbSymptomAbdominalPain = view.findViewById(R.id.cbSymptomAbdominalPain);
        cbSymptomDiarrhea = view.findViewById(R.id.cbSymptomDiarrhea);
        cbSymptomBloodInStool = view.findViewById(R.id.cbSymptomBloodInStool);
        cbSymptomOthers = view.findViewById(R.id.cbSymptomOthers);
        etSymptomOthersSpecify = view.findViewById(R.id.etSymptomOthersSpecify);
        symptomCheckBoxes = new ArrayList<>();
        symptomCheckBoxes.add(cbSymptomAbdominalPain);
        symptomCheckBoxes.add(cbSymptomDiarrhea);
        symptomCheckBoxes.add(cbSymptomBloodInStool);
        symptomCheckBoxes.add(cbSymptomOthers);

        rgClinicalFirstTreatment = view.findViewById(R.id.rgClinicalFirstTreatment);
        etClinicalFirstTreatmentDate = view.findViewById(R.id.etClinicalFirstTreatmentDate);
        rgClinicalRetreatment = view.findViewById(R.id.rgClinicalRetreatment);
        etClinicalRetreatmentDate = view.findViewById(R.id.etClinicalRetreatmentDate);
        rgClinicalCured = view.findViewById(R.id.rgClinicalCured);
        etClinicalCuredDate = view.findViewById(R.id.etClinicalCuredDate);

        etDiagnosticTest = view.findViewById(R.id.etDiagnosticTest);
        etDateOfDiagnosis = view.findViewById(R.id.etDateOfDiagnosis);
        rgDiagnosticResult = view.findViewById(R.id.rgDiagnosticResult);
        etDateConfirmed = view.findViewById(R.id.etDateConfirmed);
        rgComplicated = view.findViewById(R.id.rgComplicated);
        rgConfirmedFirstTreatment = view.findViewById(R.id.rgConfirmedFirstTreatment);
        etConfirmedFirstTreatmentDate = view.findViewById(R.id.etConfirmedFirstTreatmentDate);
        rgConfirmedRetreatment = view.findViewById(R.id.rgConfirmedRetreatment);
        etConfirmedRetreatmentDate = view.findViewById(R.id.etConfirmedRetreatmentDate);
        rgConfirmedCured = view.findViewById(R.id.rgConfirmedCured);
        etConfirmedCuredDate = view.findViewById(R.id.etConfirmedCuredDate);

        etDateReferredToHospital = view.findViewById(R.id.etDateReferredToHospital);

        rgMdaGiven = view.findViewById(R.id.rgMdaGiven);
        etMdaDateGiven = view.findViewById(R.id.etMdaDateGiven);

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
            tilName.setError(null);
            String selectedName = (String) parent.getItemAtPosition(position);
            autoPopulateFromProfile(selectedName);
        });
    }

    private void autoPopulateFromProfile(String fullCalculatedName) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = dbHelper.householdProfileDao().getProfileByCalculatedName(fullCalculatedName);
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {

                    // Assign Profile ID here
                    currentProfileId = String.valueOf(profile.id);

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
                        etDateOfBirth.setText(profile.dob);
                        try {
                            String[] dobParts = profile.dob.split("-");
                            if (dobParts.length == 3) {
                                int year = Integer.parseInt(dobParts[0]);
                                int month = Integer.parseInt(dobParts[1]) - 1;
                                int day = Integer.parseInt(dobParts[2]);

                                Calendar dob = Calendar.getInstance();
                                dob.set(year, month, day);
                                updateAgeFromDateOfBirth(dob);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void setupAgeGroupSpinner() {
        List<String> ageGroups = new ArrayList<>();
        ageGroups.add("A: 1-4 years old");
        ageGroups.add("B: 5-14 years old");
        ageGroups.add("C: 15-19 years old");
        ageGroups.add("D: 20-59 years old");
        ageGroups.add("E: 60 years old and above");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, ageGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgeGroup.setAdapter(adapter);
    }

    private void setupDatePickers() {
        // Set icon triggers to show date picker dialog
        tilDateOfRegistration.setStartIconOnClickListener(v -> showDatePicker(etDateOfRegistration));
        tilDateOfBirth.setStartIconOnClickListener(v -> showDatePicker(etDateOfBirth));
        tilDateScreened.setStartIconOnClickListener(v -> showDatePicker(etDateScreened));
        tilClinicalFirstTreatmentDate.setStartIconOnClickListener(v -> showDatePicker(etClinicalFirstTreatmentDate));
        tilClinicalRetreatmentDate.setStartIconOnClickListener(v -> showDatePicker(etClinicalRetreatmentDate));
        tilClinicalCuredDate.setStartIconOnClickListener(v -> showDatePicker(etClinicalCuredDate));
        tilDateOfDiagnosis.setStartIconOnClickListener(v -> showDatePicker(etDateOfDiagnosis));
        tilDateConfirmed.setStartIconOnClickListener(v -> showDatePicker(etDateConfirmed));
        tilConfirmedFirstTreatmentDate.setStartIconOnClickListener(v -> showDatePicker(etConfirmedFirstTreatmentDate));
        tilConfirmedRetreatmentDate.setStartIconOnClickListener(v -> showDatePicker(etConfirmedRetreatmentDate));
        tilConfirmedCuredDate.setStartIconOnClickListener(v -> showDatePicker(etConfirmedCuredDate));
        tilDateReferredToHospital.setStartIconOnClickListener(v -> showDatePicker(etDateReferredToHospital));
        tilMdaDateGiven.setStartIconOnClickListener(v -> showDatePicker(etMdaDateGiven));

        // Attach custom date watchers for auto-formatting typed entries
        etDateOfRegistration.addTextChangedListener(new DateFormattingWatcher(etDateOfRegistration));
        etDateOfBirth.addTextChangedListener(new DateFormattingWatcher(etDateOfBirth));
        etDateScreened.addTextChangedListener(new DateFormattingWatcher(etDateScreened));
        etClinicalFirstTreatmentDate.addTextChangedListener(new DateFormattingWatcher(etClinicalFirstTreatmentDate));
        etClinicalRetreatmentDate.addTextChangedListener(new DateFormattingWatcher(etClinicalRetreatmentDate));
        etClinicalCuredDate.addTextChangedListener(new DateFormattingWatcher(etClinicalCuredDate));
        etDateOfDiagnosis.addTextChangedListener(new DateFormattingWatcher(etDateOfDiagnosis));
        etDateConfirmed.addTextChangedListener(new DateFormattingWatcher(etDateConfirmed));
        etConfirmedFirstTreatmentDate.addTextChangedListener(new DateFormattingWatcher(etConfirmedFirstTreatmentDate));
        etConfirmedRetreatmentDate.addTextChangedListener(new DateFormattingWatcher(etConfirmedRetreatmentDate));
        etConfirmedCuredDate.addTextChangedListener(new DateFormattingWatcher(etConfirmedCuredDate));
        etDateReferredToHospital.addTextChangedListener(new DateFormattingWatcher(etDateReferredToHospital));
        etMdaDateGiven.addTextChangedListener(new DateFormattingWatcher(etMdaDateGiven));

        // Handle standalone age re-evaluations upon modification of the profile date field
        etDateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (isValidDateFormat(input)) {
                    try {
                        String[] parts = input.split("-");
                        int y = Integer.parseInt(parts[0]);
                        int m = Integer.parseInt(parts[1]) - 1;
                        int d = Integer.parseInt(parts[2]);
                        Calendar dob = Calendar.getInstance();
                        dob.set(y, m, d);
                        updateAgeFromDateOfBirth(dob);
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
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
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
        if (age < 0) {
            age = 0;
        }
        etAge.setText(String.valueOf(age));
        spinnerAgeGroup.setSelection(ageGroupIndexFor(age));
    }

    private int ageGroupIndexFor(int age) {
        if (age >= 1 && age <= 4) return 0;
        if (age <= 14) return 1;
        if (age <= 19) return 2;
        if (age <= 59) return 3;
        return 4;
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                SchistosomiasisRegistryRecord record = buildRecordFromForm();
                String PREFS_NAME = "AppPrefs";
                SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);
                record.setUserId(userId);
                saveToDatabase(record);
            }
        });
    }

    private boolean isValidDateFormat(String date) {
        return date.matches("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])");
    }

    private boolean validateForm() {
        tilName.setError(null);

        // Validation rule checks for mandatory date formats
        String regDate = etDateOfRegistration.getText().toString().trim();
        if (TextUtils.isEmpty(regDate)) {
            Toast.makeText(requireContext(), "Please select the date of registration", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isValidDateFormat(regDate)) {
            etDateOfRegistration.setError("Required format is yyyy-MM-dd");
            etDateOfRegistration.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etFamilySerialNumber.getText())) {
            etFamilySerialNumber.setError("Family serial number is required");
            return false;
        }
        if (TextUtils.isEmpty(etName.getText())) {
            tilName.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(etAddress.getText())) {
            etAddress.setError("Address is required");
            return false;
        }
        if (rgResidency.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Please select residency", Toast.LENGTH_SHORT).show();
            return false;
        }

        String dobDate = etDateOfBirth.getText().toString().trim();
        if (TextUtils.isEmpty(dobDate)) {
            Toast.makeText(requireContext(), "Please select the date of birth", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isValidDateFormat(dobDate)) {
            etDateOfBirth.setError("Required format is yyyy-MM-dd");
            etDateOfBirth.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etAge.getText())) {
            etAge.setError("Age is required");
            return false;
        }
        if (rgSex.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Please select sex", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Optional dates validation framework
        if (!validateOptionalDate(etDateScreened) ||
                !validateOptionalDate(etClinicalFirstTreatmentDate) ||
                !validateOptionalDate(etClinicalRetreatmentDate) ||
                !validateOptionalDate(etClinicalCuredDate) ||
                !validateOptionalDate(etDateOfDiagnosis) ||
                !validateOptionalDate(etDateConfirmed) ||
                !validateOptionalDate(etConfirmedFirstTreatmentDate) ||
                !validateOptionalDate(etConfirmedRetreatmentDate) ||
                !validateOptionalDate(etConfirmedCuredDate) ||
                !validateOptionalDate(etDateReferredToHospital) ||
                !validateOptionalDate(etMdaDateGiven)) {
            return false;
        }

        return true;
    }

    private boolean validateOptionalDate(EditText target) {
        String val = target.getText().toString().trim();
        if (!val.isEmpty() && !isValidDateFormat(val)) {
            target.setError("Required format is yyyy-MM-dd");
            target.requestFocus();
            return false;
        }
        return true;
    }

    private SchistosomiasisRegistryRecord buildRecordFromForm() {
        SchistosomiasisRegistryRecord record = new SchistosomiasisRegistryRecord();
        record.setId(currentRecordId);

        // Saving the profile ID here
        record.setProfileId(currentProfileId);

        record.setDateOfRegistration(etDateOfRegistration.getText().toString().trim());
        record.setFamilySerialNumber(etFamilySerialNumber.getText().toString().trim());
        record.setName(etName.getText().toString().trim());
        record.setAddress(etAddress.getText().toString().trim());
        record.setResidency(rgResidency.getCheckedRadioButtonId() == R.id.rbResident ? "1" : "2");
        record.setDateOfBirth(etDateOfBirth.getText().toString().trim());

        int age = 0;
        try {
            age = Integer.parseInt(etAge.getText().toString().trim());
        } catch (NumberFormatException ignored) {}
        record.setAge(age);

        String[] ageGroupCodes = {"A", "B", "C", "D", "E"};
        int ageGroupPosition = spinnerAgeGroup.getSelectedItemPosition();
        record.setAgeGroup(ageGroupCodes[Math.max(ageGroupPosition, 0)]);

        record.setSex(rgSex.getCheckedRadioButtonId() == R.id.rbMale ? "M" : "F");

        record.setHistoryOfExposure(yesNoCode(rgHistoryOfExposure, R.id.rbExposureYes));
        record.setScreened(yesNoCode(rgScreened, R.id.rbScreenedYes));
        record.setDateScreened(etDateScreened.getText().toString().trim());

        record.setWithSignsSymptoms(yesNoCode(rgSignsSymptoms, R.id.rbSignsSymptomsYes));
        record.setSignsSymptoms(collectSelectedSymptomCodes());
        record.setSignsSymptomsOtherSpecify(etSymptomOthersSpecify.getText().toString().trim());

        record.setClinicalFirstTreatmentGiven(yesNoCode(rgClinicalFirstTreatment, R.id.rbClinicalFirstTreatmentYes));
        record.setClinicalFirstTreatmentDate(etClinicalFirstTreatmentDate.getText().toString().trim());
        record.setClinicalRetreatment(yesNoCode(rgClinicalRetreatment, R.id.rbClinicalRetreatmentYes));
        record.setClinicalRetreatmentDate(etClinicalRetreatmentDate.getText().toString().trim());
        record.setClinicalCured(yesNoCode(rgClinicalCured, R.id.rbClinicalCuredYes));
        record.setClinicalCuredDate(etClinicalCuredDate.getText().toString().trim());

        record.setDiagnosticTest(etDiagnosticTest.getText().toString().trim());
        record.setDateOfDiagnosis(etDateOfDiagnosis.getText().toString().trim());
        record.setDiagnosticResult(rgDiagnosticResult.getCheckedRadioButtonId() == -1 ? null
                : (rgDiagnosticResult.getCheckedRadioButtonId() == R.id.rbDiagnosticPositive ? "1" : "0"));
        record.setDateConfirmed(etDateConfirmed.getText().toString().trim());
        record.setComplicated(rgComplicated.getCheckedRadioButtonId() == -1 ? null
                : (rgComplicated.getCheckedRadioButtonId() == R.id.rbComplicatedYes ? "1" : "0"));
        record.setConfirmedFirstTreatmentGiven(yesNoCode(rgConfirmedFirstTreatment, R.id.rbConfirmedFirstTreatmentYes));
        record.setConfirmedFirstTreatmentDate(etConfirmedFirstTreatmentDate.getText().toString().trim());
        record.setConfirmedRetreatment(yesNoCode(rgConfirmedRetreatment, R.id.rbConfirmedRetreatmentYes));
        record.setConfirmedRetreatmentDate(etConfirmedRetreatmentDate.getText().toString().trim());
        record.setConfirmedCured(yesNoCode(rgConfirmedCured, R.id.rbConfirmedCuredYes));
        record.setConfirmedCuredDate(etConfirmedCuredDate.getText().toString().trim());

        record.setDateReferredToHospital(etDateReferredToHospital.getText().toString().trim());

        record.setMdaGiven(yesNoCode(rgMdaGiven, R.id.rbMdaGivenYes));
        record.setMdaDateGiven(etMdaDateGiven.getText().toString().trim());

        record.setRemarks(etRemarks.getText().toString().trim());

        return record;
    }

    private String yesNoCode(RadioGroup group, int yesButtonId) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == -1) {
            return null;
        }
        return checkedId == yesButtonId ? "1" : "0";
    }

    private List<String> collectSelectedSymptomCodes() {
        List<String> codes = new ArrayList<>();
        Map<CheckBox, String> codeMap = new LinkedHashMap<>();
        codeMap.put(cbSymptomAbdominalPain, "A");
        codeMap.put(cbSymptomDiarrhea, "B");
        codeMap.put(cbSymptomBloodInStool, "C");
        codeMap.put(cbSymptomOthers, "D");

        for (Map.Entry<CheckBox, String> entry : codeMap.entrySet()) {
            if (entry.getKey().isChecked()) {
                codes.add(entry.getValue());
            }
        }
        return codes;
    }

    private void loadRecordForEditing(long recordId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
                SchistosomiasisRegistryRecord record = db.schistosomiasisDao().getRecordById(recordId);

                if (record != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> populateFormForEditing(record));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void populateFormForEditing(SchistosomiasisRegistryRecord record) {
        if (record == null) return;
        this.currentRecordId = record.getId();
        this.currentProfileId = record.getProfileId(); // Set the ID here

        etDateOfRegistration.setText(record.getDateOfRegistration());
        etFamilySerialNumber.setText(record.getFamilySerialNumber());
        etName.setText(record.getName(), false);
        etAddress.setText(record.getAddress());

        if ("1".equals(record.getResidency())) {
            rgResidency.check(R.id.rbResident);
        } else if ("2".equals(record.getResidency())) {
            rgResidency.check(R.id.rbNonResident);
        }

        etDateOfBirth.setText(record.getDateOfBirth());
        etAge.setText(String.valueOf(record.getAge()));

        String ageGrp = record.getAgeGroup();
        if (ageGrp != null) {
            switch (ageGrp) {
                case "A": spinnerAgeGroup.setSelection(0); break;
                case "B": spinnerAgeGroup.setSelection(1); break;
                case "C": spinnerAgeGroup.setSelection(2); break;
                case "D": spinnerAgeGroup.setSelection(3); break;
                case "E": spinnerAgeGroup.setSelection(4); break;
            }
        }

        if ("M".equals(record.getSex())) {
            rgSex.check(R.id.rbMale);
        } else if ("F".equals(record.getSex())) {
            rgSex.check(R.id.rbFemale);
        }

        setYesNoRadioGroup(rgHistoryOfExposure, R.id.rbExposureYes, R.id.rbExposureNo, record.getHistoryOfExposure());
        setYesNoRadioGroup(rgScreened, R.id.rbScreenedYes, R.id.rbScreenedNo, record.getScreened());
        etDateScreened.setText(record.getDateScreened());

        setYesNoRadioGroup(rgSignsSymptoms, R.id.rbSignsSymptomsYes, R.id.rbSignsSymptomsNo, record.getWithSignsSymptoms());

        List<String> list = record.getSignsSymptoms();
        if (list != null) {
            cbSymptomAbdominalPain.setChecked(list.contains("A"));
            cbSymptomDiarrhea.setChecked(list.contains("B"));
            cbSymptomBloodInStool.setChecked(list.contains("C"));
            cbSymptomOthers.setChecked(list.contains("D"));
        }
        etSymptomOthersSpecify.setText(record.getSignsSymptomsOtherSpecify());

        setYesNoRadioGroup(rgClinicalFirstTreatment, R.id.rbClinicalFirstTreatmentYes, R.id.rbClinicalFirstTreatmentNo, record.getClinicalFirstTreatmentGiven());
        etClinicalFirstTreatmentDate.setText(record.getClinicalFirstTreatmentDate());
        setYesNoRadioGroup(rgClinicalRetreatment, R.id.rbClinicalRetreatmentYes, R.id.rbClinicalRetreatmentNo, record.getClinicalRetreatment());
        etClinicalRetreatmentDate.setText(record.getClinicalRetreatmentDate());
        setYesNoRadioGroup(rgClinicalCured, R.id.rbClinicalCuredYes, R.id.rbClinicalCuredNo, record.getClinicalCured());
        etClinicalCuredDate.setText(record.getClinicalCuredDate());

        etDiagnosticTest.setText(record.getDiagnosticTest());
        etDateOfDiagnosis.setText(record.getDateOfDiagnosis());
        setYesNoRadioGroup(rgDiagnosticResult, R.id.rbDiagnosticPositive, R.id.rbDiagnosticNegative, record.getDiagnosticResult());
        etDateConfirmed.setText(record.getDateConfirmed());
        setYesNoRadioGroup(rgComplicated, R.id.rbComplicatedYes, R.id.rbComplicatedNo, record.getComplicated());
        setYesNoRadioGroup(rgConfirmedFirstTreatment, R.id.rbConfirmedFirstTreatmentYes, R.id.rbConfirmedFirstTreatmentNo, record.getConfirmedFirstTreatmentGiven());
        etConfirmedFirstTreatmentDate.setText(record.getConfirmedFirstTreatmentDate());
        setYesNoRadioGroup(rgConfirmedRetreatment, R.id.rbConfirmedRetreatmentYes, R.id.rbConfirmedRetreatmentNo, record.getConfirmedRetreatment());
        etConfirmedRetreatmentDate.setText(record.getConfirmedRetreatmentDate());
        setYesNoRadioGroup(rgConfirmedCured, R.id.rbConfirmedCuredYes, R.id.rbConfirmedCuredNo, record.getConfirmedCured());
        etConfirmedCuredDate.setText(record.getConfirmedCuredDate());

        etDateReferredToHospital.setText(record.getDateReferredToHospital());
        setYesNoRadioGroup(rgMdaGiven, R.id.rbMdaGivenYes, R.id.rbMdaGivenNo, record.getMdaGiven());
        etMdaDateGiven.setText(record.getMdaDateGiven());

        etRemarks.setText(record.getRemarks());
    }

    private void setYesNoRadioGroup(RadioGroup group, int yesId, int noId, String value) {
        if ("1".equals(value)) {
            group.check(yesId);
        } else if ("0".equals(value)) {
            group.check(noId);
        } else {
            group.clearCheck();
        }
    }

    private void saveToDatabase(SchistosomiasisRegistryRecord record) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                DatabaseHelper db = DatabaseHelper.getInstance(requireContext());

                if (record.getId() == 0) {
                    long generatedId = db.schistosomiasisDao().insert(record);
                    record.setId(generatedId);
                } else {
                    db.schistosomiasisDao().update(record);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Record processed successfully", Toast.LENGTH_SHORT).show();
                        if (getParentFragmentManager() != null) {
                            getParentFragmentManager().popBackStack();
                        } else {
                            resetForm();
                        }
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error saving record: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void resetForm() {
        currentRecordId = 0;
        currentProfileId = null; // Clear ID here

        etDateOfRegistration.setText("");
        etFamilySerialNumber.setText("");
        etName.setText("");
        tilName.setError(null);
        etAddress.setText("");
        rgResidency.clearCheck();
        etDateOfBirth.setText("");
        etAge.setText("");
        spinnerAgeGroup.setSelection(0);
        rgSex.clearCheck();

        rgHistoryOfExposure.clearCheck();
        rgScreened.clearCheck();
        etDateScreened.setText("");

        rgSignsSymptoms.clearCheck();
        if (symptomCheckBoxes != null) {
            for (CheckBox cb : symptomCheckBoxes) {
                cb.setChecked(false);
            }
        }
        etSymptomOthersSpecify.setText("");

        rgClinicalFirstTreatment.clearCheck();
        etClinicalFirstTreatmentDate.setText("");
        rgClinicalRetreatment.clearCheck();
        etClinicalRetreatmentDate.setText("");
        rgClinicalCured.clearCheck();
        etClinicalCuredDate.setText("");

        etDiagnosticTest.setText("");
        etDateOfDiagnosis.setText("");
        rgDiagnosticResult.clearCheck();
        etDateConfirmed.setText("");
        rgComplicated.clearCheck();
        rgConfirmedFirstTreatment.clearCheck();
        etConfirmedFirstTreatmentDate.setText("");
        rgConfirmedRetreatment.clearCheck();
        etConfirmedRetreatmentDate.setText("");
        rgConfirmedCured.clearCheck();
        etConfirmedCuredDate.setText("");

        etDateReferredToHospital.setText("");
        rgMdaGiven.clearCheck();
        etMdaDateGiven.setText("");
        etRemarks.setText("");
    }

    // Custom formatting text watcher to insert dashes at indexes 4 and 7 for a clean yyyy-MM-dd typing flow
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
                if (s.length() == 5 || s.length() == 8) {
                    s.delete(s.length() - 1, s.length());
                }
                return;
            }

            String digits = s.toString().replaceAll("[^\\d]", "");
            StringBuilder formatted = new StringBuilder();

            int len = digits.length();
            if (len > 8) len = 8;

            for (int i = 0; i < len; i++) {
                formatted.append(digits.charAt(i));
                if (i == 3 && len > 4) {
                    formatted.append("-");
                } else if (i == 5 && len > 6) {
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