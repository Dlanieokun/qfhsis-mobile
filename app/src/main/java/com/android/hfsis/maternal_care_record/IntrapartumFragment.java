package com.android.hfsis.maternal_care_record;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.maternal_care_record.IntrapartumEntity;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class IntrapartumFragment extends Fragment {

    private Spinner spinnerOutcome, spinnerType, spinnerPlace, spinnerAttendant, spinnerWeightClass;
    private EditText etWeight, etDate, etTime, etRemarks;
    private Button btnSave;
    private RadioGroup rgSex;
    private int maternalRecordId = -1;

    public IntrapartumFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intrapartum, container, false);

        // 1. Retrieve the Maternal ID passed from the List Fragment
        if (getArguments() != null) {
            maternalRecordId = getArguments().getInt("MATERNAL_RECORD_ID", -1);
        }

        initializeViews(view);
        setupSpinners();
        setupDateTimePickers();

        // 2. Load data if record exists for this ID
        if (maternalRecordId != -1) {
            loadExistingData();
        }

        btnSave.setOnClickListener(v -> saveOrUpdateData());

        return view;
    }

    private void initializeViews(View view) {
        spinnerOutcome = view.findViewById(R.id.spinner_delivery_outcome);
        spinnerWeightClass = view.findViewById(R.id.spinner_weight_class);
        spinnerType = view.findViewById(R.id.spinner_delivery_type);
        spinnerPlace = view.findViewById(R.id.spinner_place_delivery);
        spinnerAttendant = view.findViewById(R.id.spinner_attendant);
        etWeight = view.findViewById(R.id.et_weight);
        etDate = view.findViewById(R.id.et_delivery_date);
        etTime = view.findViewById(R.id.et_delivery_time);
        etRemarks = view.findViewById(R.id.et_remarks);
        btnSave = view.findViewById(R.id.btn_save);
        rgSex = view.findViewById(R.id.rg_sex);
    }

    private void loadExistingData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            IntrapartumEntity existing = DatabaseHelper.getInstance(getContext())
                    .intrapartumDao().getRecordByMaternalId(maternalRecordId);

            if (existing != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    etWeight.setText(existing.birthWeight);
                    etDate.setText(existing.deliveryDate);
                    etTime.setText(existing.deliveryTime);
                    etRemarks.setText(existing.remarks);
                    // Add spinner logic here to set selection based on existing strings

                    setSpinnerSelection(spinnerOutcome, existing.deliveryOutcome);
                    setSpinnerSelection(spinnerType, existing.deliveryType);
                    setSpinnerSelection(spinnerPlace, existing.placeOfDelivery);
                    setSpinnerSelection(spinnerAttendant, existing.attendantAtBirth);
                    setSpinnerSelection(spinnerWeightClass, existing.weightClassification);

                    if (existing.sex != null) {
                        if (existing.sex.equals("M - Male")) {
                            rgSex.check(R.id.rb_male); // ID from your XML
                        } else if (existing.sex.equals("F - Female")) {
                            rgSex.check(R.id.rb_female); // ID from your XML
                        }
                    }
                });
            }
        });
    }
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || value.isEmpty()) return;

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveOrUpdateData() {
        if (maternalRecordId == -1) {
            Toast.makeText(getContext(), "Error: Missing Maternal ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getInstance(getContext());
            IntrapartumEntity existing = db.intrapartumDao().getRecordByMaternalId(maternalRecordId);

            // Map data
            IntrapartumEntity entity = (existing != null) ? existing : new IntrapartumEntity();
            int selectedId = rgSex.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedButton = getView().findViewById(selectedId);
                entity.sex = selectedButton.getText().toString();
            } else {
                entity.sex = ""; // Or handle as null/default
            }
            entity.maternalRecordId = maternalRecordId;
            entity.birthWeight = etWeight.getText().toString();
            entity.deliveryDate = etDate.getText().toString();
            entity.deliveryTime = etTime.getText().toString();
            entity.remarks = etRemarks.getText().toString();
            entity.deliveryOutcome = spinnerOutcome.getSelectedItem().toString();
            entity.deliveryType = spinnerType.getSelectedItem().toString();
            entity.placeOfDelivery = spinnerPlace.getSelectedItem().toString();
            entity.attendantAtBirth = spinnerAttendant.getSelectedItem().toString();
            entity.weightClassification = spinnerWeightClass.getSelectedItem().toString();


            // Insert if null, Update if found
            if (existing == null) {
                db.intrapartumDao().insertIntrapartum(entity);
            } else {
                db.intrapartumDao().updateIntrapartum(entity);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), existing == null ? "Saved" : "Updated", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupSpinners() {
        String[] outcomes = {"FT - Full Term", "PT - Pre-term", "FD - Fetal Death", "AB - Abortion"};
        String[] types = {"CS - Cesarean Section", "VD - Vaginal Delivery", "CVCD - Combined Delivery"};
        setupSpinnerAdapter(spinnerOutcome, outcomes);
        setupSpinnerAdapter(spinnerType, types);
        setupSpinnerAdapter(spinnerWeightClass, new String[]{"A - Normal (>2500g)", "B - Low (<2500g)", "C - Unknown"});
        setupSpinnerAdapter(spinnerPlace, new String[]{"Public Facility", "Private Facility", "Home", "Others"});
        setupSpinnerAdapter(spinnerAttendant, new String[]{"MD - Doctor", "RN - Nurse", "MW - Midwife", "O - Others"});

    }

    private void setupSpinnerAdapter(Spinner spinner, String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, y, m, d) ->
                    etDate.setText(String.format(Locale.US, "%02d/%02d/%02d", m + 1, d, y % 100)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (view, h, m) ->
                    etTime.setText(String.format(Locale.US, "%02d:%02d", h, m)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });
    }
}