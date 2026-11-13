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
import android.widget.EditText;
import android.widget.Filter;
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
import com.android.hfsis.model.maternal_care_record.PrenatalLabScreeningEntity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PrenatalLabScreeningFragment extends Fragment {

    private int maternalRecordId = -1;
    private int profileId = -1;
    private String patientName = "";

    private DatabaseHelper db;

    private TextView tvLabPatientHeader;
    private Button btnSaveLabScreeningForm;

    // CBC fields
    private TextInputEditText etCbcDate, etCbcRemarks;
    private AutoCompleteTextView actvCbcResult;

    // GDM fields
    private TextInputEditText etGdmDate, etGdmRemarks;
    private AutoCompleteTextView actvGdmResult;

    // Hepatitis B fields
    private TextInputEditText etHepBDate, etHepBRemarks;
    private AutoCompleteTextView actvHepBResult;

    // HIV fields
    private TextInputEditText etHivDate, etHivRemarks;
    private AutoCompleteTextView actvHivResult;

    // Syphilis fields
    private TextInputEditText etSyphilisDate, etSyphilisRemarks, etSyphilisConfirmatoryDate;
    private AutoCompleteTextView actvSyphilisResult, actvSyphilisConfirmatoryResult, actvSyphilisTreatment;

    public PrenatalLabScreeningFragment() {
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
        return inflater.inflate(R.layout.fragment_prenatal_lab_screening, container, false);
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

        tvLabPatientHeader = view.findViewById(R.id.tvLabPatientHeader);
        if (patientName != null && !patientName.isEmpty()) {
            tvLabPatientHeader.setText("Patient: " + patientName + " (Maternal ID: " + maternalRecordId + ")");
        }

        initializeFormFields(view);
        setupDropdownMenus();
        setupDatePickers();
        loadExistingLabScreeningData();

        btnSaveLabScreeningForm.setOnClickListener(v -> executeSaveOrUpdatePipeline());
    }

    private void initializeFormFields(View view) {
        // CBC
        etCbcDate = view.findViewById(R.id.etCbcDate);
        actvCbcResult = view.findViewById(R.id.actvCbcResult);
        etCbcRemarks = view.findViewById(R.id.etCbcRemarks);

        // GDM
        etGdmDate = view.findViewById(R.id.etGdmDate);
        actvGdmResult = view.findViewById(R.id.actvGdmResult);
        etGdmRemarks = view.findViewById(R.id.etGdmRemarks);

        // Hepatitis B
        etHepBDate = view.findViewById(R.id.etHepBDate);
        actvHepBResult = view.findViewById(R.id.actvHepBResult);
        etHepBRemarks = view.findViewById(R.id.etHepBRemarks);

        // HIV
        etHivDate = view.findViewById(R.id.etHivDate);
        actvHivResult = view.findViewById(R.id.actvHivResult);
        etHivRemarks = view.findViewById(R.id.etHivRemarks);

        // Syphilis
        etSyphilisDate = view.findViewById(R.id.etSyphilisDate);
        actvSyphilisResult = view.findViewById(R.id.actvSyphilisResult);
        etSyphilisRemarks = view.findViewById(R.id.etSyphilisRemarks);
        etSyphilisConfirmatoryDate = view.findViewById(R.id.etSyphilisConfirmatoryDate);
        actvSyphilisConfirmatoryResult = view.findViewById(R.id.actvSyphilisConfirmatoryResult);
        actvSyphilisTreatment = view.findViewById(R.id.actvSyphilisTreatment);

        btnSaveLabScreeningForm = view.findViewById(R.id.btnSaveLabScreeningForm);
    }

    /**
     * ArrayAdapter that never filters by the current text, so after an existing
     * value is loaded (e.g. "Normal") the user can still pick any other option.
     */
    private static class NoFilterAdapter extends ArrayAdapter<String> {
        private final String[] items;

        NoFilterAdapter(android.content.Context context, String[] items) {
            super(context, android.R.layout.simple_list_item_1, items);
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

    private void bindDropdown(AutoCompleteTextView view, String[] options) {
        view.setAdapter(new NoFilterAdapter(requireContext(), options));
        view.setOnClickListener(v -> view.showDropDown());
    }

    private void setupDropdownMenus() {
        if (getContext() == null) return;

        String[] cbcOptions = {"Normal", "Anemic"};
        String[] gdmOptions = {"Normal", "At Risk (GDM)"};
        String[] reactiveOptions = {"Non-Reactive", "Reactive"};
        String[] syphilisConfirmatoryOptions = {"1 - Positive", "0 - Negative"};
        String[] syphilisTreatmentOptions = {"1 - Yes", "0 - No"};

        bindDropdown(actvCbcResult, cbcOptions);
        bindDropdown(actvGdmResult, gdmOptions);
        bindDropdown(actvHepBResult, reactiveOptions);
        bindDropdown(actvHivResult, reactiveOptions);
        bindDropdown(actvSyphilisResult, reactiveOptions);
        bindDropdown(actvSyphilisConfirmatoryResult, syphilisConfirmatoryOptions);
        bindDropdown(actvSyphilisTreatment, syphilisTreatmentOptions);
    }

    private void setupDatePickers() {
        bindDatePicker(etCbcDate);
        bindDatePicker(etGdmDate);
        bindDatePicker(etHepBDate);
        bindDatePicker(etHivDate);
        bindDatePicker(etSyphilisDate);
        bindDatePicker(etSyphilisConfirmatoryDate);
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

    private void loadExistingLabScreeningData() {
        if (maternalRecordId == -1) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            PrenatalLabScreeningEntity record = db.prenatalLabScreeningDao().getRecordByMaternalId(maternalRecordId);
            if (record != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    btnSaveLabScreeningForm.setText("Update Laboratory Screenings");

                    // CBC
                    etCbcDate.setText(record.cbcDate);
                    actvCbcResult.setText(record.cbcResult, false);
                    etCbcRemarks.setText(record.cbcRemarks);

                    // GDM
                    etGdmDate.setText(record.gdmDate);
                    actvGdmResult.setText(record.gdmResult, false);
                    etGdmRemarks.setText(record.gdmRemarks);

                    // HepB
                    etHepBDate.setText(record.hepBDate);
                    actvHepBResult.setText(record.hepBResult, false);
                    etHepBRemarks.setText(record.hepBRemarks);

                    // HIV
                    etHivDate.setText(record.hivDate);
                    actvHivResult.setText(record.hivResult, false);
                    etHivRemarks.setText(record.hivRemarks);

                    // Syphilis
                    etSyphilisDate.setText(record.syphilisDate);
                    actvSyphilisResult.setText(record.syphilisResult, false);
                    etSyphilisRemarks.setText(record.syphilisRemarks);
                    etSyphilisConfirmatoryDate.setText(record.syphilisConfirmatoryDate);
                    actvSyphilisConfirmatoryResult.setText(record.syphilisConfirmatoryResult, false);
                    actvSyphilisTreatment.setText(record.syphilisTreatment, false);
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
        final PrenatalLabScreeningEntity data = new PrenatalLabScreeningEntity();
        data.maternalRecordId = maternalRecordId;

        data.cbcDate = etCbcDate.getText().toString().trim();
        data.cbcResult = actvCbcResult.getText().toString().trim();
        data.cbcRemarks = etCbcRemarks.getText().toString().trim();

        data.gdmDate = etGdmDate.getText().toString().trim();
        data.gdmResult = actvGdmResult.getText().toString().trim();
        data.gdmRemarks = etGdmRemarks.getText().toString().trim();

        data.hepBDate = etHepBDate.getText().toString().trim();
        data.hepBResult = actvHepBResult.getText().toString().trim();
        data.hepBRemarks = etHepBRemarks.getText().toString().trim();

        data.hivDate = etHivDate.getText().toString().trim();
        data.hivResult = actvHivResult.getText().toString().trim();
        data.hivRemarks = etHivRemarks.getText().toString().trim();

        data.syphilisDate = etSyphilisDate.getText().toString().trim();
        data.syphilisResult = actvSyphilisResult.getText().toString().trim();
        data.syphilisRemarks = etSyphilisRemarks.getText().toString().trim();
        data.syphilisConfirmatoryDate = etSyphilisConfirmatoryDate.getText().toString().trim();
        data.syphilisConfirmatoryResult = actvSyphilisConfirmatoryResult.getText().toString().trim();
        data.syphilisTreatment = actvSyphilisTreatment.getText().toString().trim();

        data.isSynced = false; // mark dirty so Upload picks it up
        data.updatedAt = System.currentTimeMillis();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Decide insert vs update from the DB itself, not from a UI flag
                PrenatalLabScreeningEntity existing =
                        db.prenatalLabScreeningDao().getRecordByMaternalId(maternalRecordId);
                final boolean isUpdate = existing != null;

                if (isUpdate) {
                    data.id = existing.id;
                    data.newInsert = existing.newInsert; // keep "never uploaded" state
                    db.prenatalLabScreeningDao().updateLabScreening(data);
                } else {
                    data.newInsert = true;
                    db.prenatalLabScreeningDao().insertLabScreening(data);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String statusMessage = isUpdate
                                ? "Laboratory screenings log updated successfully!"
                                : "Laboratory screenings log saved successfully!";
                        Toast.makeText(getContext(), statusMessage, Toast.LENGTH_SHORT).show();
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