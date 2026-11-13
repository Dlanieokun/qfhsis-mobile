package com.android.hfsis.maternal_care_record;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.android.hfsis.model.maternal_care_record.PrenatalSupplementationEntity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PrenatalSupplementationFragment extends Fragment {

    private int maternalRecordId = -1;
    private int profileId = -1;
    private String patientName = "";

    private DatabaseHelper db;

    private TextView tvSuppPatientHeader;
    private Button btnSaveSupplementationForm;

    // Checkboxes
    private CheckBox cbReceivedDeworming, cbCompletedIfa, cbCompletedMm, cbCompletedCc;

    // Dates
    private TextInputEditText etDewormingDate, etIfaCompletedDate, etMmCompletedDate, etCcCompletedDate;

    // IFA inputs
    private TextInputEditText etIfaV1Num, etIfaV1Date, etIfaV2Num, etIfaV2Date, etIfaV3Num, etIfaV3Date;
    private TextInputEditText etIfaV4Num, etIfaV4Date, etIfaV5Num, etIfaV5Date, etIfaV6Num, etIfaV6Date;

    // MM inputs
    private TextInputEditText etMmV1Num, etMmV1Date, etMmV2Num, etMmV2Date, etMmV3Num, etMmV3Date;
    private TextInputEditText etMmV4Num, etMmV4Date, etMmV5Num, etMmV5Date, etMmV6Num, etMmV6Date;

    // Calcium Carbonate inputs
    private TextInputEditText etCcV2Num, etCcV2Date, etCcV3Num, etCcV3Date, etCcV4Num, etCcV4Date;

    public PrenatalSupplementationFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prenatal_supplementation, container, false);
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

        db = DatabaseHelper.getInstance(requireContext());

        tvSuppPatientHeader = view.findViewById(R.id.tvSuppPatientHeader);
        if (patientName != null && !patientName.isEmpty()) {
            tvSuppPatientHeader.setText("Patient: " + patientName + " (Maternal ID: " + maternalRecordId + ")");
        }

        initializeFormFields(view);
        setupDatePickers();
        loadExistingSupplementationData();

        btnSaveSupplementationForm.setOnClickListener(v -> executeSaveOrUpdatePipeline());
    }

    private void initializeFormFields(View view) {
        // Checkboxes
        cbReceivedDeworming = view.findViewById(R.id.cbReceivedDeworming);
        cbCompletedIfa = view.findViewById(R.id.cbCompletedIfa);
        cbCompletedMm = view.findViewById(R.id.cbCompletedMm);
        cbCompletedCc = view.findViewById(R.id.cbCompletedCc);

        // Master dates
        etDewormingDate = view.findViewById(R.id.etDewormingDate);
        etIfaCompletedDate = view.findViewById(R.id.etIfaCompletedDate);
        etMmCompletedDate = view.findViewById(R.id.etMmCompletedDate);
        etCcCompletedDate = view.findViewById(R.id.etCcCompletedDate);

        // IFA
        etIfaV1Num = view.findViewById(R.id.etIfaV1Num);
        etIfaV1Date = view.findViewById(R.id.etIfaV1Date);
        etIfaV2Num = view.findViewById(R.id.etIfaV2Num);
        etIfaV2Date = view.findViewById(R.id.etIfaV2Date);
        etIfaV3Num = view.findViewById(R.id.etIfaV3Num);
        etIfaV3Date = view.findViewById(R.id.etIfaV3Date);
        etIfaV4Num = view.findViewById(R.id.etIfaV4Num);
        etIfaV4Date = view.findViewById(R.id.etIfaV4Date);
        etIfaV5Num = view.findViewById(R.id.etIfaV5Num);
        etIfaV5Date = view.findViewById(R.id.etIfaV5Date);
        etIfaV6Num = view.findViewById(R.id.etIfaV6Num);
        etIfaV6Date = view.findViewById(R.id.etIfaV6Date);

        // MM
        etMmV1Num = view.findViewById(R.id.etMmV1Num);
        etMmV1Date = view.findViewById(R.id.etMmV1Date);
        etMmV2Num = view.findViewById(R.id.etMmV2Num);
        etMmV2Date = view.findViewById(R.id.etMmV2Date);
        etMmV3Num = view.findViewById(R.id.etMmV3Num);
        etMmV3Date = view.findViewById(R.id.etMmV3Date);
        etMmV4Num = view.findViewById(R.id.etMmV4Num);
        etMmV4Date = view.findViewById(R.id.etMmV4Date);
        etMmV5Num = view.findViewById(R.id.etMmV5Num);
        etMmV5Date = view.findViewById(R.id.etMmV5Date);
        etMmV6Num = view.findViewById(R.id.etMmV6Num);
        etMmV6Date = view.findViewById(R.id.etMmV6Date);

        // CC
        etCcV2Num = view.findViewById(R.id.etCcV2Num);
        etCcV2Date = view.findViewById(R.id.etCcV2Date);
        etCcV3Num = view.findViewById(R.id.etCcV3Num);
        etCcV3Date = view.findViewById(R.id.etCcV3Date);
        etCcV4Num = view.findViewById(R.id.etCcV4Num);
        etCcV4Date = view.findViewById(R.id.etCcV4Date);

        btnSaveSupplementationForm = view.findViewById(R.id.btnSaveSupplementationForm);
    }

    private void setupDatePickers() {
        bindDatePicker(etDewormingDate);
        bindDatePicker(etIfaCompletedDate);
        bindDatePicker(etMmCompletedDate);
        bindDatePicker(etCcCompletedDate);

        bindDatePicker(etIfaV1Date); bindDatePicker(etIfaV2Date); bindDatePicker(etIfaV3Date);
        bindDatePicker(etIfaV4Date); bindDatePicker(etIfaV5Date); bindDatePicker(etIfaV6Date);

        bindDatePicker(etMmV1Date); bindDatePicker(etMmV2Date); bindDatePicker(etMmV3Date);
        bindDatePicker(etMmV4Date); bindDatePicker(etMmV5Date); bindDatePicker(etMmV6Date);

        bindDatePicker(etCcV2Date); bindDatePicker(etCcV3Date); bindDatePicker(etCcV4Date);
    }

    private void bindDatePicker(TextInputEditText editText) {
        if (editText == null) return;

        // Formats keyboard inputs while typing manually
        editText.addTextChangedListener(new DateFormattingWatcher(editText));

        // Opens calendar date picker only when clicking the end icon
        TextInputLayout til = findParentTextInputLayout(editText);
        if (til != null) {
            til.setEndIconOnClickListener(v -> showDatePickerDialog(editText));
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

    private void showDatePickerDialog(TextInputEditText editText) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.US, "%02d-%02d-%04d", month + 1, dayOfMonth, year);
            editText.setText(selectedDate);
            editText.setError(null);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void loadExistingSupplementationData() {
        if (maternalRecordId == -1) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            PrenatalSupplementationEntity record = db.prenatalSupplementationDao().getRecordByMaternalId(maternalRecordId);
            if (record != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    btnSaveSupplementationForm.setText("Update Supplementation Log");

                    cbReceivedDeworming.setChecked(record.receivedDeworming);
                    cbCompletedIfa.setChecked(record.completedIfa);
                    cbCompletedMm.setChecked(record.completedMm);
                    cbCompletedCc.setChecked(record.completedCc);

                    etDewormingDate.setText(record.dewormingDate);
                    etIfaCompletedDate.setText(record.ifaCompletedDate);
                    etMmCompletedDate.setText(record.mmCompletedDate);
                    etCcCompletedDate.setText(record.ccCompletedDate);

                    etIfaV1Num.setText(record.ifaV1Num); etIfaV1Date.setText(record.ifaV1Date);
                    etIfaV2Num.setText(record.ifaV2Num); etIfaV2Date.setText(record.ifaV2Date);
                    etIfaV3Num.setText(record.ifaV3Num); etIfaV3Date.setText(record.ifaV3Date);
                    etIfaV4Num.setText(record.ifaV4Num); etIfaV4Date.setText(record.ifaV4Date);
                    etIfaV5Num.setText(record.ifaV5Num); etIfaV5Date.setText(record.ifaV5Date);
                    etIfaV6Num.setText(record.ifaV6Num); etIfaV6Date.setText(record.ifaV6Date);

                    etMmV1Num.setText(record.mmV1Num); etMmV1Date.setText(record.mmV1Date);
                    etMmV2Num.setText(record.mmV2Num); etMmV2Date.setText(record.mmV2Date);
                    etMmV3Num.setText(record.mmV3Num); etMmV3Date.setText(record.mmV3Date);
                    etMmV4Num.setText(record.mmV4Num); etMmV4Date.setText(record.mmV4Date);
                    etMmV5Num.setText(record.mmV5Num); etMmV5Date.setText(record.mmV5Date);
                    etMmV6Num.setText(record.mmV6Num); etMmV6Date.setText(record.mmV6Date);

                    etCcV2Num.setText(record.ccV2Num); etCcV2Date.setText(record.ccV2Date);
                    etCcV3Num.setText(record.ccV3Num); etCcV3Date.setText(record.ccV3Date);
                    etCcV4Num.setText(record.ccV4Num); etCcV4Date.setText(record.ccV4Date);
                });
            }
        });
    }

    private void executeSaveOrUpdatePipeline() {
        if (maternalRecordId == -1) {
            Toast.makeText(getContext(), "Error: Missing Maternal ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read all UI values on the main thread
        final PrenatalSupplementationEntity data = new PrenatalSupplementationEntity();
        data.maternalRecordId = maternalRecordId;

        data.receivedDeworming = cbReceivedDeworming.isChecked();
        data.completedIfa = cbCompletedIfa.isChecked();
        data.completedMm = cbCompletedMm.isChecked();
        data.completedCc = cbCompletedCc.isChecked();

        data.dewormingDate = etDewormingDate.getText().toString().trim();
        data.ifaCompletedDate = etIfaCompletedDate.getText().toString().trim();
        data.mmCompletedDate = etMmCompletedDate.getText().toString().trim();
        data.ccCompletedDate = etCcCompletedDate.getText().toString().trim();

        // IFA
        data.ifaV1Num = etIfaV1Num.getText().toString().trim(); data.ifaV1Date = etIfaV1Date.getText().toString().trim();
        data.ifaV2Num = etIfaV2Num.getText().toString().trim(); data.ifaV2Date = etIfaV2Date.getText().toString().trim();
        data.ifaV3Num = etIfaV3Num.getText().toString().trim(); data.ifaV3Date = etIfaV3Date.getText().toString().trim();
        data.ifaV4Num = etIfaV4Num.getText().toString().trim(); data.ifaV4Date = etIfaV4Date.getText().toString().trim();
        data.ifaV5Num = etIfaV5Num.getText().toString().trim(); data.ifaV5Date = etIfaV5Date.getText().toString().trim();
        data.ifaV6Num = etIfaV6Num.getText().toString().trim(); data.ifaV6Date = etIfaV6Date.getText().toString().trim();

        // MM
        data.mmV1Num = etMmV1Num.getText().toString().trim(); data.mmV1Date = etMmV1Date.getText().toString().trim();
        data.mmV2Num = etMmV2Num.getText().toString().trim(); data.mmV2Date = etMmV2Date.getText().toString().trim();
        data.mmV3Num = etMmV3Num.getText().toString().trim(); data.mmV3Date = etMmV3Date.getText().toString().trim();
        data.mmV4Num = etMmV4Num.getText().toString().trim(); data.mmV4Date = etMmV4Date.getText().toString().trim();
        data.mmV5Num = etMmV5Num.getText().toString().trim(); data.mmV5Date = etMmV5Date.getText().toString().trim();
        data.mmV6Num = etMmV6Num.getText().toString().trim(); data.mmV6Date = etMmV6Date.getText().toString().trim();

        // CC
        data.ccV2Num = etCcV2Num.getText().toString().trim(); data.ccV2Date = etCcV2Date.getText().toString().trim();
        data.ccV3Num = etCcV3Num.getText().toString().trim(); data.ccV3Date = etCcV3Date.getText().toString().trim();
        data.ccV4Num = etCcV4Num.getText().toString().trim(); data.ccV4Date = etCcV4Date.getText().toString().trim();

        data.isSynced = false;
        data.updatedAt = System.currentTimeMillis();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Decide insert vs update from the DB itself
                PrenatalSupplementationEntity existing =
                        db.prenatalSupplementationDao().getRecordByMaternalId(maternalRecordId);
                final boolean isUpdate = existing != null;

                if (isUpdate) {
                    data.id = existing.id;
                    data.newInsert = existing.newInsert; // keep "never uploaded" state
                    db.prenatalSupplementationDao().updateSupplementationRecord(data);
                } else {
                    data.newInsert = true;
                    db.prenatalSupplementationDao().insertSupplementationRecord(data);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String msg = isUpdate
                                ? "Supplementation log updated successfully!"
                                : "Supplementation log saved successfully!";
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
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