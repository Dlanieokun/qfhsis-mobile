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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ScrollView;
import android.widget.EditText;
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
import com.android.hfsis.model.maternal_care_record.Prenatal8AncEntity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class Prenatal8AncFormFragment extends Fragment {

    private int maternalRecordId = -1;
    private int profileId = -1;
    private String patientName = "";

    private DatabaseHelper db;

    // Header View
    private TextView tvFormPatientHeader;

    // Date & BP Input Views (Visits 1 - 8)
    private TextInputEditText etVisit1Date, etVisit1Bp;
    private TextInputEditText etVisit2Date, etVisit2Bp;
    private TextInputEditText etVisit3Date, etVisit3Bp;
    private TextInputEditText etVisit4Date, etVisit4Bp;
    private TextInputEditText etVisit5Date, etVisit5Bp;
    private TextInputEditText etVisit6Date, etVisit6Bp;
    private TextInputEditText etVisit7Date, etVisit7Bp;
    private TextInputEditText etVisit8Date, etVisit8Bp;

    // Status Checkboxes & Details
    private CheckBox cbCompleted8Anc, cbHighBp, cbDangerSigns, cbHighBpReferred;
    private TextInputEditText etDangerSignsDetail, etDateReferred;
    private AutoCompleteTextView actvClassification;
    private TextInputEditText etClassificationDate;

    private Button btnSave8AncForm;

    public Prenatal8AncFormFragment() {
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
        return inflater.inflate(R.layout.fragment_prenatal8_anc_form, container, false);
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

        initFormViews(view);

        if (patientName != null && !patientName.isEmpty()) {
            tvFormPatientHeader.setText("Patient: " + patientName + " (Maternal ID: " + maternalRecordId + ")");
        }

        setupClassificationDropdown();
        setupAllDatePickers();
        checkAndLoadExistingData();

        btnSave8AncForm.setOnClickListener(v -> saveOrUpdateFormInput());
    }

    private void initFormViews(View view) {
        tvFormPatientHeader = view.findViewById(R.id.tvFormPatientHeader);

        etVisit1Date = view.findViewById(R.id.etVisit1Date);
        etVisit1Bp = view.findViewById(R.id.etVisit1Bp);
        etVisit2Date = view.findViewById(R.id.etVisit2Date);
        etVisit2Bp = view.findViewById(R.id.etVisit2Bp);
        etVisit3Date = view.findViewById(R.id.etVisit3Date);
        etVisit3Bp = view.findViewById(R.id.etVisit3Bp);
        etVisit4Date = view.findViewById(R.id.etVisit4Date);
        etVisit4Bp = view.findViewById(R.id.etVisit4Bp);
        etVisit5Date = view.findViewById(R.id.etVisit5Date);
        etVisit5Bp = view.findViewById(R.id.etVisit5Bp);
        etVisit6Date = view.findViewById(R.id.etVisit6Date);
        etVisit6Bp = view.findViewById(R.id.etVisit6Bp);
        etVisit7Date = view.findViewById(R.id.etVisit7Date);
        etVisit7Bp = view.findViewById(R.id.etVisit7Bp);
        etVisit8Date = view.findViewById(R.id.etVisit8Date);
        etVisit8Bp = view.findViewById(R.id.etVisit8Bp);

        cbCompleted8Anc = view.findViewById(R.id.cbCompleted8Anc);
        cbHighBp = view.findViewById(R.id.cbHighBp);
        cbDangerSigns = view.findViewById(R.id.cbDangerSigns);
        etDangerSignsDetail = view.findViewById(R.id.etDangerSignsDetail);
        cbHighBpReferred = view.findViewById(R.id.cbHighBpReferred);
        etDateReferred = view.findViewById(R.id.etDateReferred);
        actvClassification = view.findViewById(R.id.actvClassification);
        etClassificationDate = view.findViewById(R.id.etClassificationDate);

        btnSave8AncForm = view.findViewById(R.id.btnSave8AncForm);
    }

    /**
     * ArrayAdapter that never filters by the current text, so after an existing
     * classification is loaded the user can still pick any other option.
     */
    private static class NoFilterAdapter extends ArrayAdapter<String> {
        private final String[] items;

        NoFilterAdapter(android.content.Context context, String[] items) {
            super(context, android.R.layout.simple_dropdown_item_1line, items);
            this.items = items;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    results.values = items;
                    results.count = items.length;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }
    }

    private void setupClassificationDropdown() {
        String[] classificationOptions = {
                "A - Resident",
                "B - Trans In",
                "C - Trans Out before receiving 8ANC"
        };
        actvClassification.setAdapter(new NoFilterAdapter(requireContext(), classificationOptions));
        actvClassification.setOnClickListener(v -> actvClassification.showDropDown());
    }

    private void setupAllDatePickers() {
        bindDatePickerDialog(etVisit1Date);
        bindDatePickerDialog(etVisit2Date);
        bindDatePickerDialog(etVisit3Date);
        bindDatePickerDialog(etVisit4Date);
        bindDatePickerDialog(etVisit5Date);
        bindDatePickerDialog(etVisit6Date);
        bindDatePickerDialog(etVisit7Date);
        bindDatePickerDialog(etVisit8Date);
        bindDatePickerDialog(etDateReferred);
        bindDatePickerDialog(etClassificationDate);
    }

    private void bindDatePickerDialog(TextInputEditText field) {
        if (field == null) return;

        // Formats keyboard inputs while typing manually
        field.addTextChangedListener(new DateFormattingWatcher(field));

        // Opens calendar date picker only when clicking the end icon
        TextInputLayout til = findParentTextInputLayout(field);
        if (til != null) {
            til.setEndIconOnClickListener(v -> showDatePickerDialog(field));
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

    private void showDatePickerDialog(TextInputEditText field) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.US, "%02d-%02d-%04d", month + 1, dayOfMonth, year);
            field.setText(selectedDate);
            field.setError(null);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void checkAndLoadExistingData() {
        if (maternalRecordId == -1) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            Prenatal8AncEntity record = db.prenatal8AncDao().getRecordByMaternalId(maternalRecordId);
            if (record != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    btnSave8AncForm.setText("Update Record");

                    etVisit1Date.setText(record.visit1Date);
                    etVisit1Bp.setText(record.visit1Bp);
                    etVisit2Date.setText(record.visit2Date);
                    etVisit2Bp.setText(record.visit2Bp);
                    etVisit3Date.setText(record.visit3Date);
                    etVisit3Bp.setText(record.visit3Bp);
                    etVisit4Date.setText(record.visit4Date);
                    etVisit4Bp.setText(record.visit4Bp);
                    etVisit5Date.setText(record.visit5Date);
                    etVisit5Bp.setText(record.visit5Bp);
                    etVisit6Date.setText(record.visit6Date);
                    etVisit6Bp.setText(record.visit6Bp);
                    etVisit7Date.setText(record.visit7Date);
                    etVisit7Bp.setText(record.visit7Bp);
                    etVisit8Date.setText(record.visit8Date);
                    etVisit8Bp.setText(record.visit8Bp);

                    cbCompleted8Anc.setChecked(record.completed8Anc);
                    cbHighBp.setChecked(record.highBp);
                    cbDangerSigns.setChecked(record.dangerSigns);
                    etDangerSignsDetail.setText(record.dangerSignsDetail);
                    cbHighBpReferred.setChecked(record.highBpReferred);
                    etDateReferred.setText(record.dateReferred);

                    if (record.classificationStatus != null) {
                        actvClassification.setText(record.classificationStatus, false);
                    }
                    etClassificationDate.setText(record.classificationDate);
                });
            }
        });
    }

    private void saveOrUpdateFormInput() {
        if (maternalRecordId == -1) {
            Toast.makeText(getContext(), "Error: Missing Maternal ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read all UI values on the main thread
        final Prenatal8AncEntity record = new Prenatal8AncEntity();
        record.maternalRecordId = maternalRecordId;

        record.visit1Date = etVisit1Date.getText().toString().trim();
        record.visit1Bp = etVisit1Bp.getText().toString().trim();
        record.visit2Date = etVisit2Date.getText().toString().trim();
        record.visit2Bp = etVisit2Bp.getText().toString().trim();
        record.visit3Date = etVisit3Date.getText().toString().trim();
        record.visit3Bp = etVisit3Bp.getText().toString().trim();
        record.visit4Date = etVisit4Date.getText().toString().trim();
        record.visit4Bp = etVisit4Bp.getText().toString().trim();
        record.visit5Date = etVisit5Date.getText().toString().trim();
        record.visit5Bp = etVisit5Bp.getText().toString().trim();
        record.visit6Date = etVisit6Date.getText().toString().trim();
        record.visit6Bp = etVisit6Bp.getText().toString().trim();
        record.visit7Date = etVisit7Date.getText().toString().trim();
        record.visit7Bp = etVisit7Bp.getText().toString().trim();
        record.visit8Date = etVisit8Date.getText().toString().trim();
        record.visit8Bp = etVisit8Bp.getText().toString().trim();

        record.completed8Anc = cbCompleted8Anc.isChecked();
        record.highBp = cbHighBp.isChecked();
        record.dangerSigns = cbDangerSigns.isChecked();
        record.dangerSignsDetail = etDangerSignsDetail.getText().toString().trim();
        record.highBpReferred = cbHighBpReferred.isChecked();
        record.dateReferred = etDateReferred.getText().toString().trim();
        record.classificationStatus = actvClassification.getText().toString().trim();
        record.classificationDate = etClassificationDate.getText().toString().trim();

        record.isSynced = false;
        record.updatedAt = System.currentTimeMillis();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Decide insert vs update from the DB itself
                Prenatal8AncEntity existing = db.prenatal8AncDao().getRecordByMaternalId(maternalRecordId);
                final boolean isUpdate = existing != null;

                if (isUpdate) {
                    record.id = existing.id;
                    record.newInsert = existing.newInsert; // keep "never uploaded" state
                    db.prenatal8AncDao().update8AncRecord(record);
                } else {
                    record.newInsert = true;
                    db.prenatal8AncDao().insert8AncRecord(record);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String message = isUpdate
                                ? "8ANC record updated successfully!"
                                : "8ANC record saved successfully!";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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