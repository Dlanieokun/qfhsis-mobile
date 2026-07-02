package com.android.hfsis.maternal_care_record;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.maternal_care_record.PrenatalLabScreeningEntity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PrenatalLabScreeningFragment extends Fragment {

    private int maternalRecordId = -1;
    private int profileId = -1;
    private String patientName = "";

    private DatabaseHelper db;
    private boolean isEditMode = false;
    private int existingRecordId = -1;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        // Syphilis Layout Map UI Bindings
        etSyphilisDate = view.findViewById(R.id.etSyphilisDate);
        actvSyphilisResult = view.findViewById(R.id.actvSyphilisResult);
        etSyphilisRemarks = view.findViewById(R.id.etSyphilisRemarks);
        etSyphilisConfirmatoryDate = view.findViewById(R.id.etSyphilisConfirmatoryDate);
        actvSyphilisConfirmatoryResult = view.findViewById(R.id.actvSyphilisConfirmatoryResult);
        actvSyphilisTreatment = view.findViewById(R.id.actvSyphilisTreatment);

        btnSaveLabScreeningForm = view.findViewById(R.id.btnSaveLabScreeningForm);
    }

    private void setupDropdownMenus() {
        if (getContext() == null) return;

        String[] cbcOptions = {"Normal", "Anemic"};
        String[] gdmOptions = {"Normal", "At Risk (GDM)"};
        String[] reactiveOptions = {"Non-Reactive", "Reactive"};

        // Added Custom Layout Selectable Arrays
        String[] syphilisConfirmatoryOptions = {"1 - Positive", "0 - Negative"};
        String[] syphilisTreatmentOptions = {"1 - Yes", "0 - No"};

        ArrayAdapter<String> cbcAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, cbcOptions);
        ArrayAdapter<String> gdmAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, gdmOptions);
        ArrayAdapter<String> reactiveAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, reactiveOptions);
        ArrayAdapter<String> confirmatoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, syphilisConfirmatoryOptions);
        ArrayAdapter<String> treatmentAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, syphilisTreatmentOptions);

        actvCbcResult.setAdapter(cbcAdapter);
        actvGdmResult.setAdapter(gdmAdapter);
        actvHepBResult.setAdapter(reactiveAdapter);
        actvHivResult.setAdapter(reactiveAdapter);
        actvSyphilisResult.setAdapter(reactiveAdapter);

        // Set new dynamic dropdown components options maps
        actvSyphilisConfirmatoryResult.setAdapter(confirmatoryAdapter);
        actvSyphilisTreatment.setAdapter(treatmentAdapter);
    }

    private void setupDatePickers() {
        bindDatePicker(etCbcDate);
        bindDatePicker(etGdmDate);
        bindDatePicker(etHepBDate);
        bindDatePicker(etHivDate);
        bindDatePicker(etSyphilisDate);
        bindDatePicker(etSyphilisConfirmatoryDate); // Configured confirmatory picker link
    }

    private void bindDatePicker(TextInputEditText editText) {
        if (editText == null) return;
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog picker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String selectedDate = String.format(Locale.US, "%02d/%02d/%04d", month + 1, dayOfMonth, year);
                editText.setText(selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });
    }

    private void loadExistingLabScreeningData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            PrenatalLabScreeningEntity record = db.prenatalLabScreeningDao().getRecordByMaternalId(maternalRecordId);
            if (record != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    isEditMode = true;
                    existingRecordId = record.id;
                    btnSaveLabScreeningForm.setText("Update Laboratory Screenings");

                    // Restore CBC
                    etCbcDate.setText(record.cbcDate);
                    actvCbcResult.setText(record.cbcResult, false);
                    etCbcRemarks.setText(record.cbcRemarks);

                    // Restore GDM
                    etGdmDate.setText(record.gdmDate);
                    actvGdmResult.setText(record.gdmResult, false);
                    etGdmRemarks.setText(record.gdmRemarks);

                    // Restore HepB
                    etHepBDate.setText(record.hepBDate);
                    actvHepBResult.setText(record.hepBResult, false);
                    etHepBRemarks.setText(record.hepBRemarks);

                    // Restore HIV
                    etHivDate.setText(record.hivDate);
                    actvHivResult.setText(record.hivResult, false);
                    etHivRemarks.setText(record.hivRemarks);

                    // Restore Syphilis
                    etSyphilisDate.setText(record.syphilisDate);
                    actvSyphilisResult.setText(record.syphilisResult, false);
                    etSyphilisRemarks.setText(record.syphilisRemarks);

                    // Restore extra added properties parameters safely
                    etSyphilisConfirmatoryDate.setText(record.syphilisConfirmatoryDate);
                    actvSyphilisConfirmatoryResult.setText(record.syphilisConfirmatoryResult, false);
                    actvSyphilisTreatment.setText(record.syphilisTreatment, false);
                });
            }
        });
    }

    private void executeSaveOrUpdatePipeline() {
        PrenatalLabScreeningEntity data = new PrenatalLabScreeningEntity();
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

        // Map data properties states from updated UI views elements fields explicitly
        data.syphilisConfirmatoryDate = etSyphilisConfirmatoryDate.getText().toString().trim();
        data.syphilisConfirmatoryResult = actvSyphilisConfirmatoryResult.getText().toString().trim();
        data.syphilisTreatment = actvSyphilisTreatment.getText().toString().trim();

        Executors.newSingleThreadExecutor().execute(() -> {
            if (isEditMode) {
                data.id = existingRecordId;
                db.prenatalLabScreeningDao().updateLabScreening(data);
            } else {
                db.prenatalLabScreeningDao().insertLabScreening(data);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String statusMessage = isEditMode ? "Laboratory screenings log updated successfully!" : "Laboratory screenings log saved successfully!";
                    Toast.makeText(getContext(), statusMessage, Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
            }
        });
    }
}