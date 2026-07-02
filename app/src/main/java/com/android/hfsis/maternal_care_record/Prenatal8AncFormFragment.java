package com.android.hfsis.maternal_care_record;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.maternal_care_record.Prenatal8AncEntity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class Prenatal8AncFormFragment extends Fragment {

    private int maternalRecordId = -1;
    private int profileId = -1;
    private String patientName = "";

    // Component configurations and tracking states
    private DatabaseHelper db;
    private boolean isEditMode = false;
    private int existingRecordId = -1;

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

    // Integrated Variable
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getDatabase(requireContext());

        // Initialize Form Views
        initFormViews(view);

        // Bind patient baseline header text metadata
        if (patientName != null && !patientName.isEmpty()) {
            tvFormPatientHeader.setText("Patient: " + patientName + " (Maternal ID: " + maternalRecordId + ")");
        }

        setupClassificationDropdown();
        setupAllDatePickers();

        // Pull down existing sub-record entries if already captured
        checkAndLoadExistingData();

        // Bind form operational submission flow execution
        btnSave8AncForm.setOnClickListener(v -> saveOrUpdateFormInput());
    }

    private void initFormViews(View view) {
        tvFormPatientHeader = view.findViewById(R.id.tvFormPatientHeader);

        // Map Visit Inputs
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

        // Map Checkboxes and evaluation strings
        cbCompleted8Anc = view.findViewById(R.id.cbCompleted8Anc);
        cbHighBp = view.findViewById(R.id.cbHighBp);
        cbDangerSigns = view.findViewById(R.id.cbDangerSigns);
        etDangerSignsDetail = view.findViewById(R.id.etDangerSignsDetail);
        cbHighBpReferred = view.findViewById(R.id.cbHighBpReferred);
        etDateReferred = view.findViewById(R.id.etDateReferred);
        actvClassification = view.findViewById(R.id.actvClassification);

        // Connect the layout id reference field
        etClassificationDate = view.findViewById(R.id.etClassificationDate);

        btnSave8AncForm = view.findViewById(R.id.btnSave8AncForm);
    }

    private void setupClassificationDropdown() {
        String[] classificationOptions = {
                "A - Resident",
                "B - Trans In",
                "C - Trans Out before receiving 8ANC"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, classificationOptions);
        actvClassification.setAdapter(adapter);
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
        bindDatePickerDialog(etClassificationDate); // Bind popup dialogue behavior to the target field
    }

    private void bindDatePickerDialog(TextInputEditText field) {
        if (field == null) return;
        field.setFocusable(false);
        field.setClickable(true);
        field.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                String selectedDate = String.format(Locale.US, "%02d/%02d/%04d", month + 1, dayOfMonth, year);
                field.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void checkAndLoadExistingData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Prenatal8AncEntity record = db.prenatal8AncDao().getRecordByMaternalId(maternalRecordId);
            if (record != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    isEditMode = true;
                    existingRecordId = record.id;
                    btnSave8AncForm.setText("Update Record");

                    // Populate fields cleanly
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

                    // Pre-fill classification date if editing
                    etClassificationDate.setText(record.classificationDate);
                });
            }
        });
    }

    private void saveOrUpdateFormInput() {
        // Collect standard form inputs
        String v1Date = etVisit1Date.getText().toString().trim();
        String v1Bp = etVisit1Bp.getText().toString().trim();
        String v2Date = etVisit2Date.getText().toString().trim();
        String v2Bp = etVisit2Bp.getText().toString().trim();
        String v3Date = etVisit3Date.getText().toString().trim();
        String v3Bp = etVisit3Bp.getText().toString().trim();
        String v4Date = etVisit4Date.getText().toString().trim();
        String v4Bp = etVisit4Bp.getText().toString().trim();
        String v5Date = etVisit5Date.getText().toString().trim();
        String v5Bp = etVisit5Bp.getText().toString().trim();
        String v6Date = etVisit6Date.getText().toString().trim();
        String v6Bp = etVisit6Bp.getText().toString().trim();
        String v7Date = etVisit7Date.getText().toString().trim();
        String v7Bp = etVisit7Bp.getText().toString().trim();
        String v8Date = etVisit8Date.getText().toString().trim();
        String v8Bp = etVisit8Bp.getText().toString().trim();

        boolean comp8Anc = cbCompleted8Anc.isChecked();
        boolean hBp = cbHighBp.isChecked();
        boolean dSigns = cbDangerSigns.isChecked();
        String dSignsDetail = etDangerSignsDetail.getText().toString().trim();
        boolean hBpReferred = cbHighBpReferred.isChecked();
        String dReferred = etDateReferred.getText().toString().trim();
        String classStatus = actvClassification.getText().toString().trim();

        // Extract string value from layout input field
        String classDate = etClassificationDate.getText().toString().trim();

        Executors.newSingleThreadExecutor().execute(() -> {
            Prenatal8AncEntity record = new Prenatal8AncEntity();
            record.maternalRecordId = maternalRecordId;
            record.visit1Date = v1Date;
            record.visit1Bp = v1Bp;
            record.visit2Date = v2Date;
            record.visit2Bp = v2Bp;
            record.visit3Date = v3Date;
            record.visit3Bp = v3Bp;
            record.visit4Date = v4Date;
            record.visit4Bp = v4Bp;
            record.visit5Date = v5Date;
            record.visit5Bp = v5Bp;
            record.visit6Date = v6Date;
            record.visit6Bp = v6Bp;
            record.visit7Date = v7Date;
            record.visit7Bp = v7Bp;
            record.visit8Date = v8Date;
            record.visit8Bp = v8Bp;

            record.completed8Anc = comp8Anc;
            record.highBp = hBp;
            record.dangerSigns = dSigns;
            record.dangerSignsDetail = dSignsDetail;
            record.highBpReferred = hBpReferred;
            record.dateReferred = dReferred;
            record.classificationStatus = classStatus;

            // Map gathered data variable string value to Entity model
            record.classificationDate = classDate;

            if (isEditMode) {
                record.id = existingRecordId;
                db.prenatal8AncDao().update8AncRecord(record);
            } else {
                db.prenatal8AncDao().insert8AncRecord(record);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String message = isEditMode ? "8ANC record updated successfully!" : "8ANC record saved successfully!";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
            }
        });
    }
}