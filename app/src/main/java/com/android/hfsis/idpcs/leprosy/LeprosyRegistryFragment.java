package com.android.hfsis.idpcs.leprosy;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.idpcs.leprosy.LeprosyRegistryRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class LeprosyRegistryFragment extends Fragment {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String ARG_RECORD_ID = "record_id";

    private long existingRecordId = -1;
    private long selectedProfileId = -1;
    private DatabaseHelper database;

    private EditText etDateOfRegistration, etAddress, etDateOfBirth, etAge;
    private AutoCompleteTextView etName;
    private Spinner spinnerAgeGroup;
    private RadioGroup rgSex, rgConfirmedCase;
    private EditText etDateOfDiagnosis;
    private Spinner spinnerCaseHistory;
    private EditText etPreviousFacility;
    private RadioGroup rgClinicalClassification;
    private EditText etTreatmentStartDate, etMonthsTreatedPrior;
    private RadioGroup rgReclassified, rgUpdatedClassification;
    private EditText etDateOfReclassification;
    private Spinner spinnerTreatmentOutcome;
    private RadioGroup rgCompletedFixedMdt, rgBeyondFixedMdt;
    private EditText etFixedMdtCompletedDate, etBeyondFixedMdtCompletedDate;
    private RadioGroup rgGrade2Disability;
    private EditText etRemarks;
    private Button btnSave;
    private TextView tvFormTitle;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    public LeprosyRegistryFragment() {}

    public static LeprosyRegistryFragment newInstance() {
        return new LeprosyRegistryFragment();
    }

    public static LeprosyRegistryFragment newInstance(long recordId) {
        LeprosyRegistryFragment fragment = new LeprosyRegistryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RECORD_ID, recordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            existingRecordId = getArguments().getLong(ARG_RECORD_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leprosy_registry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = DatabaseHelper.getInstance(requireContext());

        bindViews(view);
        setupNameAutocomplete();
        setupAgeGroupSpinner();
        setupCaseHistorySpinner();
        setupTreatmentOutcomeSpinner();
        setupDatePickers();
        setupTransferInFieldSync();
        setupSaveButton();

        if (existingRecordId > 0) {
            tvFormTitle.setText("Update Leprosy Record");
            btnSave.setText("Update Record");
            loadExistingRecord();
        } else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etDateOfRegistration.setText(sdf.format(new Date()));
        }
    }

    private void bindViews(View view) {
        tvFormTitle = view.findViewById(R.id.tvFormTitle);
        etDateOfRegistration = view.findViewById(R.id.etDateOfRegistration);
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        etAge = view.findViewById(R.id.etAge);
        spinnerAgeGroup = view.findViewById(R.id.spinnerAgeGroup);
        rgSex = view.findViewById(R.id.rgSex);
        rgConfirmedCase = view.findViewById(R.id.rgConfirmedCase);
        etDateOfDiagnosis = view.findViewById(R.id.etDateOfDiagnosis);
        spinnerCaseHistory = view.findViewById(R.id.spinnerCaseHistory);
        etPreviousFacility = view.findViewById(R.id.etPreviousFacility);
        rgClinicalClassification = view.findViewById(R.id.rgClinicalClassification);
        etTreatmentStartDate = view.findViewById(R.id.etTreatmentStartDate);
        etMonthsTreatedPrior = view.findViewById(R.id.etMonthsTreatedPrior);
        rgReclassified = view.findViewById(R.id.rgReclassified);
        etDateOfReclassification = view.findViewById(R.id.etDateOfReclassification);
        rgUpdatedClassification = view.findViewById(R.id.rgUpdatedClassification);
        spinnerTreatmentOutcome = view.findViewById(R.id.spinnerTreatmentOutcome);
        rgCompletedFixedMdt = view.findViewById(R.id.rgCompletedFixedMdt);
        etFixedMdtCompletedDate = view.findViewById(R.id.etFixedMdtCompletedDate);
        rgBeyondFixedMdt = view.findViewById(R.id.rgBeyondFixedMdt);
        etBeyondFixedMdtCompletedDate = view.findViewById(R.id.etBeyondFixedMdtCompletedDate);
        rgGrade2Disability = view.findViewById(R.id.rgGrade2Disability);
        etRemarks = view.findViewById(R.id.etRemarks);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupNameAutocomplete() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> namesList = database.householdProfileDao().getAllHouseholdNames();
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

        // Opens the full unfiltered dropdown immediately upon field area touch interactions
        etName.setOnClickListener(v -> etName.showDropDown());

        etName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            autoPopulateFromProfile(selectedName);
        });
    }

    private void autoPopulateFromProfile(String fullCalculatedName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = database.householdProfileDao().getProfileByCalculatedName(fullCalculatedName);
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    selectedProfileId = profile.id; // Assign the extracted ID here

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

                    if ("M".equalsIgnoreCase(profile.sex) || "Male".equalsIgnoreCase(profile.sex)) {
                        rgSex.check(R.id.rbMale);
                    } else if ("F".equalsIgnoreCase(profile.sex) || "Female".equalsIgnoreCase(profile.sex)) {
                        rgSex.check(R.id.rbFemale);
                    }

                    if (!TextUtils.isEmpty(profile.dob)) {
                        etDateOfBirth.setText(profile.dob);
                        try {
                            // Updated split logic to parse YYYY-MM-DD
                            String[] dobParts = profile.dob.split("-");
                            if (dobParts.length == 3) {
                                int year = Integer.parseInt(dobParts[0]);
                                int month = Integer.parseInt(dobParts[1]) - 1;
                                int day = Integer.parseInt(dobParts[2]);

                                Calendar selectedDob = Calendar.getInstance();
                                selectedDob.set(year, month, day);
                                updateAgeFromDateOfBirth(selectedDob);
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
        ageGroups.add("A: 0-14 years old");
        ageGroups.add("B: 15-18 years old");
        ageGroups.add("C: 19 years old and above");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ageGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgeGroup.setAdapter(adapter);
    }

    private void setupCaseHistorySpinner() {
        List<String> caseHistories = new ArrayList<>();
        caseHistories.add("0: New (No history)");
        caseHistories.add("1: Relapse");
        caseHistories.add("2: Defaulter");
        caseHistories.add("3: Transfer-in");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, caseHistories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCaseHistory.setAdapter(adapter);
    }

    private void setupTreatmentOutcomeSpinner() {
        List<String> outcomes = new ArrayList<>();
        outcomes.add("— Select status —");
        outcomes.add("1: Ongoing Treatment");
        outcomes.add("2: Completed Treatment");
        outcomes.add("3: Defaulted");
        outcomes.add("4: Transferred Out");
        outcomes.add("5: Died");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, outcomes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTreatmentOutcome.setAdapter(adapter);
    }

    private void setupDatePickers() {
        etDateOfRegistration.setOnClickListener(v -> showDatePicker(etDateOfRegistration));
        etDateOfBirth.setOnClickListener(v -> showDatePicker(etDateOfBirth));
        etDateOfDiagnosis.setOnClickListener(v -> showDatePicker(etDateOfDiagnosis));
        etTreatmentStartDate.setOnClickListener(v -> showDatePicker(etTreatmentStartDate));
        etDateOfReclassification.setOnClickListener(v -> showDatePicker(etDateOfReclassification));
        etFixedMdtCompletedDate.setOnClickListener(v -> showDatePicker(etFixedMdtCompletedDate));
        etBeyondFixedMdtCompletedDate.setOnClickListener(v -> showDatePicker(etBeyondFixedMdtCompletedDate));
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (datePicker, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            target.setText(dateFormat.format(selected.getTime()));
            if (target == etDateOfBirth) {
                updateAgeFromDateOfBirth(selected);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void updateAgeFromDateOfBirth(Calendar dob) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--;
        if (age < 0) age = 0;
        etAge.setText(String.valueOf(age));
        spinnerAgeGroup.setSelection(ageGroupIndexFor(age));
    }

    private int ageGroupIndexFor(int age) {
        if (age <= 14) return 0;
        if (age <= 18) return 1;
        return 2;
    }

    private void setupTransferInFieldSync() {
        spinnerCaseHistory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setTransferInFieldsEnabled(position == 3);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setTransferInFieldsEnabled(false);
            }
        });
    }

    private void setTransferInFieldsEnabled(boolean enabled) {
        etPreviousFacility.setEnabled(enabled);
        etMonthsTreatedPrior.setEnabled(enabled);
        if (!enabled) {
            etPreviousFacility.setText("");
            etMonthsTreatedPrior.setText("");
        }
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                LeprosyRegistryRecord record = buildRecordFromForm();
                String PREFS_NAME = "AppPrefs";
                SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);
                record.setUserId(userId);

                new Thread(() -> {
                    if (existingRecordId > 0) {
                        record.setId(existingRecordId);
                        database.leprosyRegistryDao().update(record);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Record updated successfully", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        });
                    } else {
                        database.leprosyRegistryDao().insert(record);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Record saved successfully", Toast.LENGTH_SHORT).show();
                            resetForm();
                        });
                    }
                }).start();
            }
        });
    }

    private void loadExistingRecord() {
        new Thread(() -> {
            LeprosyRegistryRecord record = database.leprosyRegistryDao().getRecordById(existingRecordId);
            if (record != null) {
                requireActivity().runOnUiThread(() -> populateForm(record));
            }
        }).start();
    }

    private void populateForm(LeprosyRegistryRecord record) {
        selectedProfileId = record.getProfileId(); // Restore the extracted ID here

        etDateOfRegistration.setText(record.getDateOfRegistration());
        etName.setText(record.getName());
        etAddress.setText(record.getAddress());
        etDateOfBirth.setText(record.getDateOfBirth());
        etAge.setText(String.valueOf(record.getAge()));

        if ("A".equals(record.getAgeGroup())) spinnerAgeGroup.setSelection(0);
        else if ("B".equals(record.getAgeGroup())) spinnerAgeGroup.setSelection(1);
        else if ("C".equals(record.getAgeGroup())) spinnerAgeGroup.setSelection(2);

        if ("M".equals(record.getSex())) rgSex.check(R.id.rbMale);
        else if ("F".equals(record.getSex())) rgSex.check(R.id.rbFemale);

        if ("1".equals(record.getConfirmedCase())) rgConfirmedCase.check(R.id.rbConfirmedCaseYes);
        else if ("0".equals(record.getConfirmedCase())) rgConfirmedCase.check(R.id.rbConfirmedCaseNo);

        etDateOfDiagnosis.setText(record.getDateOfDiagnosis());

        try {
            int chPos = Integer.parseInt(record.getCaseHistory());
            spinnerCaseHistory.setSelection(chPos);
        } catch (Exception e) { spinnerCaseHistory.setSelection(0); }
        etPreviousFacility.setText(record.getPreviousFacility());

        if ("1".equals(record.getClinicalClassification())) rgClinicalClassification.check(R.id.rbClassificationPb);
        else if ("2".equals(record.getClinicalClassification())) rgClinicalClassification.check(R.id.rbClassificationMb);

        etTreatmentStartDate.setText(record.getTreatmentStartDate());
        etMonthsTreatedPrior.setText(record.getMonthsTreatedPrior());

        if ("1".equals(record.getReclassified())) rgReclassified.check(R.id.rbReclassifiedYes);
        else if ("0".equals(record.getReclassified())) rgReclassified.check(R.id.rbReclassifiedNo);

        etDateOfReclassification.setText(record.getDateOfReclassification());

        if ("1".equals(record.getUpdatedClassification())) rgUpdatedClassification.check(R.id.rbUpdatedClassificationPb);
        else if ("2".equals(record.getUpdatedClassification())) rgUpdatedClassification.check(R.id.rbUpdatedClassificationMb);

        if (record.getTreatmentOutcome() != null) {
            try {
                int outPos = Integer.parseInt(record.getTreatmentOutcome());
                spinnerTreatmentOutcome.setSelection(outPos);
            } catch (Exception e) { spinnerTreatmentOutcome.setSelection(0); }
        }

        if ("1".equals(record.getCompletedFixedMdt())) rgCompletedFixedMdt.check(R.id.rbCompletedFixedMdtYes);
        else if ("0".equals(record.getCompletedFixedMdt())) rgCompletedFixedMdt.check(R.id.rbCompletedFixedMdtNo);

        etFixedMdtCompletedDate.setText(record.getFixedMdtCompletedDate());

        if ("1".equals(record.getBeyondFixedMdt())) rgBeyondFixedMdt.check(R.id.rbBeyondFixedMdtYes);
        else if ("0".equals(record.getBeyondFixedMdt())) rgBeyondFixedMdt.check(R.id.rbBeyondFixedMdtNo);

        etBeyondFixedMdtCompletedDate.setText(record.getBeyondFixedMdtCompletedDate());

        if ("1".equals(record.getGrade2Disability())) rgGrade2Disability.check(R.id.rbGrade2DisabilityYes);
        else if ("0".equals(record.getGrade2Disability())) rgGrade2Disability.check(R.id.rbGrade2DisabilityNo);

        etRemarks.setText(record.getRemarks());
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(etDateOfRegistration.getText())) { etDateOfRegistration.setError("Required"); return false; }
        if (TextUtils.isEmpty(etName.getText())) { etName.setError("Required"); return false; }
        if (TextUtils.isEmpty(etAddress.getText())) { etAddress.setError("Required"); return false; }
        if (TextUtils.isEmpty(etDateOfBirth.getText())) { etDateOfBirth.setError("Required"); return false; }
        if (TextUtils.isEmpty(etAge.getText())) { etAge.setError("Required"); return false; }
        if (rgSex.getCheckedRadioButtonId() == -1) { Toast.makeText(requireContext(), "Select Sex", Toast.LENGTH_SHORT).show(); return false; }
        if (rgConfirmedCase.getCheckedRadioButtonId() == -1) { Toast.makeText(requireContext(), "Select Confirmation Status", Toast.LENGTH_SHORT).show(); return false; }
        return true;
    }

    private LeprosyRegistryRecord buildRecordFromForm() {
        LeprosyRegistryRecord record = new LeprosyRegistryRecord();

        record.setProfileId(selectedProfileId); // Add the ID into the payload
        record.setDateOfRegistration(etDateOfRegistration.getText().toString());
        record.setName(etName.getText().toString().trim());
        record.setAddress(etAddress.getText().toString().trim());
        record.setDateOfBirth(etDateOfBirth.getText().toString());
        int age = 0;
        try { age = Integer.parseInt(etAge.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        record.setAge(age);

        String[] ageGroupCodes = {"A", "B", "C"};
        record.setAgeGroup(ageGroupCodes[Math.max(spinnerAgeGroup.getSelectedItemPosition(), 0)]);
        record.setSex(rgSex.getCheckedRadioButtonId() == R.id.rbMale ? "M" : "F");
        record.setConfirmedCase(rgConfirmedCase.getCheckedRadioButtonId() == R.id.rbConfirmedCaseYes ? "1" : "0");
        record.setDateOfDiagnosis(etDateOfDiagnosis.getText().toString());

        String[] caseHistoryCodes = {"0", "1", "2", "3"};
        record.setCaseHistory(caseHistoryCodes[Math.max(spinnerCaseHistory.getSelectedItemPosition(), 0)]);
        record.setPreviousFacility(etPreviousFacility.getText().toString().trim());

        record.setClinicalClassification(classificationCode(rgClinicalClassification, R.id.rbClassificationPb));
        record.setTreatmentStartDate(etTreatmentStartDate.getText().toString());
        record.setMonthsTreatedPrior(etMonthsTreatedPrior.getText().toString().trim());

        record.setReclassified(yesNoCode(rgReclassified, R.id.rbReclassifiedYes));
        record.setDateOfReclassification(etDateOfReclassification.getText().toString());
        record.setUpdatedClassification(classificationCode(rgUpdatedClassification, R.id.rbUpdatedClassificationPb));

        String[] outcomeCodes = {null, "1", "2", "3", "4", "5"};
        int outcomePosition = spinnerTreatmentOutcome.getSelectedItemPosition();
        record.setTreatmentOutcome(outcomePosition >= 0 && outcomePosition < outcomeCodes.length ? outcomeCodes[outcomePosition] : null);

        record.setCompletedFixedMdt(yesNoCode(rgCompletedFixedMdt, R.id.rbCompletedFixedMdtYes));
        record.setFixedMdtCompletedDate(etFixedMdtCompletedDate.getText().toString());
        record.setBeyondFixedMdt(yesNoCode(rgBeyondFixedMdt, R.id.rbBeyondFixedMdtYes));
        record.setBeyondFixedMdtCompletedDate(etBeyondFixedMdtCompletedDate.getText().toString());
        record.setGrade2Disability(yesNoCode(rgGrade2Disability, R.id.rbGrade2DisabilityYes));
        record.setRemarks(etRemarks.getText().toString().trim());

        return record;
    }

    private String yesNoCode(RadioGroup group, int yesButtonId) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == -1) return null;
        return checkedId == yesButtonId ? "1" : "0";
    }

    private String classificationCode(RadioGroup group, int pbButtonId) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == -1) return null;
        return checkedId == pbButtonId ? "1" : "2";
    }

    private void resetForm() {
        existingRecordId = -1;
        selectedProfileId = -1; // Reset the selected ID

        tvFormTitle.setText("Leprosy Registry Form");
        etDateOfRegistration.setText("");
        etName.setText("");
        etAddress.setText("");
        etDateOfBirth.setText("");
        etAge.setText("");
        spinnerAgeGroup.setSelection(0);
        rgSex.clearCheck();
        rgConfirmedCase.clearCheck();
        etDateOfDiagnosis.setText("");
        spinnerCaseHistory.setSelection(0);
        etPreviousFacility.setText("");
        rgClinicalClassification.clearCheck();
        etTreatmentStartDate.setText("");
        etMonthsTreatedPrior.setText("");
        rgReclassified.clearCheck();
        etDateOfReclassification.setText("");
        rgUpdatedClassification.clearCheck();
        spinnerTreatmentOutcome.setSelection(0);
        rgCompletedFixedMdt.clearCheck();
        etFixedMdtCompletedDate.setText("");
        rgBeyondFixedMdt.clearCheck();
        etBeyondFixedMdtCompletedDate.setText("");
        rgGrade2Disability.clearCheck();
        etRemarks.setText("");
        btnSave.setText("Save Record");
    }
}