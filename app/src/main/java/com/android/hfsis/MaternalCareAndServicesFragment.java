package com.android.hfsis;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.MaternalCareRecord;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MaternalCareAndServicesFragment extends Fragment {

    private int selectedProfileId = -1;
    private int editRecordId = -1;
    private boolean isEditMode = false;

    private DatabaseHelper db;

    private EditText etMaternalRegDate, etMaternalSerial, etMaternalAddress, etMaternalAge, etMaternalBirthDate;
    private TextInputLayout tilMaternalRegDate, tilMaternalBirthDate, tilMaternalLMP;
    private AutoCompleteTextView etMaternalName;
    private AutoCompleteTextView spinnerMaternalAgeGroup;
    private EditText etMaternalLMP, etMaternalGP, etMaternalEDD;
    private EditText etMaternalWeight, etMaternalHeight;
    private TextView tvMaternalBmiScore, tvMaternalBmiStatus;
    private Button btnSubmitMaternal;

    private final List<String> patientNamesAdapterList = new ArrayList<>();
    private ArrayAdapter<String> nameAdapter;

    public MaternalCareAndServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            editRecordId = getArguments().getInt("edit_record_id", -1);
            isEditMode = editRecordId != -1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maternal_care_and_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getDatabase(requireContext());

        // Bind layout views
        tilMaternalRegDate = view.findViewById(R.id.tilMaternalRegDate);
        tilMaternalBirthDate = view.findViewById(R.id.tilMaternalBirthDate);
        tilMaternalLMP = view.findViewById(R.id.tilMaternalLMP);

        etMaternalRegDate = view.findViewById(R.id.etMaternalRegDate);
        etMaternalSerial = view.findViewById(R.id.etMaternalSerial);
        etMaternalName = view.findViewById(R.id.etMaternalName);
        etMaternalAddress = view.findViewById(R.id.etMaternalAddress);
        etMaternalBirthDate = view.findViewById(R.id.etMaternalBirthDate);
        etMaternalAge = view.findViewById(R.id.etMaternalAge);
        spinnerMaternalAgeGroup = view.findViewById(R.id.spinnerMaternalAgeGroup);
        etMaternalLMP = view.findViewById(R.id.etMaternalLMP);
        etMaternalGP = view.findViewById(R.id.etMaternalGP);
        etMaternalEDD = view.findViewById(R.id.etMaternalEDD);
        etMaternalWeight = view.findViewById(R.id.etMaternalWeight);
        etMaternalHeight = view.findViewById(R.id.etMaternalHeight);
        tvMaternalBmiScore = view.findViewById(R.id.tvMaternalBmiScore);
        tvMaternalBmiStatus = view.findViewById(R.id.tvMaternalBmiStatus);
        btnSubmitMaternal = view.findViewById(R.id.btnSubmitMaternal);

        setupAgeGroupSpinner();
        setupPatientAutocomplete();
        setupDatePickers();
        setupBmiCalculator();

        if (isEditMode) {
            loadRecordForEditing();
        }else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etMaternalRegDate.setText(sdf.format(new Date()));
        }

        btnSubmitMaternal.setOnClickListener(v -> saveMaternalRecord());
    }

    private void setupAgeGroupSpinner() {
        String[] groups = {"10-14", "15-19", "20-49", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, groups);
        spinnerMaternalAgeGroup.setAdapter(adapter);
    }

    private void setupPatientAutocomplete() {
        nameAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, patientNamesAdapterList);
        etMaternalName.setAdapter(nameAdapter);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> names = db.householdProfileDao().getAllHouseholdNames();
            if (names != null && !names.isEmpty()) {
                patientNamesAdapterList.clear();
                patientNamesAdapterList.addAll(names);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> nameAdapter.notifyDataSetChanged());
                }
            }
        });

        etMaternalName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCalculatedName = (String) parent.getItemAtPosition(position);

            Executors.newSingleThreadExecutor().execute(() -> {
                HouseholdProfile profile = db.householdProfileDao().getProfileByCalculatedName(selectedCalculatedName);

                if (profile != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        selectedProfileId = profile.id;
                        etMaternalSerial.setText(profile.hhNumber);

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
                        etMaternalAddress.setText(fullAddress.toString());

                        if (profile.dob != null && !profile.dob.isEmpty()) {
                            etMaternalBirthDate.setText(profile.dob);
                            calculateAgeFromBirthdate(profile.dob);
                        }
                    });
                }
            });
        });
    }

    private void setupDatePickers() {
        // Use End Icons for Pickers just like ProfilingFragment
        tilMaternalRegDate.setEndIconOnClickListener(v -> showDatePicker(etMaternalRegDate, null));
        tilMaternalBirthDate.setEndIconOnClickListener(v -> showDatePicker(etMaternalBirthDate, this::calculateAgeFromBirthdate));
        tilMaternalLMP.setEndIconOnClickListener(v -> showDatePicker(etMaternalLMP, this::calculateEddFromLmp));

        // Format keyboard inputs while typing manually
        etMaternalRegDate.addTextChangedListener(new DateFormattingWatcher(etMaternalRegDate));
        etMaternalBirthDate.addTextChangedListener(new DateFormattingWatcher(etMaternalBirthDate));
        etMaternalLMP.addTextChangedListener(new DateFormattingWatcher(etMaternalLMP));

        // Evaluate automated calculations inline when full string lengths are supplied manually
        etMaternalBirthDate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    calculateAgeFromBirthdate(s.toString());
                }
            }
        });

        etMaternalLMP.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    calculateEddFromLmp(s.toString());
                }
            }
        });
    }

    private void showDatePicker(EditText targetField, @Nullable OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar selCalendar = Calendar.getInstance();
            selCalendar.set(Calendar.YEAR, year);
            selCalendar.set(Calendar.MONTH, month);
            selCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String selectedDate = sdf.format(selCalendar.getTime());
            targetField.setText(selectedDate);
            targetField.setError(null);

            if (listener != null) {
                listener.onDateSelected(selectedDate);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    interface OnDateSelectedListener {
        void onDateSelected(String dateStr);
    }

    private void calculateAgeFromBirthdate(String birthDateStr) {
        if (birthDateStr == null || birthDateStr.trim().isEmpty()) return;

        Date birthDate = null;

        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            birthDate = dbFormat.parse(birthDateStr);
        } catch (ParseException e) {
            try {
                SimpleDateFormat pickerFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                birthDate = pickerFormat.parse(birthDateStr);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }

        if (birthDate == null) return;

        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        etMaternalAge.setText(String.valueOf(age));
        autoSelectAgeGroup(age);
    }

    private void autoSelectAgeGroup(int age) {
        String groupValue = "Other";
        if (age >= 10 && age <= 14) groupValue = "10-14";
        else if (age >= 15 && age <= 19) groupValue = "15-19";
        else if (age >= 20 && age <= 49) groupValue = "20-49";

        spinnerMaternalAgeGroup.setText(groupValue, false);
    }

    private void calculateEddFromLmp(String lmpDateStr) {
        if (lmpDateStr == null || lmpDateStr.trim().isEmpty()) return;
        Date lmpDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            lmpDate = sdf.parse(lmpDateStr);
        } catch (ParseException e) {
            try {
                SimpleDateFormat sdfLegacy = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                lmpDate = sdfLegacy.parse(lmpDateStr);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }

        if (lmpDate == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lmpDate);
        calendar.add(Calendar.DAY_OF_YEAR, 280);

        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String eddStr = outputFormat.format(calendar.getTime());
        etMaternalEDD.setText(eddStr);
    }

    private void setupBmiCalculator() {
        TextWatcher bmiWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { computeBmi(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etMaternalWeight.addTextChangedListener(bmiWatcher);
        etMaternalHeight.addTextChangedListener(bmiWatcher);
    }

    private void computeBmi() {
        String weightStr = etMaternalWeight.getText().toString().trim();
        String heightStr = etMaternalHeight.getText().toString().trim();

        if (weightStr.isEmpty() || heightStr.isEmpty()) {
            tvMaternalBmiScore.setText("0.0");
            tvMaternalBmiStatus.setText("Awaiting Inputs");
            return;
        }

        try {
            double weight = Double.parseDouble(weightStr);
            double heightCm = Double.parseDouble(heightStr);

            if (heightCm <= 0) return;

            double heightMeters = heightCm / 100.0;
            double bmi = weight / (heightMeters * heightMeters);

            tvMaternalBmiScore.setText(String.format(Locale.US, "%.1f", bmi));

            String status;
            if (bmi < 18.5) status = "Underweight";
            else if (bmi >= 18.5 && bmi < 25.0) status = "Normal Weight";
            else if (bmi >= 25.0 && bmi < 30.0) status = "Overweight";
            else status = "Obese";

            tvMaternalBmiStatus.setText(status);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void loadRecordForEditing() {
        Executors.newSingleThreadExecutor().execute(() -> {
            MaternalCareRecord record = db.maternalCareDao().getMaternalRecordById(editRecordId);
            if (record != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    selectedProfileId = record.profileId;
                    etMaternalRegDate.setText(record.registrationDate);
                    etMaternalSerial.setText(record.familySerialNumber);
                    etMaternalName.setText(record.patientName, false);
                    etMaternalAddress.setText(record.homeAddress);
                    etMaternalBirthDate.setText(record.birthDate);
                    etMaternalAge.setText(String.valueOf(record.age));
                    spinnerMaternalAgeGroup.setText(record.ageGroup, false);
                    etMaternalLMP.setText(record.lmpDate);
                    etMaternalGP.setText(record.gravidaPara);
                    etMaternalEDD.setText(record.eddDate);
                    etMaternalWeight.setText(String.valueOf(record.weightKg));
                    etMaternalHeight.setText(String.valueOf(record.heightCm));
                    tvMaternalBmiScore.setText(record.bmiValue);
                    tvMaternalBmiStatus.setText(record.bmiStatus);
                });
            }
        });
    }

    private void saveMaternalRecord() {
        String patientName = etMaternalName.getText().toString().trim();
        String trackingNo = etMaternalSerial.getText().toString().trim();

        if (patientName.isEmpty()) {
            Toast.makeText(getActivity(), "Please input or select a patient profile name", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            MaternalCareRecord record = new MaternalCareRecord();
            if (isEditMode) {
                record.id = editRecordId;
            }

            int ageValue = 0;
            try { ageValue = Integer.parseInt(etMaternalAge.getText().toString().trim()); } catch (Exception ignored) {}

            double weightValue = 0.0, heightValue = 0.0;
            try { weightValue = Double.parseDouble(etMaternalWeight.getText().toString().trim()); } catch (Exception ignored) {}
            try { heightValue = Double.parseDouble(etMaternalHeight.getText().toString().trim()); } catch (Exception ignored) {}

            record.profileId = selectedProfileId;
            record.registrationDate = etMaternalRegDate.getText().toString().trim();
            record.familySerialNumber = trackingNo;
            record.patientName = patientName;
            record.homeAddress = etMaternalAddress.getText().toString().trim();
            record.age = ageValue;
            record.ageGroup = spinnerMaternalAgeGroup.getText().toString().trim();
            record.birthDate = etMaternalBirthDate.getText().toString().trim();
            record.lmpDate = etMaternalLMP.getText().toString().trim();
            record.gravidaPara = etMaternalGP.getText().toString().trim();
            record.eddDate = etMaternalEDD.getText().toString().trim();
            record.weightKg = weightValue;
            record.heightCm = heightValue;
            record.bmiValue = tvMaternalBmiScore.getText().toString().trim();
            record.bmiStatus = tvMaternalBmiStatus.getText().toString().trim();

            if (isEditMode) {
                db.maternalCareDao().updateMaternalRecord(record);
            } else {
                db.maternalCareDao().insertMaternalRecord(record);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String statusText = isEditMode ? "updated" : "saved";
                    Toast.makeText(getActivity(), "Maternal health record " + statusText + " successfully!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
            }
        });
    }

    // Shared Date Mask Watcher Component
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