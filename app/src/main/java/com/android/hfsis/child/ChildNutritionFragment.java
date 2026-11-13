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
import com.android.hfsis.model.child.ChildNutritionRecord;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ChildNutritionFragment extends Fragment {

    // Record tracking properties
    private long selectedProfileId = -1;
    private long editingRecordId = -1;
    private boolean isEditMode = false;
    private DatabaseHelper database;

    // --- Section 1: Basic Information ---
    private TextInputLayout tilDateRegistration, tilFamilySerialNumber, tilChildName, tilDateOfBirth, tilAge, tilMotherName, tilAddress;
    private TextInputEditText etDateRegistration, etFamilySerialNumber, etDateOfBirth, etAge, etMotherName, etAddress;
    private AutoCompleteTextView etChildName;
    private RadioGroup rgSex;

    // --- Section 2: Newborn Assessment ---
    private TextInputLayout tilLengthAtBirth, tilWeightAtBirth, tilBirthWeightStatus, tilBreastfeedingDate, tilPlaceOfDelivery;
    private TextInputEditText etLengthAtBirth, etWeightAtBirth, etBreastfeedingDate, etPlaceOfDelivery;
    private AutoCompleteTextView etBirthWeightStatus;

    // --- Section 3: Iron Supplementation ---
    private TextInputLayout tilIron1Month, tilIron2Months, tilIron3Months, tilIronCompletedDate;
    private TextInputEditText etIron1Month, etIron2Months, etIron3Months, etIronCompletedDate;
    private RadioGroup rgIronCompleted;

    // --- Section 4: Vitamin A Supplementation ---
    private TextInputLayout tilVitaA6to11, tilVitaA200Y1D1, tilVitaA200Y1D2, tilVitaA200Y2D1, tilVitaA200Y2D2, tilVitaA200Y3D1, tilVitaA200Y3D2, tilVitaA200Y4D1, tilVitaA200Y4D2;
    private TextInputEditText etVitaA6to11, etVitaA200Y1D1, etVitaA200Y1D2, etVitaA200Y2D1, etVitaA200Y2D2, etVitaA200Y3D1, etVitaA200Y3D2, etVitaA200Y4D1, etVitaA200Y4D2;

    // --- Section 5: MNP Supplementation ---
    private TextInputLayout tilMnp6to11Provided, tilMnp6to11Completed, tilMnp6to11Remarks;
    private TextInputLayout tilMnp12to23Provided, tilMnp12to23Completed, tilMnp12to23Remarks;
    private TextInputEditText etMnp6to11Provided, etMnp6to11Completed, etMnp6to11Remarks;
    private TextInputEditText etMnp12to23Provided, etMnp12to23Completed, etMnp12to23Remarks;

    // --- Section 6: LNS-SQ Supplementation ---
    private TextInputLayout tilLns6to11Provided, tilLns6to11Completed, tilLns6to11Remarks;
    private TextInputLayout tilLns12to23Provided, tilLns12to23Completed, tilLns12to23Remarks;
    private TextInputEditText etLns6to11Provided, etLns6to11Completed, etLns6to11Remarks;
    private TextInputEditText etLns12to23Provided, etLns12to23Completed, etLns12to23Remarks;

    // --- Section 7: MAM — SFP ---
    private RadioGroup rgMamIdentified, rgMamEnrolled, rgMamCured, rgMamNonCured, rgMamDefaulted, rgMamDied;

    // --- Section 8: SAM — OTC ---
    private RadioGroup rgSamIdentified, rgSamAdmitted, rgSamCured, rgSamNonCured, rgSamDefaulted, rgSamDied;

    // --- Section 9: Remarks ---
    private TextInputLayout tilRemarks;
    private TextInputEditText etRemarks;

    // Form Action Buttons
    private MaterialButton btnClear, btnSave;
    private TextView tvFormTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_nutrition, container, false);
        setupKeyboardInsets(view);
        database = DatabaseHelper.getInstance(requireContext().getApplicationContext());

        initViews(view);
        setupDropdowns();
        setupDatePickers();

        if (getArguments() != null) {
            long incomingId = getArguments().getLong("recordId", -1);
            if (incomingId == -1) {
                incomingId = getArguments().getLong("EDIT_RECORD_ID", -1);
            }
            if (incomingId != -1) {
                editingRecordId = incomingId;
                isEditMode = true;
                tvFormTitle.setText("UPDATE CHILD NUTRITION ENTRY");
                btnSave.setText("UPDATE ENTRY");
                loadExistingRecord(editingRecordId);
            }
        }else {
            // Automatically set the current date for a new record (MM-dd-yyyy)
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            etDateRegistration.setText(sdf.format(new Date()));
        }

        btnSave.setOnClickListener(v -> saveOrUpdateRecord());
        btnClear.setOnClickListener(v -> clearAllFields());

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

    private void initViews(View view) {
        tvFormTitle = view.findViewById(R.id.tvFormTitle);

        // Section 1
        tilDateRegistration = view.findViewById(R.id.tilDateRegistration);
        tilFamilySerialNumber = view.findViewById(R.id.tilFamilySerialNumber);
        tilChildName = view.findViewById(R.id.tilChildName);
        tilDateOfBirth = view.findViewById(R.id.tilDateOfBirth);
        tilAge = view.findViewById(R.id.tilAge);
        tilMotherName = view.findViewById(R.id.tilMotherName);
        tilAddress = view.findViewById(R.id.tilAddress);

        etDateRegistration = view.findViewById(R.id.etDateRegistration);
        etFamilySerialNumber = view.findViewById(R.id.etFamilySerialNumber);
        etChildName = view.findViewById(R.id.etChildName);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        etAge = view.findViewById(R.id.etAge);
        etMotherName = view.findViewById(R.id.etMotherName);
        etAddress = view.findViewById(R.id.etAddress);
        rgSex = view.findViewById(R.id.rgSex);

        // Section 2
        tilLengthAtBirth = view.findViewById(R.id.tilLengthAtBirth);
        tilWeightAtBirth = view.findViewById(R.id.tilWeightAtBirth);
        tilBirthWeightStatus = view.findViewById(R.id.tilBirthWeightStatus);
        tilBreastfeedingDate = view.findViewById(R.id.tilBreastfeedingDate);
        tilPlaceOfDelivery = view.findViewById(R.id.tilPlaceOfDelivery);

        etLengthAtBirth = view.findViewById(R.id.etLengthAtBirth);
        etWeightAtBirth = view.findViewById(R.id.etWeightAtBirth);
        etBirthWeightStatus = view.findViewById(R.id.actBirthWeightStatus);
        etBreastfeedingDate = view.findViewById(R.id.etBreastfeedingDate);
        etPlaceOfDelivery = view.findViewById(R.id.etPlaceOfDelivery);

        // Section 3
        tilIron1Month = view.findViewById(R.id.tilIron1Month);
        tilIron2Months = view.findViewById(R.id.tilIron2Months);
        tilIron3Months = view.findViewById(R.id.tilIron3Months);
        tilIronCompletedDate = view.findViewById(R.id.tilIronCompletedDate);

        etIron1Month = view.findViewById(R.id.etIron1Month);
        etIron2Months = view.findViewById(R.id.etIron2Months);
        etIron3Months = view.findViewById(R.id.etIron3Months);
        etIronCompletedDate = view.findViewById(R.id.etIronCompletedDate);
        rgIronCompleted = view.findViewById(R.id.rgIronCompleted);

        // Section 4
        tilVitaA6to11 = view.findViewById(R.id.tilVitaA6to11);
        tilVitaA200Y1D1 = view.findViewById(R.id.tilVitaA200Y1D1);
        tilVitaA200Y1D2 = view.findViewById(R.id.tilVitaA200Y1D2);
        tilVitaA200Y2D1 = view.findViewById(R.id.tilVitaA200Y2D1);
        tilVitaA200Y2D2 = view.findViewById(R.id.tilVitaA200Y2D2);
        tilVitaA200Y3D1 = view.findViewById(R.id.tilVitaA200Y3D1);
        tilVitaA200Y3D2 = view.findViewById(R.id.tilVitaA200Y3D2);
        tilVitaA200Y4D1 = view.findViewById(R.id.tilVitaA200Y4D1);
        tilVitaA200Y4D2 = view.findViewById(R.id.tilVitaA200Y4D2);

        etVitaA6to11 = view.findViewById(R.id.etVitaA6to11);
        etVitaA200Y1D1 = view.findViewById(R.id.etVitaA200Y1D1);
        etVitaA200Y1D2 = view.findViewById(R.id.etVitaA200Y1D2);
        etVitaA200Y2D1 = view.findViewById(R.id.etVitaA200Y2D1);
        etVitaA200Y2D2 = view.findViewById(R.id.etVitaA200Y2D2);
        etVitaA200Y3D1 = view.findViewById(R.id.etVitaA200Y3D1);
        etVitaA200Y3D2 = view.findViewById(R.id.etVitaA200Y3D2);
        etVitaA200Y4D1 = view.findViewById(R.id.etVitaA200Y4D1);
        etVitaA200Y4D2 = view.findViewById(R.id.etVitaA200Y4D2);

        // Section 5
        tilMnp6to11Provided = view.findViewById(R.id.tilMnp6to11Provided);
        tilMnp6to11Completed = view.findViewById(R.id.tilMnp6to11Completed);
        tilMnp6to11Remarks = view.findViewById(R.id.tilMnp6to11Remarks);
        tilMnp12to23Provided = view.findViewById(R.id.tilMnp12to23Provided);
        tilMnp12to23Completed = view.findViewById(R.id.tilMnp12to23Completed);
        tilMnp12to23Remarks = view.findViewById(R.id.tilMnp12to23Remarks);

        etMnp6to11Provided = view.findViewById(R.id.etMnp6to11Provided);
        etMnp6to11Completed = view.findViewById(R.id.etMnp6to11Completed);
        etMnp6to11Remarks = view.findViewById(R.id.etMnp6to11Remarks);
        etMnp12to23Provided = view.findViewById(R.id.etMnp12to23Provided);
        etMnp12to23Completed = view.findViewById(R.id.etMnp12to23Completed);
        etMnp12to23Remarks = view.findViewById(R.id.etMnp12to23Remarks);

        // Section 6
        tilLns6to11Provided = view.findViewById(R.id.tilLns6to11Provided);
        tilLns6to11Completed = view.findViewById(R.id.tilLns6to11Completed);
        tilLns6to11Remarks = view.findViewById(R.id.tilLns6to11Remarks);
        tilLns12to23Provided = view.findViewById(R.id.tilLns12to23Provided);
        tilLns12to23Completed = view.findViewById(R.id.tilLns12to23Completed);
        tilLns12to23Remarks = view.findViewById(R.id.tilLns12to23Remarks);

        etLns6to11Provided = view.findViewById(R.id.etLns6to11Provided);
        etLns6to11Completed = view.findViewById(R.id.etLns6to11Completed);
        etLns6to11Remarks = view.findViewById(R.id.etLns6to11Remarks);
        etLns12to23Provided = view.findViewById(R.id.etLns12to23Provided);
        etLns12to23Completed = view.findViewById(R.id.etLns12to23Completed);
        etLns12to23Remarks = view.findViewById(R.id.etLns12to23Remarks);

        // Section 7
        rgMamIdentified = view.findViewById(R.id.rgMamIdentified);
        rgMamEnrolled = view.findViewById(R.id.rgMamEnrolled);
        rgMamCured = view.findViewById(R.id.rgMamCured);
        rgMamNonCured = view.findViewById(R.id.rgMamNonCured);
        rgMamDefaulted = view.findViewById(R.id.rgMamDefaulted);
        rgMamDied = view.findViewById(R.id.rgMamDied);

        // Section 8
        rgSamIdentified = view.findViewById(R.id.rgSamIdentified);
        rgSamAdmitted = view.findViewById(R.id.rgSamAdmitted);
        rgSamCured = view.findViewById(R.id.rgSamCured);
        rgSamNonCured = view.findViewById(R.id.rgSamNonCured);
        rgSamDefaulted = view.findViewById(R.id.rgSamDefaulted);
        rgSamDied = view.findViewById(R.id.rgSamDied);

        // Section 9
        tilRemarks = view.findViewById(R.id.tilRemarks);
        etRemarks = view.findViewById(R.id.etRemarks);

        btnClear = view.findViewById(R.id.btnClear);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupDropdowns() {
        // 1. Existing Birth Weight Status
        String[] weightStatuses = {"L — Low Birth Weight", "N — Normal", "U — Unknown"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, weightStatuses);
        etBirthWeightStatus.setAdapter(adapter);

        // 2. Bind etChildName Autocomplete using HouseholdProfileDao
        if (database != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                List<String> householdNames = database.householdProfileDao().getAllHouseholdNames();

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                householdNames
                        );
                        etChildName.setAdapter(nameAdapter);

                        etChildName.setOnItemClickListener((parent, view, position, id) -> {
                            String selectedName = (String) parent.getItemAtPosition(position);

                            Executors.newSingleThreadExecutor().execute(() -> {
                                HouseholdProfile profile = database.householdProfileDao().getProfileByCalculatedName(selectedName);

                                if (profile != null && isAdded() && getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        selectedProfileId = profile.id;
                                        etFamilySerialNumber.setText(profile.hhNumber);

                                        // Auto-fill DOB and auto-calculate Age
                                        if (TextUtils.isEmpty(getText(etDateOfBirth)) && !TextUtils.isEmpty(profile.dob)) {
                                            String dobDisplay = toDisplayDate(profile.dob);
                                            etDateOfBirth.setText(dobDisplay);
                                            calculateAgeInMonths(dobDisplay);
                                        }

                                        if (rgSex.getCheckedRadioButtonId() == -1 && !TextUtils.isEmpty(profile.sex)) {
                                            if (profile.sex.equalsIgnoreCase("Male") || profile.sex.equalsIgnoreCase("M")) {
                                                rgSex.check(R.id.rbMale);
                                            } else if (profile.sex.equalsIgnoreCase("Female") || profile.sex.equalsIgnoreCase("F")) {
                                                rgSex.check(R.id.rbFemale);
                                            }
                                        }

                                        String sitioPart = (profile.sitio != null && !profile.sitio.isEmpty()) ? profile.sitio : "";
                                        String brgyPart = (profile.barangay != null) ? ", " + profile.barangay : "";
                                        String munPart = (profile.municipality != null) ? ", " + profile.municipality : "";
                                        String provPart = (profile.province != null) ? ", " + profile.province : "";
                                        String regPart = (profile.region != null) ? ", " + profile.region : "";
                                        etAddress.setText(sitioPart + brgyPart + munPart + provPart + regPart);

                                    });
                                }
                            });
                        });
                    });
                }
            });
        }
    }

    private void setupDatePickers() {
        bindDatePicker(tilDateRegistration, etDateRegistration);
        bindDatePicker(tilDateOfBirth, etDateOfBirth);
        bindDatePicker(tilBreastfeedingDate, etBreastfeedingDate);
        bindDatePicker(tilIron1Month, etIron1Month);
        bindDatePicker(tilIron2Months, etIron2Months);
        bindDatePicker(tilIron3Months, etIron3Months);
        bindDatePicker(tilIronCompletedDate, etIronCompletedDate);
        bindDatePicker(tilVitaA6to11, etVitaA6to11);
        bindDatePicker(tilVitaA200Y1D1, etVitaA200Y1D1);
        bindDatePicker(tilVitaA200Y1D2, etVitaA200Y1D2);
        bindDatePicker(tilVitaA200Y2D1, etVitaA200Y2D1);
        bindDatePicker(tilVitaA200Y2D2, etVitaA200Y2D2);
        bindDatePicker(tilVitaA200Y3D1, etVitaA200Y3D1);
        bindDatePicker(tilVitaA200Y3D2, etVitaA200Y3D2);
        bindDatePicker(tilVitaA200Y4D1, etVitaA200Y4D1);
        bindDatePicker(tilVitaA200Y4D2, etVitaA200Y4D2);
        bindDatePicker(tilMnp6to11Provided, etMnp6to11Provided);
        bindDatePicker(tilMnp6to11Completed, etMnp6to11Completed);
        bindDatePicker(tilMnp12to23Provided, etMnp12to23Provided);
        bindDatePicker(tilMnp12to23Completed, etMnp12to23Completed);
        bindDatePicker(tilLns6to11Provided, etLns6to11Provided);
        bindDatePicker(tilLns6to11Completed, etLns6to11Completed);
        bindDatePicker(tilLns12to23Provided, etLns12to23Provided);
        bindDatePicker(tilLns12to23Completed, etLns12to23Completed);

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
        Date existing = parseDate(getText(targetEditText));
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

                    // Instantly calculate age if the target field is Date of Birth
                    if (targetEditText.getId() == R.id.etDateOfBirth) {
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

    // Returns true when every visible, non-empty date field holds a valid, non-future date
    private boolean validateDateFields() {
        EditText[] fields = new EditText[]{
                etDateRegistration,
                etDateOfBirth,
                etBreastfeedingDate,
                etIron1Month,
                etIron2Months,
                etIron3Months,
                etIronCompletedDate,
                etVitaA6to11,
                etVitaA200Y1D1,
                etVitaA200Y1D2,
                etVitaA200Y2D1,
                etVitaA200Y2D2,
                etVitaA200Y3D1,
                etVitaA200Y3D2,
                etVitaA200Y4D1,
                etVitaA200Y4D2,
                etMnp6to11Provided,
                etMnp6to11Completed,
                etMnp12to23Provided,
                etMnp12to23Completed,
                etLns6to11Provided,
                etLns6to11Completed,
                etLns12to23Provided,
                etLns12to23Completed
        };
        Date now = new Date();
        for (EditText f : fields) {
            if (f == null || !f.isShown()) continue;
            String text = getText(f);
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

    private void calculateAgeInMonths(String dobString) {
        if (TextUtils.isEmpty(dobString)) return;
        try {
            Date birthDate = parseDate(toDisplayDate(dobString));
            if (birthDate != null) {
                Calendar birth = Calendar.getInstance();
                birth.setTime(birthDate);
                Calendar today = Calendar.getInstance();

                int months = (today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)) * 12
                        + today.get(Calendar.MONTH) - birth.get(Calendar.MONTH);

                // Adjust if the current day of the month is before the birth day of the month
                if (today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)) {
                    months--;
                }
                etAge.setText(String.valueOf(Math.max(0, months)));
            }
        } catch (Exception ignored) {}
    }

    private void loadExistingRecord(long id) {
        if (database == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            ChildNutritionRecord record = database.childNutritionDao().getAllRecords().stream()
                    .filter(r -> r.getId() == id).findFirst().orElse(null);

            if (record != null && isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    selectedProfileId = record.getProfileId();
                    etDateRegistration.setText(toDisplayDate(record.getDateRegistration()));
                    etFamilySerialNumber.setText(record.getFamilySerialNumber());
                    etChildName.setText(record.getChildName());
                    etDateOfBirth.setText(toDisplayDate(record.getDateOfBirth()));
                    etAge.setText(record.getAgeMonths());
                    etMotherName.setText(record.getMotherName());
                    etAddress.setText(record.getAddress());

                    if ("M".equalsIgnoreCase(record.getSex())) rgSex.check(R.id.rbMale);
                    else if ("F".equalsIgnoreCase(record.getSex())) rgSex.check(R.id.rbFemale);

                    etLengthAtBirth.setText(record.getLengthAtBirth());
                    etWeightAtBirth.setText(record.getWeightAtBirth());

                    String bwStatus = record.getBirthWeightStatus();
                    if (!TextUtils.isEmpty(bwStatus)) {
                        if ("L".equalsIgnoreCase(bwStatus)) etBirthWeightStatus.setText("L — Low Birth Weight", false);
                        else if ("N".equalsIgnoreCase(bwStatus)) etBirthWeightStatus.setText("N — Normal", false);
                        else if ("U".equalsIgnoreCase(bwStatus)) etBirthWeightStatus.setText("U — Unknown", false);
                    }

                    etBreastfeedingDate.setText(toDisplayDate(record.getBreastfeedingDate()));
                    etPlaceOfDelivery.setText(record.getPlaceOfDelivery());

                    etIron1Month.setText(toDisplayDate(record.getIron1Month()));
                    etIron2Months.setText(toDisplayDate(record.getIron2Months()));
                    etIron3Months.setText(toDisplayDate(record.getIron3Months()));
                    setRadioValue(rgIronCompleted, record.getIronCompleted());
                    etIronCompletedDate.setText(toDisplayDate(record.getIronCompletedDate()));

                    etVitaA6to11.setText(toDisplayDate(record.getVitaA6to11()));
                    etVitaA200Y1D1.setText(toDisplayDate(record.getVitaA200Y1D1()));
                    etVitaA200Y1D2.setText(toDisplayDate(record.getVitaA200Y1D2()));
                    etVitaA200Y2D1.setText(toDisplayDate(record.getVitaA200Y2D1()));
                    etVitaA200Y2D2.setText(toDisplayDate(record.getVitaA200Y2D2()));
                    etVitaA200Y3D1.setText(toDisplayDate(record.getVitaA200Y3D1()));
                    etVitaA200Y3D2.setText(toDisplayDate(record.getVitaA200Y3D2()));
                    etVitaA200Y4D1.setText(toDisplayDate(record.getVitaA200Y4D1()));
                    etVitaA200Y4D2.setText(toDisplayDate(record.getVitaA200Y4D2()));

                    etMnp6to11Provided.setText(toDisplayDate(record.getMnp6to11Provided()));
                    etMnp6to11Completed.setText(toDisplayDate(record.getMnp6to11Completed()));
                    etMnp6to11Remarks.setText(record.getMnp6to11Remarks());
                    etMnp12to23Provided.setText(toDisplayDate(record.getMnp12to23Provided()));
                    etMnp12to23Completed.setText(toDisplayDate(record.getMnp12to23Completed()));
                    etMnp12to23Remarks.setText(record.getMnp12to23Remarks());

                    etLns6to11Provided.setText(toDisplayDate(record.getLns6to11Provided()));
                    etLns6to11Completed.setText(toDisplayDate(record.getLns6to11Completed()));
                    etLns6to11Remarks.setText(record.getLns6to11Remarks());
                    etLns12to23Provided.setText(toDisplayDate(record.getLns12to23Provided()));
                    etLns12to23Completed.setText(toDisplayDate(record.getLns12to23Completed()));
                    etLns12to23Remarks.setText(record.getLns12to23Remarks());

                    setRadioValue(rgMamIdentified, record.getMamIdentified());
                    setRadioValue(rgMamEnrolled, record.getMamEnrolled());
                    setRadioValue(rgMamCured, record.getMamCured());
                    setRadioValue(rgMamNonCured, record.getMamNonCured());
                    setRadioValue(rgMamDefaulted, record.getMamDefaulted());
                    setRadioValue(rgMamDied, record.getMamDied());

                    setRadioValue(rgSamIdentified, record.getSamIdentified());
                    setRadioValue(rgSamAdmitted, record.getSamAdmitted());
                    setRadioValue(rgSamCured, record.getSamCured());
                    setRadioValue(rgSamNonCured, record.getSamNonCured());
                    setRadioValue(rgSamDefaulted, record.getSamDefaulted());
                    setRadioValue(rgSamDied, record.getSamDied());

                    etRemarks.setText(record.getRemarks());
                });
            }
        });
    }

    private void saveOrUpdateRecord() {
        if (TextUtils.isEmpty(getText(etChildName))) {
            tilChildName.setError("Child Name is required");
            return;
        }
        tilChildName.setError(null);

        if (!validateDateFields()) {
            Toast.makeText(getContext(), "Please check the highlighted date", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            ChildNutritionRecord record = new ChildNutritionRecord();
            if (isEditMode) {
                record.setId(editingRecordId);
            }

            String PREFS_NAME = "AppPrefs";
            SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            record.userId = userId;
            record.setProfileId(selectedProfileId);
            record.setDateRegistration(getText(etDateRegistration));
            record.setFamilySerialNumber(getText(etFamilySerialNumber));
            record.setChildName(getText(etChildName));
            record.setDateOfBirth(getText(etDateOfBirth));
            record.setAgeMonths(getText(etAge));
            record.setMotherName(getText(etMotherName));
            record.setAddress(getText(etAddress));

            int sexId = rgSex.getCheckedRadioButtonId();
            if (sexId == R.id.rbMale) record.setSex("M");
            else if (sexId == R.id.rbFemale) record.setSex("F");

            record.setLengthAtBirth(getText(etLengthAtBirth));
            record.setWeightAtBirth(getText(etWeightAtBirth));
            record.setBirthWeightStatus(getBirthWeightCode(getText(etBirthWeightStatus)));
            record.setBreastfeedingDate(getText(etBreastfeedingDate));
            record.setPlaceOfDelivery(getText(etPlaceOfDelivery));

            record.setIron1Month(getText(etIron1Month));
            record.setIron2Months(getText(etIron2Months));
            record.setIron3Months(getText(etIron3Months));
            record.setIronCompleted(getRadioValue(rgIronCompleted));
            record.setIronCompletedDate(getText(etIronCompletedDate));

            record.setVitaA6to11(getText(etVitaA6to11));
            record.setVitaA200Y1D1(getText(etVitaA200Y1D1));
            record.setVitaA200Y1D2(getText(etVitaA200Y1D2));
            record.setVitaA200Y2D1(getText(etVitaA200Y2D1));
            record.setVitaA200Y2D2(getText(etVitaA200Y2D2));
            record.setVitaA200Y3D1(getText(etVitaA200Y3D1));
            record.setVitaA200Y3D2(getText(etVitaA200Y3D2));
            record.setVitaA200Y4D1(getText(etVitaA200Y4D1));
            record.setVitaA200Y4D2(getText(etVitaA200Y4D2));

            record.setMnp6to11Provided(getText(etMnp6to11Provided));
            record.setMnp6to11Completed(getText(etMnp6to11Completed));
            record.setMnp6to11Remarks(getText(etMnp6to11Remarks));
            record.setMnp12to23Provided(getText(etMnp12to23Provided));
            record.setMnp12to23Completed(getText(etMnp12to23Completed));
            record.setMnp12to23Remarks(getText(etMnp12to23Remarks));

            record.setLns6to11Provided(getText(etLns6to11Provided));
            record.setLns6to11Completed(getText(etLns6to11Completed));
            record.setLns6to11Remarks(getText(etLns6to11Remarks));
            record.setLns12to23Provided(getText(etLns12to23Provided));
            record.setLns12to23Completed(getText(etLns12to23Completed));
            record.setLns12to23Remarks(getText(etLns12to23Remarks));

            record.setMamIdentified(getRadioValue(rgMamIdentified));
            record.setMamEnrolled(getRadioValue(rgMamEnrolled));
            record.setMamCured(getRadioValue(rgMamCured));
            record.setMamNonCured(getRadioValue(rgMamNonCured));
            record.setMamDefaulted(getRadioValue(rgMamDefaulted));
            record.setMamDied(getRadioValue(rgMamDied));

            record.setSamIdentified(getRadioValue(rgSamIdentified));
            record.setSamAdmitted(getRadioValue(rgSamAdmitted));
            record.setSamCured(getRadioValue(rgSamCured));
            record.setSamNonCured(getRadioValue(rgSamNonCured));
            record.setSamDefaulted(getRadioValue(rgSamDefaulted));
            record.setSamDied(getRadioValue(rgSamDied));

            record.setRemarks(getText(etRemarks));

            if (database != null) {
                if (isEditMode) {
                    database.childNutritionDao().update(record);
                } else {
                    record.setNewInsert(true);
                    database.childNutritionDao().insert(record);
                }

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), isEditMode ? "Record updated successfully!" : "Record saved successfully!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            }
        });
    }

    private void clearAllFields() {
        clearField(etDateRegistration, tilDateRegistration);
        clearField(etFamilySerialNumber, tilFamilySerialNumber);
        clearField(etChildName, tilChildName);
        clearField(etDateOfBirth, tilDateOfBirth);
        clearField(etAge, tilAge);
        clearField(etMotherName, tilMotherName);
        clearField(etAddress, tilAddress);
        rgSex.clearCheck();

        clearField(etLengthAtBirth, tilLengthAtBirth);
        clearField(etWeightAtBirth, tilWeightAtBirth);
        clearField(etBirthWeightStatus, tilBirthWeightStatus);
        clearField(etBreastfeedingDate, tilBreastfeedingDate);
        clearField(etPlaceOfDelivery, tilPlaceOfDelivery);

        clearField(etIron1Month, tilIron1Month);
        clearField(etIron2Months, tilIron2Months);
        clearField(etIron3Months, tilIron3Months);
        rgIronCompleted.clearCheck();
        clearField(etIronCompletedDate, tilIronCompletedDate);

        clearField(etVitaA6to11, tilVitaA6to11);
        clearField(etVitaA200Y1D1, tilVitaA200Y1D1);
        clearField(etVitaA200Y1D2, tilVitaA200Y1D2);
        clearField(etVitaA200Y2D1, tilVitaA200Y2D1);
        clearField(etVitaA200Y2D2, tilVitaA200Y2D2);
        clearField(etVitaA200Y3D1, tilVitaA200Y3D1);
        clearField(etVitaA200Y3D2, tilVitaA200Y3D2);
        clearField(etVitaA200Y4D1, tilVitaA200Y4D1);
        clearField(etVitaA200Y4D2, tilVitaA200Y4D2);

        clearField(etMnp6to11Provided, tilMnp6to11Provided);
        clearField(etMnp6to11Completed, tilMnp6to11Completed);
        clearField(etMnp6to11Remarks, tilMnp6to11Remarks);
        clearField(etMnp12to23Provided, tilMnp12to23Provided);
        clearField(etMnp12to23Completed, tilMnp12to23Completed);
        clearField(etMnp12to23Remarks, tilMnp12to23Remarks);

        clearField(etLns6to11Provided, tilLns6to11Provided);
        clearField(etLns6to11Completed, tilLns6to11Completed);
        clearField(etLns6to11Remarks, tilLns6to11Remarks);
        clearField(etLns12to23Provided, tilLns12to23Provided);
        clearField(etLns12to23Completed, tilLns12to23Completed);
        clearField(etLns12to23Remarks, tilLns12to23Remarks);

        rgMamIdentified.clearCheck();
        rgMamEnrolled.clearCheck();
        rgMamCured.clearCheck();
        rgMamNonCured.clearCheck();
        rgMamDefaulted.clearCheck();
        rgMamDied.clearCheck();

        rgSamIdentified.clearCheck();
        rgSamAdmitted.clearCheck();
        rgSamCured.clearCheck();
        rgSamNonCured.clearCheck();
        rgSamDefaulted.clearCheck();
        rgSamDied.clearCheck();

        clearField(etRemarks, tilRemarks);
    }

    private String getText(EditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void clearField(EditText field, TextInputLayout layout) {
        field.setText("");
        if (layout != null) layout.setError(null);
    }

    private int getRadioValue(RadioGroup group) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == -1) return -1;
        RadioButton rb = group.findViewById(checkedId);
        if (rb == null) return -1;
        String label = rb.getText().toString();
        try { return Integer.parseInt(String.valueOf(label.charAt(0))); }
        catch (NumberFormatException e) { return -1; }
    }

    private void setRadioValue(RadioGroup group, int value) {
        if (value == -1) {
            group.clearCheck();
            return;
        }
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                String label = rb.getText().toString();
                if (!label.isEmpty() && label.startsWith(String.valueOf(value))) {
                    rb.setChecked(true);
                    return;
                }
            }
        }
    }

    private String getBirthWeightCode(String displayText) {
        if (displayText == null || displayText.isEmpty()) return "";
        return String.valueOf(displayText.charAt(0));
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