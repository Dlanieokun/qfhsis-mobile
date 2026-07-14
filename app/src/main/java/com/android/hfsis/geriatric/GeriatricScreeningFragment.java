package com.android.hfsis.geriatric;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.geriatric.GeriatricScreeningRecord;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class GeriatricScreeningFragment extends Fragment {

    private static final String ARG_RECORD_ID = "record_id";
    private static final String DATE_PATTERN = "yyyy-MM-dd"; // Updated from MM/dd/yyyy to ISO YYYY-MM-DD

    private long existingRecordId = -1;
    private int selectedProfileId = 0; // ---> ADDED VARIABLE TO TRACK PROFILE ID
    private DatabaseHelper database;

    // Client/Admin Information Views
    private EditText etDateOfScreening, etFamilySerialNumber, etAddress, etDateOfBirth, etAge;
    // Added TextInputLayout fields for date container tracking
    private TextInputLayout tilDateOfScreening, tilDateOfBirth, tilPpvDateGiven, tilInfluenzaDateGiven;
    private AutoCompleteTextView etName;
    private RadioGroup rgSex;
    private RadioButton rbMale, rbFemale;

    // Screening Results CheckBoxes
    private CheckBox cbResultNegative, cbResultMemory, cbResultDepression, cbResultPolypharmacy,
            cbResultUrinary, cbResultFunctional, cbResultFall, cbResultMalnutrition,
            cbResultHearing, cbResultVision;

    // Care Plan and Immunization Views
    private SwitchMaterial switchCarePlanProvided, switchPpvReceived;
    private EditText etPpvDateGiven, etInfluenzaDateGiven, etRemarks;
    private Button btnSave;

    private final Calendar calendar = Calendar.getInstance();

    public static GeriatricScreeningFragment newInstance(long recordId) {
        GeriatricScreeningFragment fragment = new GeriatricScreeningFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RECORD_ID, recordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = DatabaseHelper.getInstance(requireContext());
        if (getArguments() != null) {
            existingRecordId = getArguments().getLong(ARG_RECORD_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geriatric_screening, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupListeners();
        setupNameAutocomplete();

        if (existingRecordId != -1) {
            loadExistingRecord();
        } else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etDateOfScreening.setText(sdf.format(new Date()));
        }
    }

    private void initializeViews(View view) {
        // Find date TextInputLayout views
        tilDateOfScreening = view.findViewById(R.id.tilDateOfScreening);
        etDateOfScreening = view.findViewById(R.id.etDateOfScreening);
        etFamilySerialNumber = view.findViewById(R.id.etFamilySerialNumber);
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        tilDateOfBirth = view.findViewById(R.id.tilDateOfBirth);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        etAge = view.findViewById(R.id.etAge);
        rgSex = view.findViewById(R.id.rgSex);
        rbMale = view.findViewById(R.id.rbMale);
        rbFemale = view.findViewById(R.id.rbFemale);

        cbResultNegative = view.findViewById(R.id.cbResultNegative);
        cbResultMemory = view.findViewById(R.id.cbResultMemory);
        cbResultDepression = view.findViewById(R.id.cbResultDepression);
        cbResultPolypharmacy = view.findViewById(R.id.cbResultPolypharmacy);
        cbResultUrinary = view.findViewById(R.id.cbResultUrinary);
        cbResultFunctional = view.findViewById(R.id.cbResultFunctional);
        cbResultFall = view.findViewById(R.id.cbResultFall);
        cbResultMalnutrition = view.findViewById(R.id.cbResultMalnutrition);
        cbResultHearing = view.findViewById(R.id.cbResultHearing);
        cbResultVision = view.findViewById(R.id.cbResultVision);

        switchCarePlanProvided = view.findViewById(R.id.switchCarePlanProvided);
        switchPpvReceived = view.findViewById(R.id.switchPpvReceived);
        tilPpvDateGiven = view.findViewById(R.id.tilPpvDateGiven);
        etPpvDateGiven = view.findViewById(R.id.etPpvDateGiven);
        tilInfluenzaDateGiven = view.findViewById(R.id.tilInfluenzaDateGiven);
        etInfluenzaDateGiven = view.findViewById(R.id.etInfluenzaDateGiven);
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

        etName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            autoPopulateFromProfile(selectedName);
        });

        // ---> Wipes profile matching reference link constraint if user clears input manually
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
        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = database.householdProfileDao().getProfileByCalculatedName(fullCalculatedName);
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {

                    // ---> CAPTURE PROFILE ID <---
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
                        rbMale.setChecked(true);
                    } else if ("F".equalsIgnoreCase(profile.sex) || "Female".equalsIgnoreCase(profile.sex)) {
                        rbFemale.setChecked(true);
                    }

                    if (!TextUtils.isEmpty(profile.dob)) {
                        etDateOfBirth.setText(profile.dob);
                        calculateAge();
                    }
                });
            }
        });
    }

    private void setupListeners() {
        // Date Pickers assigned to the Start Icon of the TextInputLayout wrapper layout containers
        if (tilDateOfScreening != null) tilDateOfScreening.setStartIconOnClickListener(v -> showDatePickerDialog(etDateOfScreening));
        if (tilDateOfBirth != null) tilDateOfBirth.setStartIconOnClickListener(v -> showDatePickerDialog(etDateOfBirth));
        if (tilPpvDateGiven != null) tilPpvDateGiven.setStartIconOnClickListener(v -> showDatePickerDialog(etPpvDateGiven));
        if (tilInfluenzaDateGiven != null) tilInfluenzaDateGiven.setStartIconOnClickListener(v -> showDatePickerDialog(etInfluenzaDateGiven));

        // Format mask text watchers attached to the underlying edit texts
        etDateOfScreening.addTextChangedListener(new DateFormattingWatcher(etDateOfScreening));
        etDateOfBirth.addTextChangedListener(new DateFormattingWatcher(etDateOfBirth));
        etPpvDateGiven.addTextChangedListener(new DateFormattingWatcher(etPpvDateGiven));
        etInfluenzaDateGiven.addTextChangedListener(new DateFormattingWatcher(etInfluenzaDateGiven));

        // Mutually exclusive checkbox handling for Screening Results
        cbResultNegative.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                clearAndDisableAilments(false);
            } else {
                clearAndDisableAilments(true);
            }
        });

        btnSave.setOnClickListener(v -> saveRecord());
    }

    private void showDatePickerDialog(EditText editText) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
            editText.setText(sdf.format(calendar.getTime()));
            editText.setError(null);

            if (editText == etDateOfBirth) {
                calculateAge();
            }
        };

        new DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void calculateAge() {
        String dobStr = etDateOfBirth.getText().toString().trim();
        if (TextUtils.isEmpty(dobStr)) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
            Calendar dob = Calendar.getInstance();
            dob.setTime(sdf.parse(dobStr));
            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            etAge.setText(String.valueOf(Math.max(0, age)));
        } catch (Exception ignored) {}
    }

    private void clearAndDisableAilments(boolean enabled) {
        CheckBox[] ailmentCheckBoxes = {
                cbResultMemory, cbResultDepression, cbResultPolypharmacy, cbResultUrinary,
                cbResultFunctional, cbResultFall, cbResultMalnutrition, cbResultHearing, cbResultVision
        };
        for (CheckBox cb : ailmentCheckBoxes) {
            if (!enabled) {
                cb.setChecked(false);
            }
            cb.setEnabled(enabled);
        }
    }

    private void loadExistingRecord() {
        new Thread(() -> {
            GeriatricScreeningRecord record = database.geriatricScreeningDao().getRecordById(existingRecordId);
            if (record != null) {
                requireActivity().runOnUiThread(() -> populateForm(record));
            }
        }).start();
    }

    private void populateForm(GeriatricScreeningRecord record) {
        // ---> LOAD PROFILE ID <---
        selectedProfileId = record.getProfileId();

        etDateOfScreening.setText(record.getDateOfScreening());
        etFamilySerialNumber.setText(record.getFamilySerialNumber());
        etName.setText(record.getName(), false);
        etAddress.setText(record.getAddress());
        etDateOfBirth.setText(record.getDateOfBirth());
        etAge.setText(String.valueOf(record.getAge()));

        if ("M".equals(record.getSex())) {
            rbMale.setChecked(true);
        } else if ("F".equals(record.getSex())) {
            rbFemale.setChecked(true);
        }

        String results = record.getResults();
        if ("0".equals(results)) {
            cbResultNegative.setChecked(true);
        } else if (results != null) {
            List<String> codes = Arrays.asList(results.split(","));
            cbResultMemory.setChecked(codes.contains("A"));
            cbResultDepression.setChecked(codes.contains("B"));
            cbResultPolypharmacy.setChecked(codes.contains("C"));
            cbResultUrinary.setChecked(codes.contains("D"));
            cbResultFunctional.setChecked(codes.contains("E"));
            cbResultFall.setChecked(codes.contains("F"));
            cbResultMalnutrition.setChecked(codes.contains("G"));
            cbResultHearing.setChecked(codes.contains("H"));
            cbResultVision.setChecked(codes.contains("I"));
        }

        switchCarePlanProvided.setChecked(record.isCarePlanProvided());
        switchPpvReceived.setChecked(record.isPpvReceivedAt60());
        etPpvDateGiven.setText(record.getPpvDateGiven());
        etInfluenzaDateGiven.setText(record.getInfluenzaDateGiven());
        etRemarks.setText(record.getRemarks());

        btnSave.setText("Update Record");
    }

    private void saveRecord() {
        if (!validateForm()) return;

        new Thread(() -> {
            GeriatricScreeningRecord record;
            if (existingRecordId != -1) {
                record = database.geriatricScreeningDao().getRecordById(existingRecordId);
            } else {
                record = new GeriatricScreeningRecord();
            }

            if (record != null) {
                String PREFS_NAME = "AppPrefs";
                SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);

                record.setUserId(userId);

                // ---> SET PROFILE ID ON RECORD <---
                record.setProfileId(selectedProfileId);

                record.setDateOfScreening(etDateOfScreening.getText().toString().trim());
                record.setFamilySerialNumber(etFamilySerialNumber.getText().toString().trim());
                record.setName(etName.getText().toString().trim());
                record.setAddress(etAddress.getText().toString().trim());
                record.setDateOfBirth(etDateOfBirth.getText().toString().trim());

                int age = 0;
                try {
                    age = Integer.parseInt(etAge.getText().toString().trim());
                } catch (NumberFormatException ignored) {}
                record.setAge(age);

                record.setSex(rgSex.getCheckedRadioButtonId() == R.id.rbMale ? "M" : "F");

                if (cbResultNegative.isChecked()) {
                    record.setResults("0");
                } else {
                    List<String> codes = new ArrayList<>();
                    if (cbResultMemory.isChecked()) codes.add("A");
                    if (cbResultDepression.isChecked()) codes.add("B");
                    if (cbResultPolypharmacy.isChecked()) codes.add("C");
                    if (cbResultUrinary.isChecked()) codes.add("D");
                    if (cbResultFunctional.isChecked()) codes.add("E");
                    if (cbResultFall.isChecked()) codes.add("F");
                    if (cbResultMalnutrition.isChecked()) codes.add("G");
                    if (cbResultHearing.isChecked()) codes.add("H");
                    if (cbResultVision.isChecked()) codes.add("I");
                    record.setResults(TextUtils.join(",", codes));
                }

                record.setCarePlanProvided(switchCarePlanProvided.isChecked());
                record.setPpvReceivedAt60(switchPpvReceived.isChecked());
                record.setPpvDateGiven(etPpvDateGiven.getText().toString().trim());
                record.setInfluenzaDateGiven(etInfluenzaDateGiven.getText().toString().trim());
                record.setRemarks(etRemarks.getText().toString().trim());

                if (existingRecordId != -1) {
                    database.geriatricScreeningDao().update(record);
                } else {
                    database.geriatricScreeningDao().insert(record);
                }

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Record saved successfully!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
            }
        }).start();
    }

    private boolean isValidDateFormat(String date) {
        return date.matches("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])");
    }

    private boolean validateForm() {
        String screeningDate = etDateOfScreening.getText().toString().trim();
        if (TextUtils.isEmpty(screeningDate)) {
            etDateOfScreening.setError("Required field");
            return false;
        }
        if (!isValidDateFormat(screeningDate)) {
            etDateOfScreening.setError("Invalid format. Use YYYY-MM-DD");
            etDateOfScreening.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etFamilySerialNumber.getText())) {
            etFamilySerialNumber.setError("Required field");
            return false;
        }
        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("Required field");
            return false;
        }

        String dobDate = etDateOfBirth.getText().toString().trim();
        if (TextUtils.isEmpty(dobDate)) {
            etDateOfBirth.setError("Required field");
            return false;
        }
        if (!isValidDateFormat(dobDate)) {
            etDateOfBirth.setError("Invalid format. Use YYYY-MM-DD");
            etDateOfBirth.requestFocus();
            return false;
        }

        String ppvDate = etPpvDateGiven.getText().toString().trim();
        if (!ppvDate.isEmpty() && !isValidDateFormat(ppvDate)) {
            etPpvDateGiven.setError("Invalid format. Use YYYY-MM-DD");
            etPpvDateGiven.requestFocus();
            return false;
        }

        String fluDate = etInfluenzaDateGiven.getText().toString().trim();
        if (!fluDate.isEmpty() && !isValidDateFormat(fluDate)) {
            etInfluenzaDateGiven.setError("Invalid format. Use YYYY-MM-DD");
            etInfluenzaDateGiven.requestFocus();
            return false;
        }

        if (rgSex.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Please select Sex classification", Toast.LENGTH_SHORT).show();
            return false;
        }

        boolean isAnyResultChecked = cbResultNegative.isChecked() || cbResultMemory.isChecked() ||
                cbResultDepression.isChecked() || cbResultPolypharmacy.isChecked() ||
                cbResultUrinary.isChecked() || cbResultFunctional.isChecked() ||
                cbResultFall.isChecked() || cbResultMalnutrition.isChecked() ||
                cbResultHearing.isChecked() || cbResultVision.isChecked();

        if (!isAnyResultChecked) {
            Toast.makeText(requireContext(), "Please check Negative or at least one positive screening symptom domain.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // Fixed Watcher implementation optimized to automatically inject hyphens for YYYY-MM-DD strings
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
}