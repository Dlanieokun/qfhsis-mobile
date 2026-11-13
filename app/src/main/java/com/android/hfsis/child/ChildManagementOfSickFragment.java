package com.android.hfsis.child;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
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
import com.android.hfsis.model.child.ChildSickRecord;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ChildManagementOfSickFragment extends Fragment {

    // Record state variables
    private long editingRecordId = -1;
    private long selectedProfileId = -1; // ---> ADDED VARIABLE
    private boolean isEditMode = false;
    private DatabaseHelper database;

    // Section 1 — Basic Information
    private TextInputLayout tilDateRegistration, tilFamilySerialNumber, tilChildName,
            tilDateOfBirth, tilAge, tilMotherName, tilAddress;
    private TextInputEditText etDateRegistration, etFamilySerialNumber, etDateOfBirth, etAge, etMotherName, etAddress;
    private AutoCompleteTextView etChildName;
    private RadioGroup rgSex;
    private RadioButton rbMale, rbFemale;
    private TextView tvSexError;

    // Section 2 — Vitamin A Supplementation
    private TextInputLayout tilVitaminADateGiven;
    private TextInputEditText etVitaminADateGiven;
    private CheckBox cbVitaminA100IU, cbVitaminA200IU;

    // Section 3 — Diagnosis & Management
    private CheckBox cbDiagnosisMeasles, cbDiagnosisPersistentDiarrhea;

    private TextInputLayout tilDiarrheaDateGiven;
    private TextInputEditText etDiarrheaDateGiven;
    private CheckBox cbOrsOnly, cbOrsAndZinc;

    private TextInputLayout tilPneumoniaDateGiven, tilPneumoniaOthersSpec;
    private TextInputEditText etPneumoniaDateGiven, etPneumoniaOthersSpec;
    private CheckBox cbAmoxicillinDrops, cbAmoxicillinClavulanate, cbCefuroxime, cbPneumoniaOthers;

    // Section 4 — Remarks
    private TextInputEditText etRemarks;

    // Action Buttons
    private MaterialButton btnClear, btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_management_of_sick, container, false);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupKeyboardInsets(view);
        database = DatabaseHelper.getInstance(getContext());

        initializeViews(view);
        setupDatePickerFields();
        setupChildNameAutoComplete();
        setupConditionalViews();

        // Check if we are opening in Edit mode
        if (getArguments() != null && getArguments().containsKey("RECORD_ID")) {
            editingRecordId = getArguments().getLong("RECORD_ID");
            isEditMode = true;
            btnSave.setText("UPDATE ENTRY");
            loadRecordData(editingRecordId);
        } else {
            // Automatically set the current date for a new record (MM-dd-yyyy)
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            etDateRegistration.setText(sdf.format(new Date()));
        }

        btnClear.setOnClickListener(v -> clearForm());
        btnSave.setOnClickListener(v -> saveRecord());
    }

    private void initializeViews(View view) {
        // Layout wrappers
        tilDateRegistration = view.findViewById(R.id.tilDateRegistration);
        tilFamilySerialNumber = view.findViewById(R.id.tilFamilySerialNumber);
        tilChildName = view.findViewById(R.id.tilChildName);
        tilDateOfBirth = view.findViewById(R.id.tilDateOfBirth);
        tilAge = view.findViewById(R.id.tilAge);
        tilMotherName = view.findViewById(R.id.tilMotherName);
        tilAddress = view.findViewById(R.id.tilAddress);
        tilVitaminADateGiven = view.findViewById(R.id.tilVitaminADateGiven);
        tilDiarrheaDateGiven = view.findViewById(R.id.tilDiarrheaDateGiven);
        tilPneumoniaDateGiven = view.findViewById(R.id.tilPneumoniaDateGiven);
        tilPneumoniaOthersSpec = view.findViewById(R.id.tilPneumoniaOthersSpec);

        // Edit Texts
        etDateRegistration = view.findViewById(R.id.etDateRegistration);
        etFamilySerialNumber = view.findViewById(R.id.etFamilySerialNumber);
        etChildName = view.findViewById(R.id.etChildName);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        etAge = view.findViewById(R.id.etAge);
        etMotherName = view.findViewById(R.id.etMotherName);
        etAddress = view.findViewById(R.id.etAddress);

        // Sex components
        rgSex = view.findViewById(R.id.rgSex);
        rbMale = view.findViewById(R.id.rbMale);
        rbFemale = view.findViewById(R.id.rbFemale);
        tvSexError = view.findViewById(R.id.tvSexError);

        // Vitamin A
        etVitaminADateGiven = view.findViewById(R.id.etVitaminADateGiven);
        cbVitaminA100IU = view.findViewById(R.id.cbVitaminA100IU);
        cbVitaminA200IU = view.findViewById(R.id.cbVitaminA200IU);

        // Illnesses
        cbDiagnosisMeasles = view.findViewById(R.id.cbDiagnosisMeasles);
        cbDiagnosisPersistentDiarrhea = view.findViewById(R.id.cbDiagnosisPersistentDiarrhea);

        // Diarrhea logic
        etDiarrheaDateGiven = view.findViewById(R.id.etDiarrheaDateGiven);
        cbOrsOnly = view.findViewById(R.id.cbOrsOnly);
        cbOrsAndZinc = view.findViewById(R.id.cbOrsAndZinc);

        // Pneumonia layer
        etPneumoniaDateGiven = view.findViewById(R.id.etPneumoniaDateGiven);
        cbAmoxicillinDrops = view.findViewById(R.id.cbAmoxicillinDrops);
        cbAmoxicillinClavulanate = view.findViewById(R.id.cbAmoxicillinClavulanate);
        cbCefuroxime = view.findViewById(R.id.cbCefuroxime);
        cbPneumoniaOthers = view.findViewById(R.id.cbPneumoniaOthers);
        etPneumoniaOthersSpec = view.findViewById(R.id.etPneumoniaOthersSpec);

        etRemarks = view.findViewById(R.id.etRemarks);
        btnClear = view.findViewById(R.id.btnClear);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupDatePickerFields() {
        bindDatePicker(tilDateRegistration, etDateRegistration);
        bindDatePicker(tilDateOfBirth, etDateOfBirth);
        bindDatePicker(tilVitaminADateGiven, etVitaminADateGiven);
        bindDatePicker(tilDiarrheaDateGiven, etDiarrheaDateGiven);
        bindDatePicker(tilPneumoniaDateGiven, etPneumoniaDateGiven);

        // Auto-compute age (in months) while the DOB is typed manually
        etDateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    calculateAgeInMonths(s.toString());
                } else if (s.length() == 0) {
                    etAge.setText("");
                }
            }
        });
    }

    // Formats keyboard input as MM-dd-yyyy while typing, and opens the
    // calendar dialog from the end icon (same behavior as ProfilingFragment).
    private void bindDatePicker(TextInputLayout layout, EditText editText) {
        if (layout == null || editText == null) return;

        editText.addTextChangedListener(new DateFormattingWatcher(editText));
        layout.setEndIconOnClickListener(v -> showDatePickerDialog(editText));
    }

    private void showDatePickerDialog(EditText targetEditText) {
        if (getContext() == null) return;

        // Start on the date already in the field (if valid), otherwise today
        Calendar calendar = Calendar.getInstance();
        Date existing = parseDate(targetEditText.getText().toString().trim());
        if (existing != null) calendar.setTime(existing);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selYear, selMonth, selDay) -> {
                    Calendar selCalendar = Calendar.getInstance();
                    selCalendar.set(Calendar.YEAR, selYear);
                    selCalendar.set(Calendar.MONTH, selMonth);
                    selCalendar.set(Calendar.DAY_OF_MONTH, selDay);

                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
                    String formattedDate = sdf.format(selCalendar.getTime());
                    targetEditText.setText(formattedDate);
                    targetEditText.setError(null);

                    if (targetEditText == etDateOfBirth) {
                        calculateAgeInMonths(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
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

    // Returns true when every non-empty date field holds a valid, non-future date
    private boolean validateDateFields() {
        EditText[] fields = new EditText[]{
                etDateRegistration, etDateOfBirth, etVitaminADateGiven,
                etDiarrheaDateGiven, etPneumoniaDateGiven
        };
        Date now = new Date();
        for (EditText f : fields) {
            if (f == null) continue;
            String text = f.getText().toString().trim();
            if (text.isEmpty()) continue;
            Date d = parseDate(text);
            if (d == null || d.after(now)) {
                f.setError("Enter a valid date (MM-DD-YYYY)");
                f.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void setupConditionalViews() {
        cbPneumoniaOthers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tilPneumoniaOthersSpec.setVisibility(View.VISIBLE);
            } else {
                tilPneumoniaOthersSpec.setVisibility(View.GONE);
                etPneumoniaOthersSpec.setText("");
            }
        });
    }

    private void setupChildNameAutoComplete() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (database != null) {
                List<HouseholdProfile> profiles = database.householdProfileDao().getAllProfiles();
                if (getActivity() != null && profiles != null && !profiles.isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        List<String> namesList = new ArrayList<>();
                        for (HouseholdProfile p : profiles) {
                            String mid = (p.memberMiddleName != null && !p.memberMiddleName.trim().isEmpty()) ? " " + p.memberMiddleName.trim() : "";
                            namesList.add(p.memberFirstName.trim() + mid + " " + p.memberLastName.trim());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, namesList);
                        etChildName.setAdapter(adapter);
                        etChildName.setOnItemClickListener((parent, view, position, id) -> {
                            HouseholdProfile profile = profiles.get(position);

                            // ---> SAVE THE ID OF THE SELECTED PROFILE
                            selectedProfileId = profile.id; // Note: Use profile.getId() if id is private in HouseholdProfile.

                            etFamilySerialNumber.setText(profile.hhNumber);
                            String dobDisplay = toDisplayDate(profile.dob);
                            etDateOfBirth.setText(dobDisplay);
                            calculateAgeInMonths(dobDisplay);

                            String sitioPart = (profile.sitio != null && !profile.sitio.isEmpty()) ? profile.sitio : "";
                            String brgyPart = (profile.barangay != null) ? ", " + profile.barangay : "";
                            String munPart = (profile.municipality != null) ? ", " + profile.municipality : "";
                            String provPart = (profile.province != null) ? ", " + profile.province : "";
                            String regPart = (profile.region != null) ? ", " + profile.region : "";
                            etAddress.setText(sitioPart + brgyPart + munPart + provPart + regPart);

                            if ("Male".equalsIgnoreCase(profile.sex)) {
                                rbMale.setChecked(true);
                            } else if ("Female".equalsIgnoreCase(profile.sex)) {
                                rbFemale.setChecked(true);
                            }
                        });
                    });
                }
            }
        });
    }

    private void calculateAgeInMonths(String dobString) {
        if (TextUtils.isEmpty(dobString)) return;
        try {
            Date birthDate = parseDate(toDisplayDate(dobString));
            if (birthDate != null) {
                Calendar birth = Calendar.getInstance();
                birth.setTime(birthDate);
                Calendar today = Calendar.getInstance();

                int months = (today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)) * 12 + today.get(Calendar.MONTH) - birth.get(Calendar.MONTH);
                if (today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)) {
                    months--;
                }
                etAge.setText(String.valueOf(Math.max(0, months)));
            }
        } catch (Exception ignored) {}
    }

    private void loadRecordData(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (database != null) {
                ChildSickRecord record = database.childSickDao().getRecordById(id);
                if (record != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // ---> LOAD PROFILE ID <---
                        selectedProfileId = record.getProfileId();

                        etDateRegistration.setText(toDisplayDate(record.getDateRegistration()));
                        etFamilySerialNumber.setText(record.getFamilySerialNumber());
                        etChildName.setText(record.getChildName());
                        etDateOfBirth.setText(toDisplayDate(record.getDateOfBirth()));
                        etAge.setText(record.getAgeMonths());
                        etMotherName.setText(record.getMotherName());
                        etAddress.setText(record.getAddress());

                        if ("Male".equalsIgnoreCase(record.getSex())) {
                            rbMale.setChecked(true);
                        } else if ("Female".equalsIgnoreCase(record.getSex())) {
                            rbFemale.setChecked(true);
                        }

                        etVitaminADateGiven.setText(toDisplayDate(record.getVitaminADateGiven()));
                        cbVitaminA100IU.setChecked(record.isVitaminA100IU());
                        cbVitaminA200IU.setChecked(record.isVitaminA200IU());

                        cbDiagnosisMeasles.setChecked(record.isDiagnosisMeasles());
                        cbDiagnosisPersistentDiarrhea.setChecked(record.isDiagnosisPersistentDiarrhea());

                        etDiarrheaDateGiven.setText(toDisplayDate(record.getDiarrheaDateGiven()));
                        cbOrsOnly.setChecked(record.isOrsOnly());
                        cbOrsAndZinc.setChecked(record.isOrsAndZinc());

                        etPneumoniaDateGiven.setText(toDisplayDate(record.getPneumoniaDateGiven()));
                        cbAmoxicillinDrops.setChecked(record.isAmoxicillinDrops());
                        cbAmoxicillinClavulanate.setChecked(record.isAmoxicillinClavulanate());
                        cbCefuroxime.setChecked(record.isCefuroxime());
                        cbPneumoniaOthers.setChecked(record.isPneumoniaOthers());
                        etPneumoniaOthersSpec.setText(record.getPneumoniaOthersSpec());

                        etRemarks.setText(record.getRemarks());
                    });
                }
            }
        });
    }

    private void saveRecord() {
        String dateReg = getText(etDateRegistration);
        String childName = getText(etChildName);
        String sex = rbMale.isChecked() ? "Male" : (rbFemale.isChecked() ? "Female" : "");

        boolean hasError = false;
        tilDateRegistration.setError(null);
        tilChildName.setError(null);
        tvSexError.setVisibility(View.GONE);

        if (TextUtils.isEmpty(dateReg)) {
            tilDateRegistration.setError("Registration date is required");
            hasError = true;
        }
        if (TextUtils.isEmpty(childName)) {
            tilChildName.setError("Child full name is required");
            hasError = true;
        }
        if (TextUtils.isEmpty(sex)) {
            tvSexError.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (hasError) {
            Toast.makeText(getContext(), "Please resolve highlighted validation errors.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateDateFields()) {
            Toast.makeText(getContext(), "Please check the highlighted date", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            ChildSickRecord record = isEditMode ? database.childSickDao().getRecordById(editingRecordId) : new ChildSickRecord();
            if (record == null) record = new ChildSickRecord();

            String PREFS_NAME = "AppPrefs";
            SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            record.setUserId(userId);

            // ---> PASS PROFILE ID TO OBJECT <---
            record.setProfileId(selectedProfileId);

            record.setDateRegistration(dateReg);
            record.setFamilySerialNumber(getText(etFamilySerialNumber));
            record.setChildName(childName);
            record.setDateOfBirth(getText(etDateOfBirth));
            record.setAgeMonths(getText(etAge));
            record.setSex(sex);
            record.setMotherName(getText(etMotherName));
            record.setAddress(getText(etAddress));

            record.setVitaminADateGiven(getText(etVitaminADateGiven));
            record.setVitaminA100IU(cbVitaminA100IU.isChecked());
            record.setVitaminA200IU(cbVitaminA200IU.isChecked());

            record.setDiagnosisMeasles(cbDiagnosisMeasles.isChecked());
            record.setDiagnosisPersistentDiarrhea(cbDiagnosisPersistentDiarrhea.isChecked());

            record.setDiarrheaDateGiven(getText(etDiarrheaDateGiven));
            record.setOrsOnly(cbOrsOnly.isChecked());
            record.setOrsAndZinc(cbOrsAndZinc.isChecked());

            record.setPneumoniaDateGiven(getText(etPneumoniaDateGiven));
            record.setAmoxicillinDrops(cbAmoxicillinDrops.isChecked());
            record.setAmoxicillinClavulanate(cbAmoxicillinClavulanate.isChecked());
            record.setCefuroxime(cbCefuroxime.isChecked());
            record.setPneumoniaOthers(cbPneumoniaOthers.isChecked());
            record.setPneumoniaOthersSpec(getText(etPneumoniaOthersSpec));

            record.setRemarks(getText(etRemarks));

            if (database != null) {
                if (isEditMode) {
                    record.setSynced(false);
                    record.setUpdatedAt(System.currentTimeMillis());
                    database.childSickDao().update(record);
                } else {
                    record.setNewInsert(true);
                    database.childSickDao().insert(record);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), isEditMode ? "Record updated successfully!" : "Record saved successfully!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            }
        });
    }

    private void clearForm() {
        selectedProfileId = -1; // ---> RESET VARIABLE

        etDateRegistration.setText("");
        etFamilySerialNumber.setText("");
        etChildName.setText("");
        etDateOfBirth.setText("");
        etAge.setText("");
        rgSex.clearCheck();
        etMotherName.setText("");
        etAddress.setText("");

        etVitaminADateGiven.setText("");
        cbVitaminA100IU.setChecked(false);
        cbVitaminA200IU.setChecked(false);

        cbDiagnosisMeasles.setChecked(false);
        cbDiagnosisPersistentDiarrhea.setChecked(false);

        etDiarrheaDateGiven.setText("");
        cbOrsOnly.setChecked(false);
        cbOrsAndZinc.setChecked(false);

        etPneumoniaDateGiven.setText("");
        cbAmoxicillinDrops.setChecked(false);
        cbAmoxicillinClavulanate.setChecked(false);
        cbCefuroxime.setChecked(false);
        cbPneumoniaOthers.setChecked(false);
        etPneumoniaOthersSpec.setText("");
        tilPneumoniaOthersSpec.setVisibility(View.GONE);

        etRemarks.setText("");
    }

    private String getText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private String getText(AutoCompleteTextView field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    // Same auto-formatting watcher used in ProfilingFragment (types as MM-dd-yyyy)
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