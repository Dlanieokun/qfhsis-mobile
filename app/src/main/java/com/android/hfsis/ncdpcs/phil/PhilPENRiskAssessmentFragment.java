package com.android.hfsis.ncdpcs.phil;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.database.ncdps.PhilPENDao;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.ncdpcs.PhilPENAssessmentEntity;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * PhilPENRiskAssessmentFragment
 *
 * Captures all fields from the PhilPEN Risk Assessment (TCL_PhilPEN).
 * Supports full manual date input with real-time formatting (YYYY-MM-DD) as well as DatePicker dialogs.
 */
public class PhilPENRiskAssessmentFragment extends Fragment {

    // ── Client Information ──────────────────────────────────────────────────
    private EditText etDateAssessment, etFamilySerialNumber, etAddress, etDateOfBirth, etAge;
    private TextInputLayout tilName;
    private AutoCompleteTextView etName;
    private Spinner  spinnerAgeGroup;
    private RadioGroup rgSex;
    private RadioButton rbMale, rbFemale;

    // ── Risk Factors ────────────────────────────────────────────────────────
    private Spinner spinnerCurrentSmoker;
    private Spinner spinnerAsk, spinnerAdvise, spinnerAssess,
            spinnerAssist, spinnerArrange;
    private Spinner spinnerProvidedBTI;
    private Spinner spinnerBingeAlcohol;
    private Spinner spinnerInsufficientPA;
    private Spinner spinnerUnhealthyDiet;
    private Spinner spinnerBMI;

    // ── Hypertension Screening (TCL_PhilPEN: columns U–AY) ───────────────────
    private EditText etScreeningDate1, etScreeningDate2;
    private EditText etBpSystolic1, etBpDiastolic1, etBpSystolic2, etBpDiastolic2;
    private Spinner  spinnerHypertensionResult;
    private EditText etMedsInitial, etMedsChanged;

    // ── Type 2 Diabetes Mellitus Screening (TCL_PhilPEN: columns BI–CS) ──────
    private Spinner  spinnerDiabetesResult;
    private EditText etAntidiabeticMeds;

    // ── Remarks ───────────────────────────────────────────────────────────────
    private EditText etRemarks;

    private long currentAssessmentId = 0;
    private int selectedProfileId = 0; // Tracks the selected profile database ID
    private DatabaseHelper dbHelper;

    // Hypertension monthly medicine grid covers January–September per TCL_PhilPEN
    private static final String[] HYPERTENSION_MONTHS = {
            "January", "February", "March", "April", "May",
            "June", "July", "August", "September"
    };
    private final int[] hypertensionMonthRowIds = {
            R.id.rowJanuary, R.id.rowFebruary, R.id.rowMarch,
            R.id.rowApril,   R.id.rowMay,      R.id.rowJune,
            R.id.rowJuly,    R.id.rowAugust,   R.id.rowSeptember
    };

