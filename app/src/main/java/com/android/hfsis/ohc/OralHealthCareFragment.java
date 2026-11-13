package com.android.hfsis.ohc;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.ohc.OralHealthCareEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class OralHealthCareFragment extends Fragment {

    private static final String ARG_ENTRY_ID = "entry_id";
    private int entryId = -1;
    private int selectedProfileId = -1; // Added tracking variable for the linked profile
    private DatabaseHelper dbHelper;

    // --- Section 1: Basic Information ---
    private TextInputLayout tilDateOfVisit, tilFamilySerial, tilName, tilAddress, tilDateOfBirth, tilAgeMonths;
    private TextInputEditText etDateOfVisit, etFamilySerial, etAddress, etDateOfBirth, etAgeMonths;
    private AutoCompleteTextView etName;
    private RadioGroup rgSex;
    private RadioButton rbMale, rbFemale;
    private TextView tvSexError;

    // --- Section 2: RPOC 0–11 months ---
    private CheckBox cbOralScreening0, cbRiskAssessment0, cbOralHygiene, cbCounseling0, cbFluorideVarnish0;

    // --- Section 3: Complete RPOC 0–11 mos ---
    private RadioGroup rgCompleteRpoc0;
    private RadioButton rbCompleteRpoc0Yes, rbCompleteRpoc0No;

    // --- Section 4: RPOC 1 yr+ and Pregnant ---
    private TextInputLayout tilAgeYears;
    private TextInputEditText etAgeYears;
    private TextInputLayout tilAgeGroup1st, tilAgeGroup2nd;
    private AutoCompleteTextView actAgeGroup1st, actAgeGroup2nd;

    private TextInputLayout tilOralScreening1st, tilOralScreening2nd, tilRiskAssessment1st, tilRiskAssessment2nd,
            tilOralProphylaxis1st, tilOralProphylaxis2nd, tilFluorideVarnish1st, tilFluorideVarnish2nd,
            tilCounseling1st, tilCounseling2nd;
    private TextInputEditText etOralScreening1st, etOralScreening2nd, etRiskAssessment1st, etRiskAssessment2nd,
            etOralProphylaxis1st, etOralProphylaxis2nd, etFluorideVarnish1st, etFluorideVarnish2nd,
            etCounseling1st, etCounseling2nd;

    // --- Section 5: Complete RPOC 1st & 2nd Visit ---
    private RadioGroup rgCompleteRpoc1st, rgCompleteRpoc2nd;
    private RadioButton rbCompleteRpoc1stYes, rbCompleteRpoc1stNo, rbCompleteRpoc2ndYes, rbCompleteRpoc2ndNo;

    // --- Section 6: Service Location ---
    private RadioGroup rgServiceLocation1st, rgServiceLocation2nd;
    private RadioButton rbFacility1st, rbNonFacility1st, rbFacility2nd, rbNonFacility2nd;

    // --- Section 7: Remarks ---
    private TextInputLayout tilRemarks;
    private TextInputEditText etRemarks;

    // --- Buttons ---
    private MaterialButton btnSave, btnClear;

    private static final String[] AGE_GROUP_OPTIONS = {
            "A — 1-4 years old", "B — 5-9 years old", "C — 10-19 years old",
            "D — 20-59 years old", "E — >60 years old", "F — Pregnant (10-59 years old)"
    };

    public static OralHealthCareFragment newInstance(int id) {
        OralHealthCareFragment fragment = new OralHealthCareFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ENTRY_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_oral_health_care, container, false);
        setupKeyboardInsets(view);

        dbHelper = DatabaseHelper.getInstance(requireContext());

        if (getArguments() != null) {
            entryId = getArguments().getInt(ARG_ENTRY_ID, -1);
        }

        initViews(view);
        setupNameAutocomplete();
        setupAgeGroupDropdowns();
        setupDatePickersAndFormatters();
        setupButtons();

        if (entryId != -1) {
            loadEntryData();
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            etDateOfVisit.setText(sdf.format(new Date()));
        }

        return view;
    }

    /**
     * Keeps the focused input visible above the soft keyboard.
     * Works on edge-to-edge (Android 15+) where adjustResize alone is not enough.
     */
    private void setupKeyboardInsets(@NonNull View root) {
        final int baseLeft = root.getPaddingLeft();
        final int baseTop = root.getPaddingTop();
        final int baseRight = root.getPaddingRight();
        final int baseBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int bottom = Math.max(ime.bottom, bars.bottom);
            v.setPadding(baseLeft, baseTop, baseRight, baseBottom + bottom);

            if (ime.bottom > 0) {
                View focused = v.findFocus();
                if (focused != null) {
                    v.post(() -> scrollToView(v, focused));
                }
            }
            return insets;
        });

        // Scroll to a field whenever it gains focus while the keyboard is open
        root.getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
            if (newFocus instanceof EditText) {
                root.postDelayed(() -> scrollToView(root, newFocus), 200);
            }
        });

        ViewCompat.requestApplyInsets(root);
    }

    private void scrollToView(View root, View target) {
        if (!(root instanceof ScrollView) || !isAdded()) return;
        ScrollView scroll = (ScrollView) root;
        android.graphics.Rect rect = new android.graphics.Rect();
        target.getDrawingRect(rect);
        scroll.offsetDescendantRectToMyCoords(target, rect);
        rect.bottom += (int) (80 * getResources().getDisplayMetrics().density);
        scroll.requestChildRectangleOnScreen(scroll.getChildAt(0), rect, false);
    }

    private void initViews(View v) {
        // Section 1
        tilDateOfVisit  = v.findViewById(R.id.tilDateOfVisit);
        etDateOfVisit   = v.findViewById(R.id.etDateOfVisit);
        tilFamilySerial = v.findViewById(R.id.tilFamilySerial);
        etFamilySerial  = v.findViewById(R.id.etFamilySerial);
        tilName         = v.findViewById(R.id.tilName);
        etName          = v.findViewById(R.id.etName);
        tilAddress      = v.findViewById(R.id.tilAddress);
        etAddress       = v.findViewById(R.id.etAddress);
        tilDateOfBirth  = v.findViewById(R.id.tilDateOfBirth);
        etDateOfBirth   = v.findViewById(R.id.etDateOfBirth);
        tilAgeMonths    = v.findViewById(R.id.tilAgeMonths);
        etAgeMonths     = v.findViewById(R.id.etAgeMonths);
        rgSex           = v.findViewById(R.id.rgSex);
        rbMale          = v.findViewById(R.id.rbMale);
        rbFemale        = v.findViewById(R.id.rbFemale);
        tvSexError      = v.findViewById(R.id.tvSexError);

        // Section 2
        cbOralScreening0   = v.findViewById(R.id.cbOralScreening0);
        cbRiskAssessment0  = v.findViewById(R.id.cbRiskAssessment0);
        cbOralHygiene      = v.findViewById(R.id.cbOralHygiene);
        cbCounseling0      = v.findViewById(R.id.cbCounseling0);
        cbFluorideVarnish0 = v.findViewById(R.id.cbFluorideVarnish0);

        // Section 3
        rgCompleteRpoc0    = v.findViewById(R.id.rgCompleteRpoc0);
        rbCompleteRpoc0Yes = v.findViewById(R.id.rbCompleteRpoc0Yes);
        rbCompleteRpoc0No  = v.findViewById(R.id.rbCompleteRpoc0No);

        // Section 4
        tilAgeYears    = v.findViewById(R.id.tilAgeYears);
        etAgeYears     = v.findViewById(R.id.etAgeYears);
        tilAgeGroup1st = v.findViewById(R.id.tilAgeGroup1st);
        actAgeGroup1st = v.findViewById(R.id.actAgeGroup1st);
        tilAgeGroup2nd = v.findViewById(R.id.tilAgeGroup2nd);
        actAgeGroup2nd = v.findViewById(R.id.actAgeGroup2nd);

        etOralScreening1st   = v.findViewById(R.id.etOralScreening1st);
        tilOralScreening1st  = v.findViewById(R.id.tilOralScreening1st);
        etOralScreening2nd   = v.findViewById(R.id.etOralScreening2nd);
        tilOralScreening2nd  = v.findViewById(R.id.tilOralScreening2nd);
        etRiskAssessment1st  = v.findViewById(R.id.etRiskAssessment1st);
        tilRiskAssessment1st = v.findViewById(R.id.tilRiskAssessment1st);
        etRiskAssessment2nd  = v.findViewById(R.id.etRiskAssessment2nd);
        tilRiskAssessment2nd = v.findViewById(R.id.tilRiskAssessment2nd);
        etOralProphylaxis1st = v.findViewById(R.id.etOralProphylaxis1st);
        tilOralProphylaxis1st = v.findViewById(R.id.tilOralProphylaxis1st);
        etOralProphylaxis2nd = v.findViewById(R.id.etOralProphylaxis2nd);
        tilOralProphylaxis2nd = v.findViewById(R.id.tilOralProphylaxis2nd);
        etFluorideVarnish1st = v.findViewById(R.id.etFluorideVarnish1st);
        tilFluorideVarnish1st = v.findViewById(R.id.tilFluorideVarnish1st);
        etFluorideVarnish2nd = v.findViewById(R.id.etFluorideVarnish2nd);
        tilFluorideVarnish2nd = v.findViewById(R.id.tilFluorideVarnish2nd);
        etCounseling1st      = v.findViewById(R.id.etCounseling1st);
        tilCounseling1st     = v.findViewById(R.id.tilCounseling1st);
        etCounseling2nd      = v.findViewById(R.id.etCounseling2nd);
        tilCounseling2nd     = v.findViewById(R.id.tilCounseling2nd);

        // Section 5
        rgCompleteRpoc1st    = v.findViewById(R.id.rgCompleteRpoc1st);
        rbCompleteRpoc1stYes = v.findViewById(R.id.rbCompleteRpoc1stYes);
        rbCompleteRpoc1stNo  = v.findViewById(R.id.rbCompleteRpoc1stNo);
        rgCompleteRpoc2nd    = v.findViewById(R.id.rgCompleteRpoc2nd);
        rbCompleteRpoc2ndYes = v.findViewById(R.id.rbCompleteRpoc2ndYes);
        rbCompleteRpoc2ndNo  = v.findViewById(R.id.rbCompleteRpoc2ndNo);

        // Section 6
        rgServiceLocation1st = v.findViewById(R.id.rgServiceLocation1st);
        rbFacility1st        = v.findViewById(R.id.rbFacility1st);
        rbNonFacility1st     = v.findViewById(R.id.rbNonFacility1st);
        rgServiceLocation2nd = v.findViewById(R.id.rgServiceLocation2nd);
        rbFacility2nd        = v.findViewById(R.id.rbFacility2nd);
        rbNonFacility2nd     = v.findViewById(R.id.rbNonFacility2nd);

        // Section 7
        tilRemarks = v.findViewById(R.id.tilRemarks);
        etRemarks  = v.findViewById(R.id.etRemarks);

        // Buttons
        btnSave  = v.findViewById(R.id.btnSave);
        btnClear = v.findViewById(R.id.btnClear);
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
    }

    private void autoPopulateFromProfile(String fullCalculatedName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = dbHelper.householdProfileDao().getProfileByCalculatedName(fullCalculatedName);
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    // Update tracking profile ID
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
                        etFamilySerial.setText(profile.hhNumber);
                    }

                    if ("M".equalsIgnoreCase(profile.sex) || "Male".equalsIgnoreCase(profile.sex)) {
                        rbMale.setChecked(true);
                    } else if ("F".equalsIgnoreCase(profile.sex) || "Female".equalsIgnoreCase(profile.sex)) {
                        rbFemale.setChecked(true);
                    }

                    if (!TextUtils.isEmpty(profile.dob)) {
                        // Setting the text also triggers the DOB watcher, which computes the age
                        etDateOfBirth.setText(toDisplayDate(profile.dob));
                    }
                });
            }
        });
    }

    private void setupAgeGroupDropdowns() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, AGE_GROUP_OPTIONS);
        actAgeGroup1st.setAdapter(adapter);
        actAgeGroup1st.setOnClickListener(v -> actAgeGroup1st.showDropDown());

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, AGE_GROUP_OPTIONS);
        actAgeGroup2nd.setAdapter(adapter2);
        actAgeGroup2nd.setOnClickListener(v -> actAgeGroup2nd.showDropDown());
    }

    private void setupDatePickersAndFormatters() {
        bindDatePicker(tilDateOfVisit, etDateOfVisit, false);
        bindDatePicker(tilDateOfBirth, etDateOfBirth, true);
        bindDatePicker(tilOralScreening1st, etOralScreening1st, false);
        bindDatePicker(tilOralScreening2nd, etOralScreening2nd, false);
        bindDatePicker(tilRiskAssessment1st, etRiskAssessment1st, false);
        bindDatePicker(tilRiskAssessment2nd, etRiskAssessment2nd, false);
        bindDatePicker(tilOralProphylaxis1st, etOralProphylaxis1st, false);
        bindDatePicker(tilOralProphylaxis2nd, etOralProphylaxis2nd, false);
        bindDatePicker(tilFluorideVarnish1st, etFluorideVarnish1st, false);
        bindDatePicker(tilFluorideVarnish2nd, etFluorideVarnish2nd, false);
        bindDatePicker(tilCounseling1st, etCounseling1st, false);
        bindDatePicker(tilCounseling2nd, etCounseling2nd, false);

        // Auto-compute age while the DOB is typed manually
        etDateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    Date d = parseDate(s.toString());
                    if (d != null) {
                        Calendar c = Calendar.getInstance();
                        c.setTime(d);
                        calculateAgeMonths(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                    }
                } else if (s.length() == 0) {
                    etAgeMonths.setText("");
                    etAgeYears.setText("");
                }
            }
        });
    }

    // Formats keyboard input as MM-dd-yyyy while typing, and opens the
    // calendar dialog from the end icon (same behavior as ProfilingFragment).
    private void bindDatePicker(TextInputLayout layout, TextInputEditText editText, boolean isDOB) {
        editText.addTextChangedListener(new DateFormattingWatcher(editText));
        layout.setEndIconOnClickListener(v -> showDatePickerDialog(editText, isDOB));
    }

    private void showDatePickerDialog(TextInputEditText target, boolean isDOB) {
        // Start on the date already in the field (if valid), otherwise today
        Calendar cal = Calendar.getInstance();
        Date existing = parseDate(getText(target));
        if (existing != null) cal.setTime(existing);

        DatePickerDialog dlg = new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            Calendar selCalendar = Calendar.getInstance();
            selCalendar.set(Calendar.YEAR, year);
            selCalendar.set(Calendar.MONTH, month);
            selCalendar.set(Calendar.DAY_OF_MONTH, day);

            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            target.setText(sdf.format(selCalendar.getTime()));
            target.setError(null);

            if (isDOB) calculateAgeMonths(year, month, day);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
        dlg.show();
    }

    // Converts legacy yyyy-MM-dd values (saved before the format change) to MM-dd-yyyy for display
    private String toDisplayDate(String date) {
        if (date != null && date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return date.substring(5, 7) + "-" + date.substring(8, 10) + "-" + date.substring(0, 4);
        }
        return date;
    }

    // Strict MM-dd-yyyy parser; returns null if the text is not a real date
    private Date parseDate(String text) {
        if (text == null || !text.matches("\\d{2}-\\d{2}-\\d{4}")) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            sdf.setLenient(false);
            return sdf.parse(text);
        } catch (Exception e) {
            return null;
        }
    }

    private void calculateAgeMonths(int birthYear, int birthMonth, int birthDay) {
        Calendar dob = Calendar.getInstance();
        dob.set(birthYear, birthMonth, birthDay);
        Calendar today = Calendar.getInstance();

        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
        if (months < 0) { years--; months += 12; }

        int totalMonths = Math.max(0, years * 12 + months);
        etAgeMonths.setText(String.valueOf(totalMonths));
        etAgeYears.setText(String.valueOf(totalMonths / 12));
    }

    private void setupButtons() {
        if (entryId != -1) {
            btnSave.setText("UPDATE ENTRY");
        }
        btnSave.setOnClickListener(v -> { if (validateForm()) saveEntry(); });
        btnClear.setOnClickListener(v -> {
            if (entryId != -1) {
                loadEntryData();
            } else {
                clearForm();
            }
        });
    }

    private void loadEntryData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            OralHealthCareEntity entity = dbHelper.oralHealthCareDao().getById(entryId);
            if (entity != null && isAdded()) {
                requireActivity().runOnUiThread(() -> populateViews(entity));
            }
        });
    }

    private void populateViews(OralHealthCareEntity entity) {
        selectedProfileId = entity.profileId; // Load back tracking profile ID

        etDateOfVisit.setText(toDisplayDate(entity.dateOfVisit));
        etFamilySerial.setText(entity.familySerial);
        etName.setText(entity.name, false);
        etAddress.setText(entity.address);
        etDateOfBirth.setText(toDisplayDate(entity.dateOfBirth));
        etAgeMonths.setText(entity.ageMonths);

        if ("M".equals(entity.sex)) rbMale.setChecked(true);
        else if ("F".equals(entity.sex)) rbFemale.setChecked(true);

        cbOralScreening0.setChecked(entity.rpoc0OralScreening);
        cbRiskAssessment0.setChecked(entity.rpoc0RiskAssessment);
        cbOralHygiene.setChecked(entity.rpoc0OralHygiene);
        cbCounseling0.setChecked(entity.rpoc0Counseling);
        cbFluorideVarnish0.setChecked(entity.rpoc0FluorideVarnish);

        setRadioGroupValue(rgCompleteRpoc0, rbCompleteRpoc0Yes, rbCompleteRpoc0No, entity.completeRpoc0);

        etAgeYears.setText(entity.ageYears);
        setSelectedAgeGroup(actAgeGroup1st, entity.ageGroup1st);
        setSelectedAgeGroup(actAgeGroup2nd, entity.ageGroup2nd);

        etOralScreening1st.setText(toDisplayDate(entity.oralScreening1st));
        etOralScreening2nd.setText(toDisplayDate(entity.oralScreening2nd));
        etRiskAssessment1st.setText(toDisplayDate(entity.riskAssessment1st));
        etRiskAssessment2nd.setText(toDisplayDate(entity.riskAssessment2nd));
        etOralProphylaxis1st.setText(toDisplayDate(entity.oralProphylaxis1st));
        etOralProphylaxis2nd.setText(toDisplayDate(entity.oralProphylaxis2nd));
        etFluorideVarnish1st.setText(toDisplayDate(entity.fluorideVarnish1st));
        etFluorideVarnish2nd.setText(toDisplayDate(entity.fluorideVarnish2nd));
        etCounseling1st.setText(toDisplayDate(entity.counseling1st));
        etCounseling2nd.setText(toDisplayDate(entity.counseling2nd));

        setRadioGroupValue(rgCompleteRpoc1st, rbCompleteRpoc1stYes, rbCompleteRpoc1stNo, entity.completeRpoc1st);
        setRadioGroupValue(rgCompleteRpoc2nd, rbCompleteRpoc2ndYes, rbCompleteRpoc2ndNo, entity.completeRpoc2nd);

        if ("A".equals(entity.serviceLocation1st)) rbFacility1st.setChecked(true);
        else if ("B".equals(entity.serviceLocation1st)) rbNonFacility1st.setChecked(true);

        if ("A".equals(entity.serviceLocation2nd)) rbFacility2nd.setChecked(true);
        else if ("B".equals(entity.serviceLocation2nd)) rbNonFacility2nd.setChecked(true);

        etRemarks.setText(entity.remarks);
    }

    private void setSelectedAgeGroup(AutoCompleteTextView view, String code) {
        if (TextUtils.isEmpty(code)) return;
        for (String option : AGE_GROUP_OPTIONS) {
            if (option.startsWith(code)) {
                view.setText(option, false);
                break;
            }
        }
    }

    private void setRadioGroupValue(RadioGroup group, RadioButton yesBtn, RadioButton noBtn, int value) {
        if (value == 1) yesBtn.setChecked(true);
        else if (value == 0) noBtn.setChecked(true);
        else group.clearCheck();
    }

    // A date is valid when it is a real MM-dd-yyyy date that is not in the future
    private boolean isValidDate(String date) {
        Date d = parseDate(date);
        return d != null && !d.after(new Date());
    }

    // Validates one date field; required fields must also be non-empty
    private boolean checkDate(TextInputLayout til, TextInputEditText et, boolean required) {
        String text = getText(et);
        if (TextUtils.isEmpty(text)) {
            if (required) { til.setError("Required"); return false; }
            til.setError(null);
            return true;
        }
        if (!isValidDate(text)) {
            til.setError("Enter a valid date (MM-DD-YYYY)");
            return false;
        }
        til.setError(null);
        return true;
    }

    private boolean validateForm() {
        boolean valid = true;

        if (!checkDate(tilDateOfVisit, etDateOfVisit, true)) valid = false;

        if (TextUtils.isEmpty(getText(etName))) { tilName.setError("Required"); valid = false; } else tilName.setError(null);

        if (!checkDate(tilDateOfBirth, etDateOfBirth, true)) valid = false;

        if (rgSex.getCheckedRadioButtonId() == -1) { tvSexError.setVisibility(View.VISIBLE); valid = false; } else tvSexError.setVisibility(View.GONE);
        if (TextUtils.isEmpty(getText(etAddress))) { tilAddress.setError("Required"); valid = false; } else tilAddress.setError(null);

        if (!checkDate(tilOralScreening1st, etOralScreening1st, false)) valid = false;
        if (!checkDate(tilOralScreening2nd, etOralScreening2nd, false)) valid = false;
        if (!checkDate(tilRiskAssessment1st, etRiskAssessment1st, false)) valid = false;
        if (!checkDate(tilRiskAssessment2nd, etRiskAssessment2nd, false)) valid = false;
        if (!checkDate(tilOralProphylaxis1st, etOralProphylaxis1st, false)) valid = false;
        if (!checkDate(tilOralProphylaxis2nd, etOralProphylaxis2nd, false)) valid = false;
        if (!checkDate(tilFluorideVarnish1st, etFluorideVarnish1st, false)) valid = false;
        if (!checkDate(tilFluorideVarnish2nd, etFluorideVarnish2nd, false)) valid = false;
        if (!checkDate(tilCounseling1st, etCounseling1st, false)) valid = false;
        if (!checkDate(tilCounseling2nd, etCounseling2nd, false)) valid = false;

        if (!TextUtils.isEmpty(getText(etOralScreening1st)) && !TextUtils.isEmpty(getText(etOralScreening2nd))
                && isValidDate(getText(etOralScreening1st)) && isValidDate(getText(etOralScreening2nd))) {
            if (!isSecondVisitValid(getText(etOralScreening1st), getText(etOralScreening2nd))) {
                tilOralScreening2nd.setError("Must be ≥ 4 months after 1st visit");
                valid = false;
            } else {
                tilOralScreening2nd.setError(null);
            }
        }
        return valid;
    }

    private boolean isSecondVisitValid(String first, String second) {
        Date d1 = parseDate(first);
        Date d2 = parseDate(second);
        if (d1 == null || d2 == null) return true;
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        c1.add(Calendar.MONTH, 4);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        return !c2.before(c1);
    }

    private void saveEntry() {
        OralHealthCareEntity entry = new OralHealthCareEntity();

        if (entryId != -1) {
            entry.id = entryId;
        }

        String PREFS_NAME = "AppPrefs";
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        entry.userId = userId;

        entry.profileId = selectedProfileId; // Save tracking profile ID to entry
        entry.dateOfVisit  = getText(etDateOfVisit);
        entry.familySerial = getText(etFamilySerial);
        entry.name         = getText(etName);
        entry.address      = getText(etAddress);
        entry.dateOfBirth  = getText(etDateOfBirth);
        entry.ageMonths    = getText(etAgeMonths);
        entry.sex          = rbMale.isChecked() ? "M" : (rbFemale.isChecked() ? "F" : "");

        entry.rpoc0OralScreening   = cbOralScreening0.isChecked();
        entry.rpoc0RiskAssessment  = cbRiskAssessment0.isChecked();
        entry.rpoc0OralHygiene     = cbOralHygiene.isChecked();
        entry.rpoc0Counseling      = cbCounseling0.isChecked();
        entry.rpoc0FluorideVarnish = cbFluorideVarnish0.isChecked();

        entry.completeRpoc0 = resolveYesNo(rgCompleteRpoc0, rbCompleteRpoc0Yes.getId(), rbCompleteRpoc0No.getId());

        entry.ageYears    = getText(etAgeYears);
        entry.ageGroup1st = resolveAgeGroupCode(getText(actAgeGroup1st));
        entry.ageGroup2nd = resolveAgeGroupCode(getText(actAgeGroup2nd));

        entry.oralScreening1st   = getText(etOralScreening1st);
        entry.oralScreening2nd   = getText(etOralScreening2nd);
        entry.riskAssessment1st  = getText(etRiskAssessment1st);
        entry.riskAssessment2nd  = getText(etRiskAssessment2nd);
        entry.oralProphylaxis1st = getText(etOralProphylaxis1st);
        entry.oralProphylaxis2nd = getText(etOralProphylaxis2nd);
        entry.fluorideVarnish1st = getText(etFluorideVarnish1st);
        entry.fluorideVarnish2nd = getText(etFluorideVarnish2nd);
        entry.counseling1st      = getText(etCounseling1st);
        entry.counseling2nd      = getText(etCounseling2nd);

        entry.completeRpoc1st = resolveYesNo(rgCompleteRpoc1st, rbCompleteRpoc1stYes.getId(), rbCompleteRpoc1stNo.getId());
        entry.completeRpoc2nd = resolveYesNo(rgCompleteRpoc2nd, rbCompleteRpoc2ndYes.getId(), rbCompleteRpoc2ndNo.getId());

        entry.serviceLocation1st = rbFacility1st.isChecked() ? "A" : (rbNonFacility1st.isChecked() ? "B" : "");
        entry.serviceLocation2nd = rbFacility2nd.isChecked() ? "A" : (rbNonFacility2nd.isChecked() ? "B" : "");

        entry.remarks = getText(etRemarks);

        Executors.newSingleThreadExecutor().execute(() -> {
            if (entryId == -1) {
                entry.newInsert = true;
                dbHelper.oralHealthCareDao().insert(entry);
            } else {
                dbHelper.oralHealthCareDao().update(entry);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), (entryId == -1 ? "Saved: " : "Updated: ") + entry.name, Toast.LENGTH_SHORT).show();
                    if (entryId == -1) {
                        clearForm();
                    } else {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        });
    }

    private void clearForm() {
        selectedProfileId = -1; // Reset profile ID on clearing

        clearField(etDateOfVisit,  tilDateOfVisit);
        clearField(etFamilySerial, tilFamilySerial);
        clearField(etName,         tilName);
        clearField(etAddress,      tilAddress);
        clearField(etDateOfBirth,  tilDateOfBirth);
        clearField(etAgeMonths,    tilAgeMonths);
        rgSex.clearCheck();
        tvSexError.setVisibility(View.GONE);

        cbOralScreening0.setChecked(false);
        cbRiskAssessment0.setChecked(false);
        cbOralHygiene.setChecked(false);
        cbCounseling0.setChecked(false);
        cbFluorideVarnish0.setChecked(false);
        rgCompleteRpoc0.clearCheck();

        clearField(etAgeYears, tilAgeYears);
        actAgeGroup1st.setText("", false);
        actAgeGroup2nd.setText("", false);

        clearField(etOralScreening1st,   tilOralScreening1st);
        clearField(etOralScreening2nd,   tilOralScreening2nd);
        clearField(etRiskAssessment1st,  tilRiskAssessment1st);
        clearField(etRiskAssessment2nd,  tilRiskAssessment2nd);
        clearField(etOralProphylaxis1st, tilOralProphylaxis1st);
        clearField(etOralProphylaxis2nd, tilOralProphylaxis2nd);
        clearField(etFluorideVarnish1st, tilFluorideVarnish1st);
        clearField(etFluorideVarnish2nd, tilFluorideVarnish2nd);
        clearField(etCounseling1st,      tilCounseling1st);
        clearField(etCounseling2nd,      tilCounseling2nd);

        rgCompleteRpoc1st.clearCheck();
        rgCompleteRpoc2nd.clearCheck();
        rgServiceLocation1st.clearCheck();
        rgServiceLocation2nd.clearCheck();
        clearField(etRemarks, tilRemarks);
    }

    private String getText(TextInputEditText field) { return field.getText() != null ? field.getText().toString().trim() : ""; }
    private void clearField(TextInputEditText field, TextInputLayout layout) { field.setText(""); layout.setError(null); }

    private String getText(AutoCompleteTextView field) { return field.getText() != null ? field.getText().toString().trim() : ""; }
    private void clearField(AutoCompleteTextView field, TextInputLayout layout) { field.setText("", false); layout.setError(null); }

    private int resolveYesNo(RadioGroup group, int yesId, int noId) {
        int checked = group.getCheckedRadioButtonId();
        if (checked == yesId) return 1;
        if (checked == noId)  return 0;
        return -1;
    }

    private String resolveAgeGroupCode(String displayText) {
        if (displayText == null || displayText.isEmpty()) return "";
        return String.valueOf(displayText.charAt(0));
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
                if (s.length() == 3 || s.length() == 6) {
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
                if ((i == 1 && len > 2) || (i == 3 && len > 4)) {
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