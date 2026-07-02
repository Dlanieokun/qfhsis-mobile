package com.android.hfsis.ncdpcs.mental;

import android.app.DatePickerDialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.database.ncdps.MentalHealthDao;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.ncdpcs.mental.MentalHealthRecord;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Fragment form for capturing or updating a single entry of the
 * "Target Client List for Mental Health" (TCL_MH) register.
 */
public class MentalHealthFragment extends Fragment {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String ARG_RECORD_ID = "record_id";

    private TextInputLayout tilDateOfAssessment, tilDateOfBirth, tilName;
    private EditText etDateOfAssessment;
    private EditText etFamilySerialNumber;
    private AutoCompleteTextView etName; // Converted to AutoCompleteTextView
    private EditText etAddress;
    private EditText etDateOfBirth;
    private EditText etAge;
    private AutoCompleteTextView spinnerAgeGroup;
    private RadioGroup rgSex;
    private SwitchMaterial switchMhgap;
    private Button btnSave;

    private final List<String> ageGroups = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
    private DatabaseHelper database;
    private boolean isUpdatingAge = false;

    // Tracks if we are editing an existing record or creating a new one
    private long existingRecordId = -1;
    private int selectedProfileId = 0; // Tracks the linked household member ID

    public MentalHealthFragment() {
        // Required empty public constructor
    }

