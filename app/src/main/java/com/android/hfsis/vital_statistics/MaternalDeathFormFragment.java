package com.android.hfsis.vital_statistics;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.vital_statistics.MaternalDeathRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MaternalDeathFormFragment extends Fragment {

    private EditText etFullName, etAddress, etAge, etRemarks;
    private EditText etDate;
    private Spinner spAgeGroup, spPlace, spCause;
    private Button btnSave, btnCancel;
    private DatabaseHelper db;
    private MaternalDeathRecord record;
    private int recordId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maternal_death_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(getContext());

        // Initialize views
        etFullName = view.findViewById(R.id.etFullName);
        etDate = view.findViewById(R.id.etDate);
        etAddress = view.findViewById(R.id.etAddress);
        etAge = view.findViewById(R.id.etAge);
        etRemarks = view.findViewById(R.id.etRemarks);
        spAgeGroup = view.findViewById(R.id.spAgeGroup);
        spPlace = view.findViewById(R.id.spPlace);
        spCause = view.findViewById(R.id.spCause);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        setupSpinners();

        // Check if editing existing record
        if (getArguments() != null && getArguments().containsKey("recordId")) {
            recordId = getArguments().getInt("recordId");
            loadRecordData();
        }

        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveRecord());
        btnCancel.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupSpinners() {
        // Age Group Spinner
        ArrayAdapter<CharSequence> ageGroupAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.maternal_age_groups, android.R.layout.simple_spinner_item);
        ageGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAgeGroup.setAdapter(ageGroupAdapter);

        // Place Spinner
        ArrayAdapter<CharSequence> placeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.place_of_occurrence, android.R.layout.simple_spinner_item);
        placeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPlace.setAdapter(placeAdapter);

        // Cause Spinner
        ArrayAdapter<CharSequence> causeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.cause_of_death, android.R.layout.simple_spinner_item);
        causeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCause.setAdapter(causeAdapter);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.US, "%02d/%02d/%04d", month + 1, dayOfMonth, year);
                    etDate.setText(date);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void loadRecordData() {
        new Thread(() -> {
            record = db.maternalDeathDao().getRecordById(recordId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::populateFields);
            }
        }).start();
    }

    private void populateFields() {
        if (record != null) {
            etFullName.setText(record.fullName != null ? record.fullName : "");
            etDate.setText(record.dateOfRegistration != null ? record.dateOfRegistration : "");
            etAddress.setText(record.completeAddress != null ? record.completeAddress : "");
            etAge.setText(String.valueOf(record.age));
            etRemarks.setText(record.remarks != null ? record.remarks : "");

            // Set spinner values
            setSpinnerValue(spAgeGroup, record.ageGroup);
            setSpinnerValue(spPlace, record.placeOfOccurrence);
            setSpinnerValue(spCause, record.causeOfDeath);
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            int position = adapter.getPosition(value);
            spinner.setSelection(position);
        }
    }

    private void saveRecord() {
        String fullName = etFullName.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String remarks = etRemarks.getText().toString().trim();
        String ageGroup = spAgeGroup.getSelectedItem().toString();
        String place = spPlace.getSelectedItem().toString();
        String cause = spCause.getSelectedItem().toString();

        if (fullName.isEmpty() || date.isEmpty() || address.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);

        new Thread(() -> {
            if (recordId == -1) {
                // Create new record
                MaternalDeathRecord newRecord = new MaternalDeathRecord(
                        date, fullName, address, age, ageGroup, place, cause, remarks
                );
                db.maternalDeathDao().insert(newRecord);
            } else {
                // Update existing record
                record.dateOfRegistration = date;
                record.fullName = fullName;
                record.completeAddress = address;
                record.age = age;
                record.ageGroup = ageGroup;
                record.placeOfOccurrence = place;
                record.causeOfDeath = cause;
                record.remarks = remarks;
                db.maternalDeathDao().update(record);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Record saved successfully", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                });
            }
        }).start();
    }
}