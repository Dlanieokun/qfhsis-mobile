package com.android.hfsis.maternal_care_record;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.maternal_care_record.PrenatalImmunizationEntity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PrenatalImmunizationFragment extends Fragment {

    private int maternalRecordId = -1;
    private int profileId = -1;
    private String patientName = "";

    // Database tracking state managers
    private DatabaseHelper db;
    private boolean isEditMode = false;
    private int existingRecordId = -1;

    // View Components Layout elements mapping properties
    private TextView tvImmPatientHeader;
    private TextInputEditText etTd1Date, etTd2Date, etTd3Date, etTd4Date, etTd5Date;
    private Button btnSaveImmunizationForm;

    public PrenatalImmunizationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Safely extract transaction intent parameters sent from main list module
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize central persistence access controller helper Instance
        db = DatabaseHelper.getDatabase(requireContext());

        // Bind contextual layout parameters properties
        tvImmPatientHeader = view.findViewById(R.id.tvImmPatientHeader);
        if (patientName != null && !patientName.isEmpty()) {
            tvImmPatientHeader.setText("Patient: " + patientName + " (Maternal ID: " + maternalRecordId + ")");
        }

        // Bind form data inputs matching resource layout definitions
        etTd1Date = view.findViewById(R.id.etTd1Date);
        etTd2Date = view.findViewById(R.id.etTd2Date);
        etTd3Date = view.findViewById(R.id.etTd3Date);
        etTd4Date = view.findViewById(R.id.etTd4Date);
        etTd5Date = view.findViewById(R.id.etTd5Date);
        btnSaveImmunizationForm = view.findViewById(R.id.btnSaveImmunizationForm);

        // Wire operational behavior constraints and selection popup listeners
        setupAllDatePickerDialogs();

        // Attempt background read transaction to retrieve historical captures for this maternal patient
        loadExistingImmunizationData();

        // Form submission click pipeline execution handler
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
        inputField.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String formattedSelectionDate = String.format(Locale.US, "%02d/%02d/%04d", month + 1, dayOfMonth, year);
                inputField.setText(formattedSelectionDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void loadExistingImmunizationData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            PrenatalImmunizationEntity existingLogRecord = db.prenatalImmunizationDao().getRecordByMaternalId(maternalRecordId);
            if (existingLogRecord != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Turn switch on and adjust operational labels on main UIThread runtime execution
                    isEditMode = true;
                    existingRecordId = existingLogRecord.id;
                    btnSaveImmunizationForm.setText("Update Immunization Log");

                    // Map fields text cleanly from tracking state models
                    etTd1Date.setText(existingLogRecord.td1Date);
                    etTd2Date.setText(existingLogRecord.td2Date);
                    etTd3Date.setText(existingLogRecord.td3Date);
                    etTd4Date.setText(existingLogRecord.td4Date);
                    etTd5Date.setText(existingLogRecord.td5Date);
                });
            }
        });
    }

    private void executeSaveOrUpdatePipeline() {
        // Gather captured inputs from localized properties fields
        String td1Val = etTd1Date.getText().toString().trim();
        String td2Val = etTd2Date.getText().toString().trim();
        String td3Val = etTd3Date.getText().toString().trim();
        String td4Val = etTd4Date.getText().toString().trim();
        String td5Val = etTd5Date.getText().toString().trim();

        // Execute persistence operation task sequence out-of-band to safeguard tracking frame responsiveness
        Executors.newSingleThreadExecutor().execute(() -> {
            PrenatalImmunizationEntity targetRecordRow = new PrenatalImmunizationEntity();
            targetRecordRow.maternalRecordId = maternalRecordId;
            targetRecordRow.td1Date = td1Val;
            targetRecordRow.td2Date = td2Val;
            targetRecordRow.td3Date = td3Val;
            targetRecordRow.td4Date = td4Val;
            targetRecordRow.td5Date = td5Val;

            if (isEditMode) {
                targetRecordRow.id = existingRecordId;
                db.prenatalImmunizationDao().updateImmunizationRecord(targetRecordRow);
            } else {
                db.prenatalImmunizationDao().insertImmunizationRecord(targetRecordRow);
            }

            // Return user to management transaction screen logs and prompt dynamic indicators confirmation message
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String feedbackToastMessage = isEditMode ?
                            "Immunization record updated successfully!" :
                            "Immunization record saved successfully!";
                    Toast.makeText(getContext(), feedbackToastMessage, Toast.LENGTH_SHORT).show();

                    // Pop transaction tracking execution frame back up to parent dashboard
                    getParentFragmentManager().popBackStack();
                });
            }
        });
    }
}