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
import com.android.hfsis.model.maternal_care_record.PrenatalImmunizationEntity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PrenatalImmunizationFragment extends Fragment {

    private int maternalRecordId = -1;
    private int profileId = -1;
    private String patientName = "";

    private DatabaseHelper db;

    private TextView tvImmPatientHeader;
    private TextInputEditText etTd1Date, etTd2Date, etTd3Date, etTd4Date, etTd5Date;
    private Button btnSaveImmunizationForm;

    public PrenatalImmunizationFragment() {
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
        return inflater.inflate(R.layout.fragment_prenatal_immunization, container, false);
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

        db = DatabaseHelper.getDatabase(requireContext());

        tvImmPatientHeader = view.findViewById(R.id.tvImmPatientHeader);
        if (patientName != null && !patientName.isEmpty()) {
            tvImmPatientHeader.setText("Patient: " + patientName + " (Maternal ID: " + maternalRecordId + ")");
        }

        etTd1Date = view.findViewById(R.id.etTd1Date);
        etTd2Date = view.findViewById(R.id.etTd2Date);
        etTd3Date = view.findViewById(R.id.etTd3Date);
        etTd4Date = view.findViewById(R.id.etTd4Date);
        etTd5Date = view.findViewById(R.id.etTd5Date);
        btnSaveImmunizationForm = view.findViewById(R.id.btnSaveImmunizationForm);

        setupAllDatePickerDialogs();
        loadExistingImmunizationData();

        btnSaveImmunizationForm.setOnClickListener(v -> executeSaveOrUpdatePipeline());
    }

    private void setupAllDatePickerDialogs() {
        bindDatePickerPopup(etTd1Date);
        bindDatePickerPopup(etTd2Date);
        bindDatePickerPopup(etTd3Date);
        bindDatePickerPopup(etTd4Date);
        bindDatePickerPopup(etTd5Date);
    }

    private void bindDatePickerPopup(TextInputEditText inputField) {
        if (inputField == null) return;

        // Formats keyboard inputs while typing manually
        inputField.addTextChangedListener(new DateFormattingWatcher(inputField));

        // Opens calendar date picker only when clicking the end icon
        TextInputLayout til = findParentTextInputLayout(inputField);
        if (til != null) {
            til.setEndIconOnClickListener(v -> showDatePickerDialog(inputField));
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

    private void showDatePickerDialog(TextInputEditText inputField) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String formattedSelectionDate = String.format(Locale.US, "%02d-%02d-%04d", month + 1, dayOfMonth, year);
            inputField.setText(formattedSelectionDate);
            inputField.setError(null);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void loadExistingImmunizationData() {
        if (maternalRecordId == -1) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            PrenatalImmunizationEntity existing = db.prenatalImmunizationDao().getRecordByMaternalId(maternalRecordId);
            if (existing != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    btnSaveImmunizationForm.setText("Update Immunization Log");

                    etTd1Date.setText(existing.td1Date);
                    etTd2Date.setText(existing.td2Date);
                    etTd3Date.setText(existing.td3Date);
                    etTd4Date.setText(existing.td4Date);
                    etTd5Date.setText(existing.td5Date);
                });
            }
        });
    }

    private void executeSaveOrUpdatePipeline() {
        if (maternalRecordId == -1) {
            Toast.makeText(getContext(), "Error: Missing Maternal ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read UI values on the main thread
        final String td1Val = etTd1Date.getText().toString().trim();
        final String td2Val = etTd2Date.getText().toString().trim();
        final String td3Val = etTd3Date.getText().toString().trim();
        final String td4Val = etTd4Date.getText().toString().trim();
        final String td5Val = etTd5Date.getText().toString().trim();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Decide insert vs update from the DB itself
                PrenatalImmunizationEntity existing =
                        db.prenatalImmunizationDao().getRecordByMaternalId(maternalRecordId);
                final boolean isUpdate = existing != null;

                PrenatalImmunizationEntity record = new PrenatalImmunizationEntity();
                record.maternalRecordId = maternalRecordId;
                record.td1Date = td1Val;
                record.td2Date = td2Val;
                record.td3Date = td3Val;
                record.td4Date = td4Val;
                record.td5Date = td5Val;
                record.isSynced = false;
                record.updatedAt = System.currentTimeMillis();

                if (isUpdate) {
                    record.id = existing.id;
                    record.newInsert = existing.newInsert; // keep "never uploaded" state
                    db.prenatalImmunizationDao().updateImmunizationRecord(record);
                } else {
                    record.newInsert = true;
                    db.prenatalImmunizationDao().insertImmunizationRecord(record);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String msg = isUpdate
                                ? "Immunization record updated successfully!"
                                : "Immunization record saved successfully!";
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