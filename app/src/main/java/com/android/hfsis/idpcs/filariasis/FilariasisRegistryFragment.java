package com.android.hfsis.idpcs.filariasis;

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
import com.android.hfsis.database.idpcs.filariasis.FilariasisDao;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.idpcs.filariasis.FilariasisRegistryRecord;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilariasisRegistryFragment extends Fragment {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String ARG_RECORD_ID = "arg_record_id";

    private EditText etDateOfRegistration, etDateOfBirth, etDateNbeRdt;
    private EditText etAlbendazoleDateGiven, etDecDateGiven, etIvermectinDateGiven;
    private EditText etFamilySerialNumber, etAddress, etAge, etRemarks;

    // Changed to AutoCompleteTextView to match EyesScreeningFragment layout implementation
    private AutoCompleteTextView etName;

    private TextInputLayout tilDateOfRegistration, tilDateOfBirth, tilDateNbeRdt;
    private TextInputLayout tilAlbendazoleDateGiven, tilDecDateGiven, tilIvermectinDateGiven;

    private Spinner spinnerAgeGroup;
    private RadioGroup rgSex, rgBloodTestResult, rgLymphedemaExamined, rgElephantiasisExamined, rgHydroceleExamined;
    private CheckBox cbNbePerformed, cbRdtPerformed, cbLymphedemaPresent, cbElephantiasisPresent, cbHydrocelePresent;
    private Button btnSave;

    private DatabaseHelper dbHelper;
    private ExecutorService executorService;
    private long currentRecordId = -1;

    // Track references for linked household records
    private int selectedProfileId = 0;
    private final Calendar calendar = Calendar.getInstance();

    /**
     * Factory method used to open the form for a brand-new entry.
     */
    public static FilariasisRegistryFragment newInstance() {
        return new FilariasisRegistryFragment();
    }

    /**
     * Factory method used to open the form for editing an existing entry.
     */
    public static FilariasisRegistryFragment newInstanceForEdit(long recordId) {
        FilariasisRegistryFragment fragment = new FilariasisRegistryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RECORD_ID, recordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DatabaseHelper.getInstance(requireContext());
        executorService = Executors.newFixedThreadPool(2); // Accommodate auto-complete threads smoothly
        if (getArguments() != null) {
            currentRecordId = getArguments().getLong(ARG_RECORD_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filariasis_registry, container, false);
        initializeViews(view);
        setupNameAutocomplete();
        setupSpinners();
        setupListeners();

        if (currentRecordId != -1) {
            loadRecordData();
        } else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etDateOfRegistration.setText(sdf.format(new Date()));
        }
        return view;
    }

    private void initializeViews(View view) {
        tilDateOfRegistration = view.findViewById(R.id.tilDateOfRegistration);
        tilDateOfBirth = view.findViewById(R.id.tilDateOfBirth);
        tilDateNbeRdt = view.findViewById(R.id.tilDateNbeRdt);
        tilAlbendazoleDateGiven = view.findViewById(R.id.tilAlbendazoleDateGiven);
        tilDecDateGiven = view.findViewById(R.id.tilDecDateGiven);
        tilIvermectinDateGiven = view.findViewById(R.id.tilIvermectinDateGiven);

        etDateOfRegistration = view.findViewById(R.id.etDateOfRegistration);
        etFamilySerialNumber = view.findViewById(R.id.etFamilySerialNumber);

        // Refactored UI link to explicitly catch the AutoCompleteTextView subclass
        etName = view.findViewById(R.id.etName);

        etAddress = view.findViewById(R.id.etAddress);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        etAge = view.findViewById(R.id.etAge);
        etDateNbeRdt = view.findViewById(R.id.etDateNbeRdt);
        etAlbendazoleDateGiven = view.findViewById(R.id.etAlbendazoleDateGiven);
        etDecDateGiven = view.findViewById(R.id.etDecDateGiven);
        etIvermectinDateGiven = view.findViewById(R.id.etIvermectinDateGiven);
        etRemarks = view.findViewById(R.id.etRemarks);

        spinnerAgeGroup = view.findViewById(R.id.spinnerAgeGroup);
        rgSex = view.findViewById(R.id.rgSex);
        rgBloodTestResult = view.findViewById(R.id.rgBloodTestResult);
        rgLymphedemaExamined = view.findViewById(R.id.rgLymphedemaExamined);
        rgElephantiasisExamined = view.findViewById(R.id.rgElephantiasisExamined);
        rgHydroceleExamined = view.findViewById(R.id.rgHydroceleExamined);

        cbNbePerformed = view.findViewById(R.id.cbNbePerformed);
        cbRdtPerformed = view.findViewById(R.id.cbRdtPerformed);
        cbLymphedemaPresent = view.findViewById(R.id.cbLymphedemaPresent);
        cbElephantiasisPresent = view.findViewById(R.id.cbElephantiasisPresent);
        cbHydrocelePresent = view.findViewById(R.id.cbHydrocelePresent);

        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupNameAutocomplete() {
        executorService.execute(() -> {
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
        executorService.execute(() -> {
            HouseholdProfile profile = dbHelper.householdProfileDao().getProfileByCalculatedName(fullCalculatedName);
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    selectedProfileId = profile.id;

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
                        // Triggers age calculation instantly via text watcher hooks
                    }
                });
            }
        });
    }

    private void setupSpinners() {
        List<String> groups = new ArrayList<>();
        groups.add("Select Age Group");
        groups.add("A: <15");
        groups.add("B: 15-24");
        groups.add("C: 25-34");
        groups.add("D: 35-44");
        groups.add("E: 45-54");
        groups.add("F: 55-64");
        groups.add("G: 65+");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgeGroup.setAdapter(adapter);
    }

    private void setupListeners() {
        // Setup Start Icon Click Listeners
        if (tilDateOfRegistration != null) tilDateOfRegistration.setStartIconOnClickListener(v -> showDatePicker(etDateOfRegistration));
        if (tilDateOfBirth != null) tilDateOfBirth.setStartIconOnClickListener(v -> showDatePicker(etDateOfBirth));
        if (tilDateNbeRdt != null) tilDateNbeRdt.setStartIconOnClickListener(v -> showDatePicker(etDateNbeRdt));
        if (tilAlbendazoleDateGiven != null) tilAlbendazoleDateGiven.setStartIconOnClickListener(v -> showDatePicker(etAlbendazoleDateGiven));
        if (tilDecDateGiven != null) tilDecDateGiven.setStartIconOnClickListener(v -> showDatePicker(etDecDateGiven));
        if (tilIvermectinDateGiven != null) tilIvermectinDateGiven.setStartIconOnClickListener(v -> showDatePicker(etIvermectinDateGiven));

        // Setup EditText Click Listeners for seamless UX
        etDateOfRegistration.setOnClickListener(v -> showDatePicker(etDateOfRegistration));
        etDateOfBirth.setOnClickListener(v -> showDatePicker(etDateOfBirth));
        etDateNbeRdt.setOnClickListener(v -> showDatePicker(etDateNbeRdt));
        etAlbendazoleDateGiven.setOnClickListener(v -> showDatePicker(etAlbendazoleDateGiven));
        etDecDateGiven.setOnClickListener(v -> showDatePicker(etDecDateGiven));
        etIvermectinDateGiven.setOnClickListener(v -> showDatePicker(etIvermectinDateGiven));

        // Dynamic typing formatters
        etDateOfRegistration.addTextChangedListener(new DateFormattingWatcher(etDateOfRegistration));
        etDateOfBirth.addTextChangedListener(new DateFormattingWatcher(etDateOfBirth));
        etDateNbeRdt.addTextChangedListener(new DateFormattingWatcher(etDateNbeRdt));
        etAlbendazoleDateGiven.addTextChangedListener(new DateFormattingWatcher(etAlbendazoleDateGiven));
        etDecDateGiven.addTextChangedListener(new DateFormattingWatcher(etDecDateGiven));
        etIvermectinDateGiven.addTextChangedListener(new DateFormattingWatcher(etIvermectinDateGiven));

        // Automated Age Calculation
        etDateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    calculateAgeFromBirthDate(s.toString());
                }
            }
        });

        btnSave.setOnClickListener(v -> saveOrUpdateData());
    }

    private void showDatePicker(EditText editText) {
        DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN, Locale.US);
            editText.setText(formatter.format(calendar.getTime()));
            editText.setError(null);
        };

        new DatePickerDialog(requireContext(), listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void calculateAgeFromBirthDate(String dobText) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
            Calendar dob = Calendar.getInstance();
            dob.setTime(sdf.parse(dobText));
            Calendar now = Calendar.getInstance();

            int calculatedAge = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                calculatedAge--;
            }

            int finalAge = Math.max(0, calculatedAge);
            etAge.setText(String.valueOf(finalAge));

            // Contextually pre-select age bracket spinner item based on standard rules
            autoSelectAgeGroupSpinner(finalAge);

        } catch (Exception ignored) {}
    }

    private void autoSelectAgeGroupSpinner(int age) {
        int targetPosition = 0;
        if (age < 15) targetPosition = 1;
        else if (age <= 24) targetPosition = 2;
        else if (age <= 34) targetPosition = 3;
        else if (age <= 44) targetPosition = 4;
        else if (age <= 54) targetPosition = 5;
        else if (age <= 64) targetPosition = 6;
        else targetPosition = 7;

        if (spinnerAgeGroup.getAdapter() != null && targetPosition < spinnerAgeGroup.getAdapter().getCount()) {
            spinnerAgeGroup.setSelection(targetPosition);
        }
    }

    private boolean isValidDateFormat(String date) {
        return date.matches("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])");
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("Field Required");
            return false;
        }

        if (TextUtils.isEmpty(etDateOfRegistration.getText())) {
            etDateOfRegistration.setError("Field Required");
            return false;
        }
        if (!isValidDateFormat(etDateOfRegistration.getText().toString().trim())) {
            etDateOfRegistration.setError("Invalid format. Use YYYY-MM-DD");
            return false;
        }

        if (TextUtils.isEmpty(etFamilySerialNumber.getText())) {
            etFamilySerialNumber.setError("Field Required");
            return false;
        }

        if (TextUtils.isEmpty(etDateOfBirth.getText())) {
            etDateOfBirth.setError("Field Required");
            return false;
        }
        if (!isValidDateFormat(etDateOfBirth.getText().toString().trim())) {
            etDateOfBirth.setError("Invalid format. Use YYYY-MM-DD");
            return false;
        }

        String nbeRdtStr = etDateNbeRdt.getText().toString().trim();
        if (!nbeRdtStr.isEmpty() && !isValidDateFormat(nbeRdtStr)) {
            etDateNbeRdt.setError("Invalid format. Use YYYY-MM-DD");
            return false;
        }

        String albStr = etAlbendazoleDateGiven.getText().toString().trim();
        if (!albStr.isEmpty() && !isValidDateFormat(albStr)) {
            etAlbendazoleDateGiven.setError("Invalid format. Use YYYY-MM-DD");
            return false;
        }

        String decStr = etDecDateGiven.getText().toString().trim();
        if (!decStr.isEmpty() && !isValidDateFormat(decStr)) {
            etDecDateGiven.setError("Invalid format. Use YYYY-MM-DD");
            return false;
        }

        String ivmStr = etIvermectinDateGiven.getText().toString().trim();
        if (!ivmStr.isEmpty() && !isValidDateFormat(ivmStr)) {
            etIvermectinDateGiven.setError("Invalid format. Use YYYY-MM-DD");
            return false;
        }

        return true;
    }

    private void loadRecordData() {
        executorService.execute(() -> {
            FilariasisRegistryRecord record = dbHelper.filariasisDao().getRecordById(currentRecordId);
            if (record != null && getActivity() != null) {
                requireActivity().runOnUiThread(() -> populateUIElements(record));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void populateUIElements(FilariasisRegistryRecord record) {
        // ---> LOAD PROFILE ID <---
        selectedProfileId = record.getProfileId();

        etDateOfRegistration.setText(record.getDateOfRegistration());
        etFamilySerialNumber.setText(record.getFamilySerialNumber());

        // Populate the autocomplete without showing popup filtering UI immediately
        etName.setText(record.getName(), false);

        etAddress.setText(record.getAddress());
        etDateOfBirth.setText(record.getDateOfBirth());
        etAge.setText(String.valueOf(record.getAge()));

        if (record.getAgeGroup() != null && spinnerAgeGroup.getAdapter() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerAgeGroup.getAdapter();
            int pos = adapter.getPosition(record.getAgeGroup());
            if (pos >= 0) spinnerAgeGroup.setSelection(pos);
        }

        if ("M".equals(record.getSex())) rgSex.check(R.id.rbMale);
        else if ("F".equals(record.getSex())) rgSex.check(R.id.rbFemale);

        cbNbePerformed.setChecked(record.isNbePerformed());
        cbRdtPerformed.setChecked(record.isRdtPerformed());
        etDateNbeRdt.setText(record.getDateNbeRdt());

        if ("1".equals(record.getBloodTestResult())) rgBloodTestResult.check(R.id.rbResultPositive);
        else if ("2".equals(record.getBloodTestResult())) rgBloodTestResult.check(R.id.rbResultNegative);

        if (record.getLymphedemaExaminedFirstTime() != null) {
            rgLymphedemaExamined.check("1".equals(record.getLymphedemaExaminedFirstTime()) ? R.id.rbLymphedemaExaminedYes : R.id.rbLymphedemaExaminedNo);
        }
        cbLymphedemaPresent.setChecked(record.isHasLymphedema());

        if (record.getElephantiasisExaminedFirstTime() != null) {
            rgElephantiasisExamined.check("1".equals(record.getElephantiasisExaminedFirstTime()) ? R.id.rbElephantiasisExaminedYes : R.id.rbElephantiasisExaminedNo);
        }
        cbElephantiasisPresent.setChecked(record.isHasElephantiasis());

        if (record.getHydroceleExaminedFirstTime() != null) {
            rgHydroceleExamined.check("1".equals(record.getHydroceleExaminedFirstTime()) ? R.id.rbHydroceleExaminedYes : R.id.rbHydroceleExaminedNo);
        }
        cbHydrocelePresent.setChecked(record.isHasHydrocele());

        etAlbendazoleDateGiven.setText(record.getAlbendazoleDateGiven());
        etDecDateGiven.setText(record.getDecDateGiven());
        etIvermectinDateGiven.setText(record.getIvermectinDateGiven());
        etRemarks.setText(record.getRemarks());
    }

    private void saveOrUpdateData() {
        if (!validateFields()) return;

        FilariasisRegistryRecord record = new FilariasisRegistryRecord();
        if (currentRecordId != -1) record.setId(currentRecordId);
        String PREFS_NAME = "AppPrefs";
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        record.setUserId(userId);

        // ---> SET PROFILE ID ON RECORD <---
        record.setProfileId(selectedProfileId);

        record.setDateOfRegistration(etDateOfRegistration.getText().toString().trim());
        record.setFamilySerialNumber(etFamilySerialNumber.getText().toString().trim());
        record.setName(etName.getText().toString().trim());
        record.setAddress(etAddress.getText().toString().trim());
        record.setDateOfBirth(etDateOfBirth.getText().toString().trim());

        try {
            record.setAge(Integer.parseInt(etAge.getText().toString().trim()));
        } catch (Exception e) { record.setAge(0); }

        record.setAgeGroup(spinnerAgeGroup.getSelectedItemPosition() > 0 ? spinnerAgeGroup.getSelectedItem().toString() : null);
        record.setSex(rgSex.getCheckedRadioButtonId() == R.id.rbMale ? "M" : rgSex.getCheckedRadioButtonId() == R.id.rbFemale ? "F" : null);

        record.setNbePerformed(cbNbePerformed.isChecked());
        record.setRdtPerformed(cbRdtPerformed.isChecked());
        record.setDateNbeRdt(etDateNbeRdt.getText().toString().trim());
        record.setBloodTestResult(rgBloodTestResult.getCheckedRadioButtonId() == R.id.rbResultPositive ? "1" : rgBloodTestResult.getCheckedRadioButtonId() == R.id.rbResultNegative ? "2" : null);

        if (rgLymphedemaExamined.getCheckedRadioButtonId() != -1) {
            record.setLymphedemaExaminedFirstTime(rgLymphedemaExamined.getCheckedRadioButtonId() == R.id.rbLymphedemaExaminedYes ? "1" : "2");
            record.setHasLymphedema(cbLymphedemaPresent.isChecked());
        } else {
            record.setLymphedemaExaminedFirstTime(null);
            record.setHasLymphedema(false);
        }

        if (rgElephantiasisExamined.getCheckedRadioButtonId() != -1) {
            record.setElephantiasisExaminedFirstTime(rgElephantiasisExamined.getCheckedRadioButtonId() == R.id.rbElephantiasisExaminedYes ? "1" : "2");
            record.setHasElephantiasis(cbElephantiasisPresent.isChecked());
        } else {
            record.setElephantiasisExaminedFirstTime(null);
            record.setHasElephantiasis(false);
        }

        if (rgHydroceleExamined.getCheckedRadioButtonId() != -1) {
            record.setHydroceleExaminedFirstTime(rgHydroceleExamined.getCheckedRadioButtonId() == R.id.rbHydroceleExaminedYes ? "1" : "2");
            record.setHasHydrocele(cbHydrocelePresent.isChecked());
        } else {
            record.setHydroceleExaminedFirstTime(null);
            record.setHasHydrocele(false);
        }

        record.setAlbendazoleDateGiven(etAlbendazoleDateGiven.getText().toString().trim());
        record.setDecDateGiven(etDecDateGiven.getText().toString().trim());
        record.setIvermectinDateGiven(etIvermectinDateGiven.getText().toString().trim());
        record.setRemarks(etRemarks.getText().toString().trim());

        executorService.execute(() -> {
            FilariasisDao dao = dbHelper.filariasisDao();
            if (currentRecordId == -1) {
                dao.insertRecord(record);
            } else {
                dao.updateRecord(record);
            }

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Record saved successfully", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
            }
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}