    // Diabetes monthly medicine grid covers all 12 months per TCL_PhilPEN
    private static final String[] DIABETES_MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };
    private final int[] diabetesMonthRowIds = {
            R.id.rowDiabetesJanuary, R.id.rowDiabetesFebruary, R.id.rowDiabetesMarch,
            R.id.rowDiabetesApril,   R.id.rowDiabetesMay,      R.id.rowDiabetesJune,
            R.id.rowDiabetesJuly,    R.id.rowDiabetesAugust,   R.id.rowDiabetesSeptember,
            R.id.rowDiabetesOctober, R.id.rowDiabetesNovember, R.id.rowDiabetesDecember
    };

    private Button btnSubmit;

    public static PhilPENRiskAssessmentFragment newInstance() {
        return new PhilPENRiskAssessmentFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        dbHelper = DatabaseHelper.getInstance(requireContext());
        return inflater.inflate(R.layout.fragment_philpen_risk_assessment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupNameAutocomplete();
        setupSpinners();
        setupMonthRows(view, hypertensionMonthRowIds, HYPERTENSION_MONTHS);
        setupMonthRows(view, diabetesMonthRowIds, DIABETES_MONTHS);
        setupDatePickers();
        setupSubmitButton();

        // Check for EDIT_RECORD_ID argument passed from listing views
        if (getArguments() != null && getArguments().containsKey("EDIT_RECORD_ID")) {
            long recordId = getArguments().getLong("EDIT_RECORD_ID");
            loadExistingRecordFromDb(recordId);
        }else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etDateAssessment.setText(sdf.format(new Date()));
        }
    }

    private void bindViews(View v) {
        etDateAssessment       = v.findViewById(R.id.etDateAssessment);
        etFamilySerialNumber   = v.findViewById(R.id.etFamilySerialNumber);
        tilName                = v.findViewById(R.id.tilName);
        etName                 = v.findViewById(R.id.etName);
        etAddress              = v.findViewById(R.id.etAddress);
        etDateOfBirth          = v.findViewById(R.id.etDateOfBirth);
        etAge                  = v.findViewById(R.id.etAge);
        spinnerAgeGroup        = v.findViewById(R.id.spinnerAgeGroup);
        rgSex                  = v.findViewById(R.id.rgSex);
        rbMale                 = v.findViewById(R.id.rbMale);
        rbFemale               = v.findViewById(R.id.rbFemale);

        spinnerCurrentSmoker   = v.findViewById(R.id.spinnerCurrentSmoker);
        spinnerAsk             = v.findViewById(R.id.spinnerAsk);
        spinnerAdvise          = v.findViewById(R.id.spinnerAdvise);
        spinnerAssess          = v.findViewById(R.id.spinnerAssess);
        spinnerAssist          = v.findViewById(R.id.spinnerAssist);
        spinnerArrange         = v.findViewById(R.id.spinnerArrange);
        spinnerProvidedBTI     = v.findViewById(R.id.spinnerProvidedBTI);
        spinnerBingeAlcohol    = v.findViewById(R.id.spinnerBingeAlcohol);
        spinnerInsufficientPA  = v.findViewById(R.id.spinnerInsufficientPA);
        spinnerUnhealthyDiet   = v.findViewById(R.id.spinnerUnhealthyDiet);
        spinnerBMI             = v.findViewById(R.id.spinnerBMI);

        etScreeningDate1          = v.findViewById(R.id.etScreeningDate1);
        etScreeningDate2          = v.findViewById(R.id.etScreeningDate2);
        etBpSystolic1             = v.findViewById(R.id.etBpSystolic1);
        etBpDiastolic1            = v.findViewById(R.id.etBpDiastolic1);
        etBpSystolic2             = v.findViewById(R.id.etBpSystolic2);
        etBpDiastolic2            = v.findViewById(R.id.etBpDiastolic2);
        spinnerHypertensionResult = v.findViewById(R.id.spinnerHypertensionResult);
        etMedsInitial             = v.findViewById(R.id.etMedsInitial);
        etMedsChanged             = v.findViewById(R.id.etMedsChanged);

        spinnerDiabetesResult     = v.findViewById(R.id.spinnerDiabetesResult);
        etAntidiabeticMeds        = v.findViewById(R.id.etAntidiabeticMeds);

        etRemarks                 = v.findViewById(R.id.etRemarks);

        btnSubmit = v.findViewById(R.id.btnSubmit);
    }

    private void setupNameAutocomplete() {
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

        // Optional: clear selected profile ID if the user manually replaces/wipes text completely
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
            HouseholdProfile profile = dbHelper.householdProfileDao().getProfileByCalculatedName(fullCalculatedName);
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    // Capture and save profile record identity mapping
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
                        try {
                            // Updated profile parsing split configuration to accommodate YYYY-MM-DD layout
                            String[] dobParts = profile.dob.split("-");
                            if (dobParts.length == 3) {
                                int year = Integer.parseInt(dobParts[0]);
                                int month = Integer.parseInt(dobParts[1]) - 1;
                                int day = Integer.parseInt(dobParts[2]);
                                calculateAgeYears(year, month, day);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void calculateAgeYears(int birthYear, int birthMonth, int birthDay) {
        Calendar dob = Calendar.getInstance();
        dob.set(birthYear, birthMonth, birthDay);
        Calendar today = Calendar.getInstance();

        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
        if (months < 0) { years--; }

        int totalYears = Math.max(0, years);
        etAge.setText(String.valueOf(totalYears));

        if (totalYears >= 60) {
            spinnerAgeGroup.setSelection(2);
        } else if (totalYears >= 20) {
            spinnerAgeGroup.setSelection(1);
        } else {
            spinnerAgeGroup.setSelection(0);
        }
    }

    private void setupDatePickers() {
        etDateAssessment.addTextChangedListener(new DateFormattingWatcher(etDateAssessment));
        etDateOfBirth.addTextChangedListener(new DateFormattingWatcher(etDateOfBirth));
        etScreeningDate1.addTextChangedListener(new DateFormattingWatcher(etScreeningDate1));
        etScreeningDate2.addTextChangedListener(new DateFormattingWatcher(etScreeningDate2));

        setupDatePickerField(etDateAssessment);
        setupDatePickerField(etDateOfBirth);
        setupDatePickerField(etScreeningDate1);
        setupDatePickerField(etScreeningDate2);
    }

    private void setupDatePickerField(final EditText editText) {
        View.OnClickListener clickListener = v -> showCalendarDialog(editText);
        editText.setOnClickListener(clickListener);
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showCalendarDialog(editText);
            }
        });
    }

    private void showCalendarDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selCalendar = Calendar.getInstance();
                    selCalendar.set(Calendar.YEAR, selectedYear);
                    selCalendar.set(Calendar.MONTH, selectedMonth);
                    selCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    editText.setText(sdf.format(selCalendar.getTime()));
                    editText.setError(null);

                    if (editText == etDateOfBirth) {
                        calculateAgeYears(selectedYear, selectedMonth, selectedDay);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void setupSpinners() {
        setSpinner(spinnerAgeGroup, new String[]{
                "Select Age Group",
                "A – 20 to 59 years old",
                "B – 60 years old and above"
        });

        setSpinner(spinnerCurrentSmoker, new String[]{
                "Select", "0 – No", "1 – Tobacco Product",
                "2 – Vaporized Nicotine Products", "3 – Both"
        });

        String[] yesNo = {"Select", "1 – Yes", "0 – No"};
        setSpinner(spinnerAsk,        yesNo);
        setSpinner(spinnerAdvise,     yesNo);
        setSpinner(spinnerAssess,     yesNo);
        setSpinner(spinnerAssist,     yesNo);
        setSpinner(spinnerArrange,    yesNo);
        setSpinner(spinnerProvidedBTI, yesNo);

        setSpinner(spinnerBingeAlcohol,   yesNo);
        setSpinner(spinnerInsufficientPA, yesNo);
        setSpinner(spinnerUnhealthyDiet,  yesNo);

        setSpinner(spinnerBMI, new String[]{
                "Select", "0 – Normal (18.5–22.9)",
                "1 – Overweight (23.0–24.9)", "2 – Obese (>25.0)"
        });

        setSpinner(spinnerHypertensionResult, new String[]{
                "Select",
                "1 – Hypertensive (>140/90 mmHg)",
                "0 – Normal (<140/90 mmHg)"
        });

        setSpinner(spinnerDiabetesResult, new String[]{
                "Select",
                "1 – Positive (FBS ≥126 mg/dL or RBS ≥200 mg/dL or HbA1c ≥6.5%)",
                "0 – Negative"
        });
    }

    private void setSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupMonthRows(View root, int[] rowIds, String[] monthLabels) {
        for (int i = 0; i < rowIds.length; i++) {
            View row = root.findViewById(rowIds[i]);
            if (row != null) {
                TextView label = row.findViewById(R.id.tvMonthLabel);
                if (label != null) label.setText(monthLabels[i]);
            }
        }
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                PhilPENAssessmentEntity entity = collectFormData();
                String PREFS_NAME = "AppPrefs";
                SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);
                entity.userId = userId;
                onAssessmentSubmitted(entity);
            }
        });
    }

    private boolean validateForm() {
        // Regex expression matches YYYY-MM-DD structures
        String dateRegex = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])";

        String dateAssess = etDateAssessment.getText().toString().trim();
        if (dateAssess.isEmpty() || !dateAssess.matches(dateRegex)) {
            etDateAssessment.setError("Valid assessment date is required (YYYY-MM-DD)");
            etDateAssessment.requestFocus();
            return false;
        }

        if (etName.getText().toString().trim().isEmpty()) {
            tilName.setError("Client name is required");
            etName.requestFocus();
            return false;
        } else {
            tilName.setError(null);
        }

        String dob = etDateOfBirth.getText().toString().trim();
        if (!dob.isEmpty() && !dob.matches(dateRegex)) {
            etDateOfBirth.setError("Please match required date format (YYYY-MM-DD)");
            etDateOfBirth.requestFocus();
            return false;
        }

        String scr1 = etScreeningDate1.getText().toString().trim();
        if (!scr1.isEmpty() && !scr1.matches(dateRegex)) {
            etScreeningDate1.setError("Please match required date format (YYYY-MM-DD)");
            etScreeningDate1.requestFocus();
            return false;
        }

        String scr2 = etScreeningDate2.getText().toString().trim();
        if (!scr2.isEmpty() && !scr2.matches(dateRegex)) {
            etScreeningDate2.setError("Please match required date format (YYYY-MM-DD)");
            etScreeningDate2.requestFocus();
            return false;
        }

        if (spinnerAgeGroup.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Please select an Age Group", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (rgSex.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Please select Sex", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private PhilPENAssessmentEntity collectFormData() {
        PhilPENAssessmentEntity entity = new PhilPENAssessmentEntity();
        entity.id = this.currentAssessmentId;

        // Save the tracked Profile ID reference here
        entity.profileId = this.selectedProfileId;

        // Client info
        entity.dateAssessment     = etDateAssessment.getText().toString().trim();
        entity.familySerial       = etFamilySerialNumber.getText().toString().trim();
        entity.name               = etName.getText().toString().trim();
        entity.address            = etAddress.getText().toString().trim();
        entity.dateOfBirth        = etDateOfBirth.getText().toString().trim();
        entity.age                = etAge.getText().toString().trim();
        entity.ageGroup           = spinnerAgeGroup.getSelectedItem().toString();
        entity.sex                = rbMale.isChecked() ? "M" : (rbFemale.isChecked() ? "F" : "");

        // Risk factors
        entity.currentSmoker      = spinnerCurrentSmoker.getSelectedItemPosition();
        entity.btiAsk             = spinnerAsk.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.btiAdvise          = spinnerAdvise.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.btiAssess          = spinnerAssess.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.btiAssist          = spinnerAssist.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.btiArrange         = spinnerArrange.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.providedBTI        = spinnerProvidedBTI.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.bingeAlcohol       = spinnerBingeAlcohol.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.insufficientPA     = spinnerInsufficientPA.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.unhealthyDiet      = spinnerUnhealthyDiet.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.bmiCategory        = spinnerBMI.getSelectedItemPosition() - 1;

        // Hypertension Screening
        entity.screeningDate1     = etScreeningDate1.getText().toString().trim();
        entity.screeningDate2     = etScreeningDate2.getText().toString().trim();
        entity.bpSystolic1        = parseInt(etBpSystolic1);
        entity.bpDiastolic1       = parseInt(etBpDiastolic1);
        entity.bpSystolic2        = parseInt(etBpSystolic2);
        entity.bpDiastolic2       = parseInt(etBpDiastolic2);
        entity.hypertensionResult = spinnerHypertensionResult.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.medsInitial        = parseInt(etMedsInitial);
        entity.medsChanged        = parseInt(etMedsChanged);

        // Type 2 Diabetes Mellitus Screening
        entity.diabetesResult     = spinnerDiabetesResult.getSelectedItemPosition() == 1 ? 1 : 0;
        entity.antidiabeticMeds   = parseInt(etAntidiabeticMeds);

        // Remarks
        entity.remarks            = etRemarks != null ? etRemarks.getText().toString().trim() : "";

        // Month rows processing
        if (getView() != null) {
            entity.monthlyMeds = collectMonthlyMeds(getView(), hypertensionMonthRowIds, HYPERTENSION_MONTHS);
            entity.monthlyDiabeticMeds = collectMonthlyMeds(getView(), diabetesMonthRowIds, DIABETES_MONTHS);
        }

        return entity;
    }

    private PhilPENAssessmentEntity.MonthMed[] collectMonthlyMeds(View root, int[] rowIds, String[] monthLabels) {
        PhilPENAssessmentEntity.MonthMed[] monthlyMeds = new PhilPENAssessmentEntity.MonthMed[rowIds.length];
        for (int i = 0; i < rowIds.length; i++) {
            View row = root.findViewById(rowIds[i]);
            if (row != null) {
                EditText pbf  = row.findViewById(R.id.etPBF);
                EditText oop  = row.findViewById(R.id.etOOP);
                CheckBox both = row.findViewById(R.id.cbBoth);
                monthlyMeds[i] = new PhilPENAssessmentEntity.MonthMed(
                        monthLabels[i],
                        parseInt(pbf),
                        parseInt(oop),
                        both != null && both.isChecked()
                );
            }
        }
        return monthlyMeds;
    }

    private void onAssessmentSubmitted(PhilPENAssessmentEntity assessmentEntity) {
        new Thread(() -> {
            try {
                DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
                PhilPENDao dao = db.philPENDao();

                if (assessmentEntity.id == 0) {
                    long insertedId = dao.insert(assessmentEntity);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Assessment record created successfully!", Toast.LENGTH_SHORT).show();
                        this.currentAssessmentId = insertedId;
                    });
                } else {
                    dao.update(assessmentEntity);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Assessment updated successfully!", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error saving record: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void loadExistingRecordFromDb(long recordId) {
        new Thread(() -> {
            try {
                DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
                PhilPENAssessmentEntity savedRecord = db.philPENDao().getAssessmentById(recordId);
                if (savedRecord != null) {
                    requireActivity().runOnUiThread(() -> loadAssessmentForEditing(savedRecord));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void loadAssessmentForEditing(PhilPENAssessmentEntity editingEntity) {
        if (editingEntity == null) return;

        this.currentAssessmentId = editingEntity.id;
        this.selectedProfileId = editingEntity.profileId; // Re-populate tracked profile mapping id

        // 1. Text Fields & Dropdown Autocomplete
        etDateAssessment.setText(editingEntity.dateAssessment);
        etFamilySerialNumber.setText(editingEntity.familySerial);
        etName.setText(editingEntity.name, false);
        etAddress.setText(editingEntity.address);
        etDateOfBirth.setText(editingEntity.dateOfBirth);
        etAge.setText(editingEntity.age);

        // 2. Age Group Spinner Mapping
        setSpinnerSelectionByValue(spinnerAgeGroup, editingEntity.ageGroup);

        // 3. Sex RadioButtons Mapping
        if ("M".equalsIgnoreCase(editingEntity.sex)) {
            rbMale.setChecked(true);
        } else if ("F".equalsIgnoreCase(editingEntity.sex)) {
            rbFemale.setChecked(true);
        } else {
            rgSex.clearCheck();
        }

        // 4. Behavioral Risk Factors Spinners
        spinnerCurrentSmoker.setSelection(editingEntity.currentSmoker);
        spinnerAsk.setSelection(editingEntity.btiAsk == 1 ? 1 : 2);
        spinnerAdvise.setSelection(editingEntity.btiAdvise == 1 ? 1 : 2);
        spinnerAssess.setSelection(editingEntity.btiAssess == 1 ? 1 : 2);
        spinnerAssist.setSelection(editingEntity.btiAssist == 1 ? 1 : 2);
        spinnerArrange.setSelection(editingEntity.btiArrange == 1 ? 1 : 2);
        spinnerProvidedBTI.setSelection(editingEntity.providedBTI == 1 ? 1 : 2);
        spinnerBingeAlcohol.setSelection(editingEntity.bingeAlcohol == 1 ? 1 : 2);
        spinnerInsufficientPA.setSelection(editingEntity.insufficientPA == 1 ? 1 : 2);
        spinnerUnhealthyDiet.setSelection(editingEntity.unhealthyDiet == 1 ? 1 : 2);
        spinnerBMI.setSelection(editingEntity.bmiCategory + 1);

        // 5. Hypertension Screenings Fields
        etScreeningDate1.setText(editingEntity.screeningDate1);
        etScreeningDate2.setText(editingEntity.screeningDate2);
        populateNumericEditText(etBpSystolic1, editingEntity.bpSystolic1);
        populateNumericEditText(etBpDiastolic1, editingEntity.bpDiastolic1);
        populateNumericEditText(etBpSystolic2, editingEntity.bpSystolic2);
        populateNumericEditText(etBpDiastolic2, editingEntity.bpDiastolic2);
        spinnerHypertensionResult.setSelection(editingEntity.hypertensionResult == 1 ? 1 : 2);
        populateNumericEditText(etMedsInitial, editingEntity.medsInitial);
        populateNumericEditText(etMedsChanged, editingEntity.medsChanged);

        // 6. Type 2 Diabetes Mellitus Screening Fields
        spinnerDiabetesResult.setSelection(editingEntity.diabetesResult == 1 ? 1 : 2);
        populateNumericEditText(etAntidiabeticMeds, editingEntity.antidiabeticMeds);

        // 7. Remarks
        if (etRemarks != null) {
            etRemarks.setText(editingEntity.remarks);
        }

        // 8. Restore Embedded Monthly Medicine Rows
        if (getView() != null) {
            restoreMonthlyMeds(getView(), hypertensionMonthRowIds, editingEntity.monthlyMeds);
            restoreMonthlyMeds(getView(), diabetesMonthRowIds, editingEntity.monthlyDiabeticMeds);
        }
    }

    private void restoreMonthlyMeds(View root, int[] rowIds, PhilPENAssessmentEntity.MonthMed[] monthlyMeds) {
        if (monthlyMeds == null) return;
        for (int i = 0; i < rowIds.length; i++) {
            if (i < monthlyMeds.length) {
                View row = root.findViewById(rowIds[i]);
                if (row != null) {
                    EditText pbf  = row.findViewById(R.id.etPBF);
                    EditText oop  = row.findViewById(R.id.etOOP);
                    CheckBox both = row.findViewById(R.id.cbBoth);

                    PhilPENAssessmentEntity.MonthMed medData = monthlyMeds[i];
                    if (medData != null) {
                        populateNumericEditText(pbf, medData.pbf);
                        populateNumericEditText(oop, medData.oop);
                        if (both != null) {
                            both.setChecked(medData.both);
                        }
                    }
                }
            }
        }
    }

    private void populateNumericEditText(EditText et, int value) {
        if (et != null) {
            et.setText(value >= 0 ? String.valueOf(value) : "");
        }
    }

    @SuppressWarnings("unchecked")
    private void setSpinnerSelectionByValue(Spinner spinner, String value) {
        if (spinner == null || value == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private int parseInt(EditText et) {
        try {
            return Integer.parseInt(et.getText().toString().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
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
                // Automatically injects hyphens dynamically after index 3 (YYYY-) and index 5 (YYYY-MM-)
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