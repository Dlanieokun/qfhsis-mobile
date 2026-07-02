package com.android.hfsis.ncdpcs.cancer;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.ncdpcs.CancerDataMapper;
import com.android.hfsis.model.ncdpcs.CervicalCancerScreeningEntity;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CervicalCancerScreeningFragment extends Fragment {

    // Tracking tracking record ID (0 = insertion, >0 = updating existing record)
    private long currentScreeningId = 0;
    private int selectedProfileId = 0; // Tracks the linked household member ID

    // ── Client Information ─────────────────────────────────────────────────
    private EditText etDateAssessment, etFamilySerial, etAddress, etDateOfBirth, etAge;
    private TextInputLayout tilName, tilDateAssessment, tilDateOfBirth;
    private AutoCompleteTextView etName; // Converted to AutoCompleteTextView

    // ── Cervical Cancer Dropdowns ──────────────────────────────────────────
    private AutoCompleteTextView spinnerCervicalScreeningDone;
    private AutoCompleteTextView spinnerCervicalResult;
    private AutoCompleteTextView spinnerCervicalLinkedToCare;

    // ── Breast Mass Examination Dropdowns ──────────────────────────────────
    private AutoCompleteTextView spinnerBreastRiskAssessment;
    private AutoCompleteTextView spinnerBreastAgeRisk;
    private AutoCompleteTextView spinnerBreastExamType;
    private AutoCompleteTextView spinnerBreastResult;
    private AutoCompleteTextView spinnerBreastLinkedToCare;

    private EditText etRemarks;
    private Button btnSubmit;

    private static final String[] CERVICAL_SCREENING_ITEMS = {
            "Select screening method", "3 – HPV DNA", "2 – Pap Smear", "1 – VIA (Visual Inspection with Acetic Acid)", "0 – Assessed only"
    };
    private static final String[] CERVICAL_RESULT_ITEMS = {"Select result", "2 – Positive", "1 – Suspicious CA", "0 – Negative"};
    private static final String[] CERVICAL_LINKED_ITEMS = {"Select", "2 – Referred (with referral form)", "1 – Treated", "0 – No"};
    private static final String[] BREAST_RISK_ITEMS = {"Select risk level", "2 – High-risk", "1 – Symptomatic", "0 – Asymptomatic"};
    private static final String[] BREAST_AGE_RISK_ITEMS = {
            "Select age-risk class", "A – 30 to 69 yrs old (high-risk or symptomatic)", "B – 50 to 69 yrs old (asymptomatic)"
    };
    private static final String[] BREAST_EXAM_ITEMS = {
            "Select examination type", "CBE – Clinical Breast Examination", "M – Mammogram or Ultrasound", "CBE + M – Both"
    };
    private static final String[] BREAST_RESULT_ITEMS = {
            "Select result", "3 – Remarkable for CBE", "2 – Unremarkable for CBE", "1 – BI-RADS 3–6 (Mammogram/Ultrasound)", "0 – BI-RADS 0–2 (Mammogram/Ultrasound)"
    };
    private static final String[] BREAST_LINKED_ITEMS = {"Select", "1 – Referred for further treatment or management", "0 – No"};

    public static CervicalCancerScreeningFragment newInstance() {
        return new CervicalCancerScreeningFragment();
    }

    // Setter to easily inject a record ID when navigating to this fragment for editing an existing record
    public void setCurrentScreeningId(long id) {
        this.currentScreeningId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cervical_cancer_screening, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupNameAutocomplete();
        setupDropdowns();
        setupDatePickersAndFormatters();
        setupSubmitButton();

        // Load existing record fields if editing an existing row
        if (currentScreeningId > 0) {
            loadExistingRecordData(currentScreeningId);
        }else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etDateAssessment.setText(sdf.format(new Date()));
        }
    }

    private void bindViews(View v) {
        tilDateAssessment   = v.findViewById(R.id.tilDateAssessment);
        etDateAssessment    = v.findViewById(R.id.etDateAssessment);
        etFamilySerial      = v.findViewById(R.id.etFamilySerial);
        tilName             = v.findViewById(R.id.tilName);
        etName              = v.findViewById(R.id.etName); // AutoCompleteTextView
        etAddress           = v.findViewById(R.id.etAddress);
        tilDateOfBirth      = v.findViewById(R.id.tilDateOfBirth);
        etDateOfBirth       = v.findViewById(R.id.etDateOfBirth);
        etAge               = v.findViewById(R.id.etAge);

        spinnerCervicalScreeningDone  = v.findViewById(R.id.spinnerCervicalScreeningDone);
        spinnerCervicalResult         = v.findViewById(R.id.spinnerCervicalResult);
        spinnerCervicalLinkedToCare   = v.findViewById(R.id.spinnerCervicalLinkedToCare);

        spinnerBreastRiskAssessment   = v.findViewById(R.id.spinnerBreastRiskAssessment);
        spinnerBreastAgeRisk          = v.findViewById(R.id.spinnerBreastAgeRisk);
        spinnerBreastExamType         = v.findViewById(R.id.spinnerBreastExamType);
        spinnerBreastResult           = v.findViewById(R.id.spinnerBreastResult);
        spinnerBreastLinkedToCare     = v.findViewById(R.id.spinnerBreastLinkedToCare);

        etRemarks  = v.findViewById(R.id.etRemarks);
        btnSubmit  = v.findViewById(R.id.btnSubmit);
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
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = dbHelper.householdProfileDao().getProfileByCalculatedName(fullCalculatedName);
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
                        etFamilySerial.setText(profile.hhNumber);
                    }

                    if (!TextUtils.isEmpty(profile.dob)) {
                        etDateOfBirth.setText(profile.dob);
                        try {
                            String[] dobParts = profile.dob.split("-");
                            if (dobParts.length == 3) {
                                int year = Integer.parseInt(dobParts[0]);
                                int month = Integer.parseInt(dobParts[1]) - 1;
                                int day = Integer.parseInt(dobParts[2]);
                                calculateAge(year, month, day);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void calculateAge(int birthYear, int birthMonth, int birthDay) {
        Calendar dob = Calendar.getInstance();
        dob.set(birthYear, birthMonth, birthDay);
        Calendar today = Calendar.getInstance();

        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
        if (months < 0 || (months == 0 && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))) {
            years--;
        }

        int totalYears = Math.max(0, years);
        etAge.setText(String.valueOf(totalYears));
    }

    private void setupDropdowns() {
        setDropdownAdapter(spinnerCervicalScreeningDone, CERVICAL_SCREENING_ITEMS);
        setDropdownAdapter(spinnerCervicalResult, CERVICAL_RESULT_ITEMS);
        setDropdownAdapter(spinnerCervicalLinkedToCare, CERVICAL_LINKED_ITEMS);
        setDropdownAdapter(spinnerBreastRiskAssessment, BREAST_RISK_ITEMS);
        setDropdownAdapter(spinnerBreastAgeRisk, BREAST_AGE_RISK_ITEMS);
        setDropdownAdapter(spinnerBreastExamType, BREAST_EXAM_ITEMS);
        setDropdownAdapter(spinnerBreastResult, BREAST_RESULT_ITEMS);
        setDropdownAdapter(spinnerBreastLinkedToCare, BREAST_LINKED_ITEMS);
    }

    private void setDropdownAdapter(AutoCompleteTextView menu, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, items);
        menu.setAdapter(adapter);
        if (items.length > 0) {
            menu.setText(items[0], false);
        }
    }

    private void setupDatePickersAndFormatters() {
        tilDateAssessment.setStartIconOnClickListener(v -> showDatePickerDialog(etDateAssessment));
        tilDateOfBirth.setStartIconOnClickListener(v -> showDatePickerDialog(etDateOfBirth));

        etDateAssessment.addTextChangedListener(new DateFormattingWatcher(etDateAssessment));
        etDateOfBirth.addTextChangedListener(new DateFormattingWatcher(etDateOfBirth));
    }

    private void showDatePickerDialog(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, selYear, selMonth, selDay) -> {
            Calendar selCalendar = Calendar.getInstance();
            selCalendar.set(Calendar.YEAR, selYear);
            selCalendar.set(Calendar.MONTH, selMonth);
            selCalendar.set(Calendar.DAY_OF_MONTH, selDay);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            targetEditText.setText(sdf.format(selCalendar.getTime()));
            targetEditText.setError(null);
        }, year, month, day);

        dialog.show();
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                CervicalCancerScreeningData data = collectFormData();
                onScreeningSubmitted(data);
            }
        });
    }

    private void loadExistingRecordData(long id) {
        new Thread(() -> {
            try {
                DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
                CervicalCancerScreeningEntity entity = db.cervicalCancerScreeningDao().getScreeningById(id);

                if (entity != null && isAdded()) {
                    requireActivity().runOnUiThread(() -> populateFormFields(entity));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to load record details", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

    private void populateFormFields(CervicalCancerScreeningEntity entity) {
        selectedProfileId = entity.getProfileId(); // Restore matched tracking state identity
        etDateAssessment.setText(entity.getDateAssessment());
        etFamilySerial.setText(entity.getFamilySerial());
        etName.setText(entity.getName(), false); // Dropdown format protection parameter insertion
        etAddress.setText(entity.getAddress());
        etDateOfBirth.setText(entity.getDateOfBirth());
        etAge.setText(entity.getAge());
        etRemarks.setText(entity.getRemarks());

        // Reverse map integer code fields back into position selections safely
        setSelectionByCode(spinnerCervicalScreeningDone, CERVICAL_SCREENING_ITEMS, entity.getCervicalScreeningDone(), 3);
        setSelectionByCode(spinnerCervicalResult, CERVICAL_RESULT_ITEMS, entity.getCervicalResult(), 2);
        setSelectionByCode(spinnerCervicalLinkedToCare, CERVICAL_LINKED_ITEMS, entity.getCervicalLinkedToCare(), 2);
        setSelectionByCode(spinnerBreastRiskAssessment, BREAST_RISK_ITEMS, entity.getBreastRiskAssessment(), 2);
        setSelectionByCode(spinnerBreastResult, BREAST_RESULT_ITEMS, entity.getBreastResult(), 3);
        setSelectionByCode(spinnerBreastLinkedToCare, BREAST_LINKED_ITEMS, entity.getBreastLinkedToCare(), 1);

        // String evaluations mapping back selection options
        int ageRiskPos = "A".equals(entity.getBreastAgeRiskClass()) ? 1 : "B".equals(entity.getBreastAgeRiskClass()) ? 2 : 0;
        spinnerBreastAgeRisk.setText(BREAST_AGE_RISK_ITEMS[ageRiskPos], false);

        int examPos = "CBE".equals(entity.getBreastExamType()) ? 1 : "M".equals(entity.getBreastExamType()) ? 2 : "CBE+M".equals(entity.getBreastExamType()) ? 3 : 0;
        spinnerBreastExamType.setText(BREAST_EXAM_ITEMS[examPos], false);

        btnSubmit.setText("Update Record");
    }

    private void setSelectionByCode(AutoCompleteTextView spinner, String[] items, int code, int maxCode) {
        if (code == -1) {
            spinner.setText(items[0], false);
            return;
        }
        int expectedPosition = maxCode - code + 1;
        if (expectedPosition >= 0 && expectedPosition < items.length) {
            spinner.setText(items[expectedPosition], false);
        }
    }

    private boolean validateForm() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Client name is required");
            etName.requestFocus();
            return false;
        }

        String assessmentDate = etDateAssessment.getText().toString().trim();
        if (!isValidDateFormat(assessmentDate)) {
            etDateAssessment.setError("Required format is YYYY-MM-DD");
            etDateAssessment.requestFocus();
            return false;
        }

        String dobDate = etDateOfBirth.getText().toString().trim();
        if (!dobDate.isEmpty() && !isValidDateFormat(dobDate)) {
            etDateOfBirth.setError("Invalid date format (YYYY-MM-DD)");
            etDateOfBirth.requestFocus();
            return false;
        }

        if (etAge.getText().toString().trim().isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return false;
        }
        if (Arrays.asList(CERVICAL_SCREENING_ITEMS).indexOf(spinnerCervicalScreeningDone.getText().toString()) <= 0) {
            Toast.makeText(requireContext(), "Please select a Cervical Screening Method", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Arrays.asList(CERVICAL_RESULT_ITEMS).indexOf(spinnerCervicalResult.getText().toString()) <= 0) {
            Toast.makeText(requireContext(), "Please select a Cervical Screening Result", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Arrays.asList(BREAST_RISK_ITEMS).indexOf(spinnerBreastRiskAssessment.getText().toString()) <= 0) {
            Toast.makeText(requireContext(), "Please select a Breast Risk Assessment Result", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidDateFormat(String date) {
        return date.matches("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])");
    }

    private CervicalCancerScreeningData collectFormData() {
        CervicalCancerScreeningData d = new CervicalCancerScreeningData();

        d.id             = currentScreeningId;
        d.profileId      = selectedProfileId; // Includes the linked ID mapping
        d.dateAssessment = etDateAssessment.getText().toString().trim();
        d.familySerial   = etFamilySerial.getText().toString().trim();
        d.name           = etName.getText().toString().trim();
        d.address        = etAddress.getText().toString().trim();
        d.dateOfBirth    = etDateOfBirth.getText().toString().trim();
        d.age            = etAge.getText().toString().trim();

        d.cervicalScreeningDone = resolveDescendingCode(Arrays.asList(CERVICAL_SCREENING_ITEMS).indexOf(spinnerCervicalScreeningDone.getText().toString()), 3);
        d.cervicalResult        = resolveDescendingCode(Arrays.asList(CERVICAL_RESULT_ITEMS).indexOf(spinnerCervicalResult.getText().toString()), 2);
        d.cervicalLinkedToCare   = resolveDescendingCode(Arrays.asList(CERVICAL_LINKED_ITEMS).indexOf(spinnerCervicalLinkedToCare.getText().toString()), 2);

        d.breastRiskAssessment  = resolveDescendingCode(Arrays.asList(BREAST_RISK_ITEMS).indexOf(spinnerBreastRiskAssessment.getText().toString()), 2);

        int ageRiskPos = Arrays.asList(BREAST_AGE_RISK_ITEMS).indexOf(spinnerBreastAgeRisk.getText().toString());
        d.breastAgeRiskClass = (ageRiskPos == 1) ? "A" : (ageRiskPos == 2) ? "B" : "";

        int examPos = Arrays.asList(BREAST_EXAM_ITEMS).indexOf(spinnerBreastExamType.getText().toString());
        d.breastExamType = (examPos == 1) ? "CBE" : (examPos == 2) ? "M" : (examPos == 3) ? "CBE+M" : "";

        d.breastResult       = resolveDescendingCode(Arrays.asList(BREAST_RESULT_ITEMS).indexOf(spinnerBreastResult.getText().toString()), 3);
        d.breastLinkedToCare = resolveDescendingCode(Arrays.asList(BREAST_LINKED_ITEMS).indexOf(spinnerBreastLinkedToCare.getText().toString()), 1);

        d.remarks = etRemarks.getText().toString().trim();
        return d;
    }

    private int resolveDescendingCode(int position, int maxCode) {
        if (position <= 0) return -1;
        return maxCode - (position - 1);
    }

    private void onScreeningSubmitted(CervicalCancerScreeningData data) {
        CervicalCancerScreeningEntity entity = CancerDataMapper.toEntity(data);

        new Thread(() -> {
            try {
                DatabaseHelper db = DatabaseHelper.getInstance(requireContext());

                if (entity.getId() == 0) {
                    long newId = db.cervicalCancerScreeningDao().insert(entity);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Screening data saved successfully (ID: " + newId + ")", Toast.LENGTH_LONG).show();
                            resetForm();
                        });
                    }
                } else {
                    db.cervicalCancerScreeningDao().update(entity);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Screening record updated successfully!", Toast.LENGTH_LONG).show();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Database Save Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void resetForm() {
        currentScreeningId = 0;
        selectedProfileId = 0;
        etDateAssessment.setText("");
        etFamilySerial.setText("");
        etName.setText("");
        etAddress.setText("");
        etDateOfBirth.setText("");
        etAge.setText("");
        etRemarks.setText("");
        btnSubmit.setText("Submit Form");
        setupDropdowns();
    }

    public static class CervicalCancerScreeningData {
        public long id;
        public int profileId; // Added to map ID
        public String dateAssessment, familySerial, name, address, dateOfBirth, age;
        public int cervicalScreeningDone, cervicalResult, cervicalLinkedToCare;
        public int breastRiskAssessment, breastResult, breastLinkedToCare;
        public String breastAgeRiskClass, breastExamType, remarks;
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