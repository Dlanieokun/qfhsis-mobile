package com.android.hfsis.maternal_care_record;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import com.android.hfsis.model.maternal_care_record.PostpartumEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PostpartumFragment extends Fragment {

    private int maternalRecordId = -1;
    private int profileId = -1;
    private String patientName = "";

    private TextView tvPostpartumPatientHeader;
    private EditText etVisit24h, etVisit1w, etVisit2_4w, etVisit4_6w;
    private EditText etBpSys24h, etBpDias24h, etBpSys1w, etBpDias1w;
    private EditText etBpSys2_4w, etBpDias2_4w, etBpSys4_6w, etBpDias4_6w;

    // Risk & Referral Assessment Fields
    private RadioGroup rgHighBpGeneral, rgDangerSignsGeneral, rgReferredGeneral;
    private LinearLayout layoutDangerSignsChecklist;
    private CheckBox cbDsBleeding, cbDsVision, cbDsAbdominal, cbDsFever, cbDsBreathing;
    private EditText etReferralDateGeneral;

    // Classification Dropdown System
    private Spinner spinnerPostpartumClassification;
    private EditText etClassificationDate;

    // Supplementation Fields mapped from structural columns
    private EditText etIronTabs1st, etIronDate1st;
    private EditText etIronTabs2nd, etIronDate2nd;
    private EditText etIronTabs3rd, etIronDate3rd;

    private RadioGroup rgCompletedIfa, rgCompletedVitA;
    private EditText etIfaCompletionDate, etVitACompletionDate;
    private EditText etBreastfeedingDate;

    private MaterialButton btnSubmit;

    public PostpartumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            maternalRecordId = getArguments().getInt("maternal_record_id", -1);
            profileId = getArguments().getInt("profile_id", -1);
            patientName = getArguments().getString("patient_name", "Unknown Patient");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_postpartum, container, false);
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

        // Bind Base Visit Schedule Views
        tvPostpartumPatientHeader = view.findViewById(R.id.tvPostpartumPatientHeader);
        etVisit24h = view.findViewById(R.id.etPostpartumVisit24h);
        etVisit1w = view.findViewById(R.id.etPostpartumVisit1w);
        etVisit2_4w = view.findViewById(R.id.etPostpartumVisit2_4w);
        etVisit4_6w = view.findViewById(R.id.etPostpartumVisit4_6w);

        etBpSys24h = view.findViewById(R.id.etPostpartumBpSys24h);
        etBpDias24h = view.findViewById(R.id.etPostpartumBpDias24h);
        etBpSys1w = view.findViewById(R.id.etPostpartumBpSys1w);
        etBpDias1w = view.findViewById(R.id.etPostpartumBpDias1w);
        etBpSys2_4w = view.findViewById(R.id.etPostpartumBpSys2_4w);
        etBpDias2_4w = view.findViewById(R.id.etPostpartumBpDias2_4w);
        etBpSys4_6w = view.findViewById(R.id.etPostpartumBpSys4_6w);
        etBpDias4_6w = view.findViewById(R.id.etPostpartumBpDias4_6w);

        // Bind Assessment Layout Components
        rgHighBpGeneral = view.findViewById(R.id.rgHighBpGeneral);
        rgDangerSignsGeneral = view.findViewById(R.id.rgDangerSignsGeneral);
        rgReferredGeneral = view.findViewById(R.id.rgReferredGeneral);
        layoutDangerSignsChecklist = view.findViewById(R.id.layoutDangerSignsChecklist);
        cbDsBleeding = view.findViewById(R.id.cbDsBleeding);
        cbDsVision = view.findViewById(R.id.cbDsVision);
        cbDsAbdominal = view.findViewById(R.id.cbDsAbdominal);
        cbDsFever = view.findViewById(R.id.cbDsFever);
        cbDsBreathing = view.findViewById(R.id.cbDsBreathing);
        etReferralDateGeneral = view.findViewById(R.id.etReferralDateGeneral);

        // Bind Classification Views
        spinnerPostpartumClassification = view.findViewById(R.id.spinnerPostpartumClassification);
        etClassificationDate = view.findViewById(R.id.etClassificationDate);

        // Bind Supplementation (# and d:) Views
        etIronTabs1st = view.findViewById(R.id.etIronTabs1st);
        etIronDate1st = view.findViewById(R.id.etIronDate1st);
        etIronTabs2nd = view.findViewById(R.id.etIronTabs2nd);
        etIronDate2nd = view.findViewById(R.id.etIronDate2nd);
        etIronTabs3rd = view.findViewById(R.id.etIronTabs3rd);
        etIronDate3rd = view.findViewById(R.id.etIronDate3rd);

        rgCompletedIfa = view.findViewById(R.id.rgCompletedIfa);
        etIfaCompletionDate = view.findViewById(R.id.etIfaCompletionDate);
        rgCompletedVitA = view.findViewById(R.id.rgCompletedVitA);
        etVitACompletionDate = view.findViewById(R.id.etVitACompletionDate);

        etBreastfeedingDate = view.findViewById(R.id.etPostpartumBreastfeedingDate);
        btnSubmit = view.findViewById(R.id.btnSubmitPostpartum);

        if (patientName != null && !patientName.isEmpty()) {
            tvPostpartumPatientHeader.setText("Patient: " + patientName + " (ID: " + maternalRecordId + ")");
        }

        setupDropdownData();
        setupToggleListeners();
        setupDatePickerHooks();
        loadData();

        btnSubmit.setOnClickListener(v -> savePostpartumForm());
    }
    private void loadData() {
        if (maternalRecordId == -1) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            // Fetch the entity from the database
            PostpartumEntity entity = DatabaseHelper.getInstance(getContext())
                    .postpartumDao().getByMaternalId(maternalRecordId);

            if (entity != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Populate EditTexts
                    etVisit24h.setText(entity.visit24hDate);
                    etVisit1w.setText(entity.visit1wDate);
                    etVisit2_4w.setText(entity.visit2_4wDate);
                    etVisit4_6w.setText(entity.visit4_6wDate);


                    // Populate BP fields (example)
                    etBpSys24h.setText(entity.bpSys24h);
                    etBpDias24h.setText(entity.bpDias24h);
                    etBpSys1w.setText(entity.bpSys1w);
                    etBpDias1w.setText(entity.bpDias1w);
                    etBpSys2_4w.setText(entity.bpSys2_4w);
                    etBpDias2_4w.setText(entity.bpDias2_4w);
                    etBpSys4_6w.setText(entity.bpSys4_6w);
                    etBpDias4_6w.setText(entity.bpDias4_6w);


                    // Populate Checkboxes for Danger Signs
                    cbDsBleeding.setChecked(entity.dsBleeding);
                    cbDsVision.setChecked(entity.dsVision);
                    cbDsAbdominal.setChecked(entity.dsAbdominal);
                    cbDsFever.setChecked(entity.dsFever);
                    cbDsBreathing.setChecked(entity.dsBreathing);
                    // ... (set other checkboxes)
                    // ... (set other EditTexts)
                    etClassificationDate.setText(entity.classificationDate);
                    if (entity.PostpartumClassification != null) {
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerPostpartumClassification.getAdapter();
                        int spinnerPosition = adapter.getPosition(entity.PostpartumClassification);
                        if (spinnerPosition >= 0) {
                            spinnerPostpartumClassification.setSelection(spinnerPosition);
                        }
                    }
                    // Populate RadioGroups
                    if ("Yes".equals(entity.highBpGeneral)) {
                        rgHighBpGeneral.check(R.id.rbHighBpYes);
                    } else {
                        rgHighBpGeneral.check(R.id.rbHighBpNo);
                    }
                    if ("Yes".equals(entity.dangerSignsGeneral)) {
                        rgDangerSignsGeneral.check(R.id.rbDangerSignsYes);
                    } else {
                        rgDangerSignsGeneral.check(R.id.rbDangerSignsNo);
                    }

                    etBreastfeedingDate.setText(entity.breastfeedingInitiationDate);
                    cbDsBleeding.setChecked(entity.dsBleeding);
                    cbDsVision.setChecked(entity.dsVision);
                    cbDsAbdominal.setChecked(entity.dsAbdominal);
                    cbDsFever.setChecked(entity.dsFever);
                    cbDsBreathing.setChecked(entity.dsBreathing);
                    if ("Yes".equals(entity.referredGeneral)) {
                        rgReferredGeneral.check(R.id.rbReferredYes);
                    } else {
                        rgReferredGeneral.check(R.id.rbReferredNo);
                    }
                    etReferralDateGeneral.setText(entity.referralDateGeneral);
                    if ("Yes".equals(entity.completedIfa)) {
                        rgCompletedIfa.check(R.id.rbIfaYes);
                    } else {
                        rgCompletedIfa.check(R.id.rbIfaNo);
                    }
                    if ("Yes".equals(entity.completedVitA)) {
                        rgCompletedVitA.check(R.id.rbVitAYes);
                    } else {
                        rgCompletedVitA.check(R.id.rbVitANo);
                    }

                    etIfaCompletionDate.setText(entity.ifaCompletionDate);
                    etVitACompletionDate.setText(entity.vitACompletionDate);
                    etIronTabs1st.setText(entity.ironTabs1st);
                    etIronDate1st.setText(entity.ironDate1st);
                    etIronTabs2nd.setText(entity.ironTabs2nd);
                    etIronDate2nd.setText(entity.ironDate2nd);
                    etIronTabs3rd.setText(entity.ironTabs3rd);
                    etIronDate3rd.setText(entity.ironDate3rd);


                });
            }
        });
    }

    private void setupDropdownData() {
        String[] options = {
                "A - Resident",
                "B - Trans in",
                "C - Trans Out before completing 4PNC"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPostpartumClassification.setAdapter(adapter);
    }

    private void setupToggleListeners() {
        rgDangerSignsGeneral.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbDangerSignsYes) {
                layoutDangerSignsChecklist.setVisibility(View.VISIBLE);
            } else {
                layoutDangerSignsChecklist.setVisibility(View.GONE);
                clearDangerChecklist();
            }
        });

        rgReferredGeneral.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbReferredYes) {
                setFieldVisibility(etReferralDateGeneral, View.VISIBLE);
            } else {
                setFieldVisibility(etReferralDateGeneral, View.GONE);
                etReferralDateGeneral.setText("");
            }
        });

        // Dynamic toggle visibility logic for IFA completion date field
        rgCompletedIfa.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbIfaYes) {
                setFieldVisibility(etIfaCompletionDate, View.VISIBLE);
            } else {
                setFieldVisibility(etIfaCompletionDate, View.GONE);
                etIfaCompletionDate.setText("");
            }
        });

        // Dynamic toggle visibility logic for Vitamin A completion date field
        rgCompletedVitA.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbVitAYes) {
                setFieldVisibility(etVitACompletionDate, View.VISIBLE);
            } else {
                setFieldVisibility(etVitACompletionDate, View.GONE);
                etVitACompletionDate.setText("");
            }
        });
    }

    private void clearDangerChecklist() {
        cbDsBleeding.setChecked(false);
        cbDsVision.setChecked(false);
        cbDsAbdominal.setChecked(false);
        cbDsFever.setChecked(false);
        cbDsBreathing.setChecked(false);
    }

    private void setupDatePickerHooks() {
        bindDateField(etVisit24h);
        bindDateField(etVisit1w);
        bindDateField(etVisit2_4w);
        bindDateField(etVisit4_6w);

        bindDateField(etClassificationDate);
        bindDateField(etReferralDateGeneral);

        bindDateField(etIronDate1st);
        bindDateField(etIronDate2nd);
        bindDateField(etIronDate3rd);
        bindDateField(etIfaCompletionDate);
        bindDateField(etVitACompletionDate);

        bindDateField(etBreastfeedingDate);
    }

    private void bindDateField(EditText field) {
        // Formats keyboard inputs while typing manually
        field.addTextChangedListener(new DateFormattingWatcher(field));

        // Opens calendar date picker only when clicking the end icon
        TextInputLayout til = findParentTextInputLayout(field);
        if (til != null) {
            til.setEndIconOnClickListener(v -> showDatePicker(field));
        }
    }

    private TextInputLayout findParentTextInputLayout(View child) {
        ViewParent parent = child.getParent();
        while (parent != null) {
            if (parent instanceof TextInputLayout) return (TextInputLayout) parent;
            parent = parent.getParent();
        }
        return null;
    }

    // The EditText sits inside a TextInputLayout, so show/hide the whole box
    private void setFieldVisibility(EditText field, int visibility) {
        TextInputLayout til = findParentTextInputLayout(field);
        if (til != null) {
            til.setVisibility(visibility);
        } else {
            field.setVisibility(visibility);
        }
    }

    private void showDatePicker(EditText targetField) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            String formattedDate = String.format(Locale.US, "%02d-%02d-%04d", month + 1, day, year);
            targetField.setText(formattedDate);
            targetField.setError(null);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void savePostpartumForm() {
        String date24hStr = etVisit24h.getText().toString().trim();
        if (date24hStr.isEmpty()) {
            etVisit24h.setError("Initial 24-hour visit date is required");
            return;
        }

        if (rgDangerSignsGeneral.getCheckedRadioButtonId() == R.id.rbDangerSignsYes) {
            if (!(cbDsBleeding.isChecked() || cbDsVision.isChecked() || cbDsAbdominal.isChecked() || cbDsFever.isChecked() || cbDsBreathing.isChecked())) {
                Toast.makeText(getContext(), "Please choose at least 1 identified Danger Sign.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (rgReferredGeneral.getCheckedRadioButtonId() == R.id.rbReferredYes) {
            if (etReferralDateGeneral.getText().toString().trim().isEmpty()) {
                etReferralDateGeneral.setError("Referral date is required");
                return;
            }
        }

        if (rgCompletedIfa.getCheckedRadioButtonId() == R.id.rbIfaYes && etIfaCompletionDate.getText().toString().trim().isEmpty()) {
            etIfaCompletionDate.setError("IFA completion date is required");
            return;
        }

        if (rgCompletedVitA.getCheckedRadioButtonId() == R.id.rbVitAYes && etVitACompletionDate.getText().toString().trim().isEmpty()) {
            etVitACompletionDate.setError("Vitamin A completion date is required");
            return;
        }

        // 1. Map fields from your EditTexts/RadioGroups to entity
        PostpartumEntity entity = new PostpartumEntity();
        entity.maternalRecordId = this.maternalRecordId;

        // Example: Map BP fields
        entity.visit24hDate = etVisit24h.getText().toString();
        entity.bpSys24h = etBpSys24h.getText().toString();
        entity.bpDias24h = etBpDias24h.getText().toString();
        entity.visit1wDate = etVisit1w.getText().toString();
        entity.bpSys1w = etBpSys1w.getText().toString();
        entity.bpDias1w = etBpDias1w.getText().toString();
        entity.visit2_4wDate = etVisit2_4w.getText().toString();
        entity.bpSys2_4w = etBpSys2_4w.getText().toString();
        entity.bpDias2_4w = etBpDias2_4w.getText().toString();
        entity.visit4_6wDate = etVisit4_6w.getText().toString();
        entity.bpSys4_6w = etBpSys4_6w.getText().toString();
        entity.bpDias4_6w = etBpDias4_6w.getText().toString();
        entity.highBpGeneral = rgHighBpGeneral.getCheckedRadioButtonId() == R.id.rbHighBpYes ? "Yes" : "No";
        entity.dangerSignsGeneral = rgDangerSignsGeneral.getCheckedRadioButtonId() == R.id.rbDangerSignsYes ? "Yes" : "No";
        entity.referredGeneral = rgReferredGeneral.getCheckedRadioButtonId() == R.id.rbReferredYes ? "Yes" : "No";
        entity.dsBleeding = cbDsBleeding.isChecked();
        entity.dsVision = cbDsVision.isChecked();
        entity.dsAbdominal = cbDsAbdominal.isChecked();
        entity.dsFever = cbDsFever.isChecked();
        entity.dsBreathing = cbDsBreathing.isChecked();
        entity.referralDateGeneral = etReferralDateGeneral.getText().toString();
        entity.completedIfa = rgCompletedIfa.getCheckedRadioButtonId() == R.id.rbIfaYes ? "Yes" : "No";
        entity.ifaCompletionDate = etIfaCompletionDate.getText().toString();
        entity.completedVitA = rgCompletedVitA.getCheckedRadioButtonId() == R.id.rbVitAYes ? "Yes" : "No";
        entity.vitACompletionDate = etVitACompletionDate.getText().toString();
        entity.breastfeedingInitiationDate = etBreastfeedingDate.getText().toString();
        entity.classificationDate = etClassificationDate.getText().toString();
        entity.PostpartumClassification = spinnerPostpartumClassification.getSelectedItem().toString();
        entity.ironTabs1st = etIronTabs1st.getText().toString();
        entity.ironDate1st = etIronDate1st.getText().toString();
        entity.ironTabs2nd = etIronTabs2nd.getText().toString();
        entity.ironDate2nd = etIronDate2nd.getText().toString();
        entity.ironTabs3rd = etIronTabs3rd.getText().toString();
        entity.ironDate3rd = etIronDate3rd.getText().toString();



        // ... map all other fields ...

        // 2. Perform background save
        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext());
            PostpartumEntity existing = db.postpartumDao().getByMaternalId(maternalRecordId);

            if (existing == null) {
                entity.newInsert = true;
                db.postpartumDao().insert(entity);
            } else {
                entity.id = existing.id; // Ensure ID matches to update correctly
                db.postpartumDao().update(entity);
            }

            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Postpartum record saved successfully.", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
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