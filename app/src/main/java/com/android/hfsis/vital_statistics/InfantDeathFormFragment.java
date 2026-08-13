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
import com.android.hfsis.model.vital_statistics.InfantDeathRecord;

import java.util.Calendar;
import java.util.Locale;

public class InfantDeathFormFragment extends Fragment {

    private EditText etFullName, etAddress, etAge, etRemarks;
    private EditText etDate;
    private Spinner spSex;
    private Button btnSave, btnCancel;
    private DatabaseHelper db;
    private InfantDeathRecord record;
    private int recordId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_infant_death_form, container, false);
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
        spSex = view.findViewById(R.id.spSex);
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
        // Sex Spinner
        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.infant_sex, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSex.setAdapter(sexAdapter);
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
            record = db.infantDeathDao().getRecordById(recordId);
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

            // Set spinner value
            setSpinnerValue(spSex, record.sex);
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
        String sex = spSex.getSelectedItem().toString();

        if (fullName.isEmpty() || date.isEmpty() || address.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);

        new Thread(() -> {
            if (recordId == -1) {
                // Create new record
                InfantDeathRecord newRecord = new InfantDeathRecord(
                        date, fullName, address, age, sex, remarks
                );
                db.infantDeathDao().insert(newRecord);
            } else {
                // Update existing record
                record.dateOfRegistration = date;
                record.fullName = fullName;
                record.completeAddress = address;
                record.age = age;
                record.sex = sex;
                record.remarks = remarks;
                db.infantDeathDao().update(record);
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