    // Factory method providing argument mapping for modification workflows
    public static MentalHealthFragment newInstance(long recordId) {
        MentalHealthFragment fragment = new MentalHealthFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RECORD_ID, recordId);
        fragment.setArguments(args);
        return fragment;
    }

    public static MentalHealthFragment newInstance() {
        return new MentalHealthFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mental_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = DatabaseHelper.getInstance(requireContext());

        bindViews(view);
        setupNameAutocomplete();
        setupAgeGroupSpinner();
        setupDatePickers();
        setupLiveTextListeners();
        setupSaveButton();

        // If an ID was supplied, load data from the DB to populate fields for editing
        if (existingRecordId != -1) {
            loadExistingRecord(existingRecordId);
        }else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etDateOfAssessment.setText(sdf.format(new Date()));
        }
    }

    private void bindViews(View view) {
        tilDateOfAssessment = view.findViewById(R.id.tilDateOfAssessment);
        etDateOfAssessment = view.findViewById(R.id.etDateOfAssessment);
        etFamilySerialNumber = view.findViewById(R.id.etFamilySerialNumber);
        tilName = view.findViewById(R.id.tilName);
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        tilDateOfBirth = view.findViewById(R.id.tilDateOfBirth);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        etAge = view.findViewById(R.id.etAge);
        spinnerAgeGroup = view.findViewById(R.id.spinnerAgeGroup);
        rgSex = view.findViewById(R.id.rgSex);
        switchMhgap = view.findViewById(R.id.switchMhgap);
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

        // Wipes profile matching reference link constraint if the user clears input manually
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
                    selectedProfileId = profile.id; // Store reference pointer

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
                                Calendar dob = Calendar.getInstance();
                                int year = Integer.parseInt(dobParts[0]);
                                int month = Integer.parseInt(dobParts[1]) - 1;
                                int day = Integer.parseInt(dobParts[2]);
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
        ageGroups.clear();
        ageGroups.add("A: 0-9 yo");
        ageGroups.add("B: 10-19 yo");
        ageGroups.add("C: 20-59 yo");
        ageGroups.add("D: 60 yo and above");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, ageGroups);
        spinnerAgeGroup.setAdapter(adapter);
    }

    private void setupDatePickers() {
        tilDateOfAssessment.setStartIconOnClickListener(v -> showDatePicker(etDateOfAssessment, false));
        tilDateOfBirth.setStartIconOnClickListener(v -> showDatePicker(etDateOfBirth, true));
    }

    private void showDatePicker(EditText target, boolean isDateOfBirth) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    target.setText(dateFormat.format(selected.getTime()));
                    target.setError(null);

                    if (isDateOfBirth) {
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

    private void setupLiveTextListeners() {
        etDateOfAssessment.addTextChangedListener(new DateFormattingWatcher(etDateOfAssessment));
        etDateOfBirth.addTextChangedListener(new DateFormattingWatcher(etDateOfBirth));

        etDateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingAge) return;
                String input = s.toString();
                if (input.length() == 10) {
                    try {
                        Calendar dob = Calendar.getInstance();
                        dob.setTime(dateFormat.parse(input));
                        updateAgeFromDateOfBirth(dob);
                    } catch (ParseException ignored) {}
                }
            }
        });

        etAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingAge) return;
                String ageText = s.toString().trim();
                if (!ageText.isEmpty()) {
                    try {
                        int age = Integer.parseInt(ageText);
                        updateAgeGroupSpinner(age);
                    } catch (NumberFormatException ignored) {}
                }
            }
        });
    }

    private void updateAgeFromDateOfBirth(Calendar dob) {
        isUpdatingAge = true;
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        if (age < 0) age = 0;

        etAge.setText(String.valueOf(age));
        updateAgeGroupSpinner(age);
        isUpdatingAge = false;
    }

    private void updateAgeGroupSpinner(int age) {
        int index;
        if (age <= 9) index = 0;
        else if (age <= 19) index = 1;
        else if (age <= 59) index = 2;
        else index = 3;

        if (index < ageGroups.size()) {
            spinnerAgeGroup.setText(ageGroups.get(index), false);
        }
    }

    private void loadExistingRecord(long recordId) {
        new Thread(() -> {
            MentalHealthDao dao = database.mentalHealthDao();
            MentalHealthRecord record = dao.getRecordById(recordId);
            if (record != null) {
                requireActivity().runOnUiThread(() -> populateForm(record));
            }
        }).start();
    }

    private void populateForm(MentalHealthRecord record) {
        etDateOfAssessment.setText(record.getDateOfAssessment());
        etFamilySerialNumber.setText(record.getFamilySerialNumber());
        etName.setText(record.getName(), false); // Dropdown format protection parameter insertion
        etAddress.setText(record.getAddress());
        etDateOfBirth.setText(record.getDateOfBirth());
        etAge.setText(String.valueOf(record.getAge()));

        // Map code values back to selection descriptions
        String groupCode = record.getAgeGroup();
        if ("A".equals(groupCode)) spinnerAgeGroup.setText(ageGroups.get(0), false);
        else if ("B".equals(groupCode)) spinnerAgeGroup.setText(ageGroups.get(1), false);
        else if ("C".equals(groupCode)) spinnerAgeGroup.setText(ageGroups.get(2), false);
        else if ("D".equals(groupCode)) spinnerAgeGroup.setText(ageGroups.get(3), false);

        if ("M".equalsIgnoreCase(record.getSex())) {
            rgSex.check(R.id.rbMale);
        } else if ("F".equalsIgnoreCase(record.getSex())) {
            rgSex.check(R.id.rbFemale);
        }

        switchMhgap.setChecked(record.isScreenedMhgap());
        btnSave.setText("Update Record"); // Visual change indicating update action
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                MentalHealthRecord record = buildRecordFromForm();
                saveRecordToDatabase(record);
            }
        });
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(etDateOfAssessment.getText())) {
            Toast.makeText(requireContext(), "Please select the date of assessment", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etFamilySerialNumber.getText())) {
            etFamilySerialNumber.setError("Family serial number is required");
            return false;
        }
        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(etAddress.getText())) {
            etAddress.setError("Address is required");
            return false;
        }
        if (TextUtils.isEmpty(etDateOfBirth.getText())) {
            Toast.makeText(requireContext(), "Please select the date of birth", Toast.LENGTH_SHORT).show();
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
        return true;
    }

    private MentalHealthRecord buildRecordFromForm() {
        MentalHealthRecord record = new MentalHealthRecord();

        // Retain primary key if editing an existing record
        if (existingRecordId != -1) {
            record.setRecordNo((int) existingRecordId);
        }

        record.setDateOfAssessment(etDateOfAssessment.getText().toString());
        record.setFamilySerialNumber(etFamilySerialNumber.getText().toString().trim());
        record.setName(etName.getText().toString().trim());
        record.setAddress(etAddress.getText().toString().trim());
        record.setDateOfBirth(etDateOfBirth.getText().toString());

        int age = 0;
        try {
            age = Integer.parseInt(etAge.getText().toString().trim());
        } catch (NumberFormatException ignored) {}
        record.setAge(age);

        String[] ageGroupCodes = {"A", "B", "C", "D"};
        String currentSelection = spinnerAgeGroup.getText().toString();
        int ageGroupPosition = ageGroups.indexOf(currentSelection);
        record.setAgeGroup(ageGroupCodes[Math.max(ageGroupPosition, 0)]);

        record.setSex(rgSex.getCheckedRadioButtonId() == R.id.rbMale ? "M" : "F");
        record.setScreenedMhgap(switchMhgap.isChecked());

        return record;
    }

    private void saveRecordToDatabase(MentalHealthRecord record) {
        new Thread(() -> {
            try {
                MentalHealthDao dao = database.mentalHealthDao();

                if (existingRecordId != -1) {
                    // Update current entry
                    dao.update(record);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Record updated successfully!", Toast.LENGTH_SHORT).show();
                        // Navigate back after update completes
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    });
                } else {
                    // Insert a new entry
                    dao.insert(record);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Mental Health record saved successfully!", Toast.LENGTH_SHORT).show();
                        resetForm();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Database Save Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void resetForm() {
        etDateOfAssessment.setText("");
        etFamilySerialNumber.setText("");
        etName.setText("");
        etAddress.setText("");
        etDateOfBirth.setText("");
        etAge.setText("");
        spinnerAgeGroup.setText("", false);
        rgSex.clearCheck();
        switchMhgap.setChecked(false);
        existingRecordId = -1;
        selectedProfileId = 0;
        btnSave.setText("Save Record");
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
}