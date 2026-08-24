package com.android.hfsis.vital_statistics;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.hfsis.model.HouseholdProfile;

import java.util.List;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.vital_statistics.InfantDeathRecord;

import java.util.Calendar;
import java.util.Locale;

public class InfantDeathFormFragment extends Fragment {

    private AutoCompleteTextView etFullName;
    private EditText etAddress, etAge, etRemarks;
    private EditText etDate;
    private Spinner spSex;
    private Button btnSave, btnCancel;
    private DatabaseHelper db;
    private InfantDeathRecord record;
    private int recordId = -1;
    private long selectedProfileId = -1;

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
        etFullName = (AutoCompleteTextView) view.findViewById(R.id.etFullName);
        etDate     = view.findViewById(R.id.etDate);
        etAddress  = view.findViewById(R.id.etAddress);
        etAge      = view.findViewById(R.id.etAge);
        etRemarks  = view.findViewById(R.id.etRemarks);
        spSex      = view.findViewById(R.id.spSex);
        btnSave    = view.findViewById(R.id.btnSave);
        btnCancel  = view.findViewById(R.id.btnCancel);

        setupSpinners();
        loadHouseholdNamesAutoComplete();

        // Check if editing existing record
        if (getArguments() != null && getArguments().containsKey("recordId")) {
            recordId = getArguments().getInt("recordId");
            loadRecordData();
        }

        // Autocomplete selection — autofill address from household profile
        etFullName.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            autofillFromProfile(selectedName);
        });

        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveRecord());
        btnCancel.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void loadHouseholdNamesAutoComplete() {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> names = db.householdProfileDao().getAllHouseholdNames();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (names != null && !names.isEmpty() && isAdded()) {
                        etFullName.setAdapter(new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_list_item_1,
                                names));
                    }
                });
            }
        });
    }

    private void autofillFromProfile(String selectedFullName) {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = db.householdProfileDao()
                    .getProfileByCalculatedName(selectedFullName);

            if (getActivity() != null && profile != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    selectedProfileId = profile.id;

                    // Build address from profile fields
                    StringBuilder address = new StringBuilder();
                    if (profile.sitio != null && !profile.sitio.isEmpty())
                        address.append(profile.sitio).append(", ");
                    if (profile.barangay != null && !profile.barangay.isEmpty())
                        address.append(profile.barangay);
                    if (profile.municipality != null && !profile.municipality.isEmpty())
                        address.append(", ").append(profile.municipality);
                    if (profile.region != null && !profile.region.isEmpty())
                        address.append(", ").append(profile.region);

                    etAddress.setText(address.toString().trim());
                    etFullName.setError(null);
                    etAddress.setError(null);
                });
            }
        });
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.infant_sex,
                android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSex.setAdapter(sexAdapter);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.US, "%02d/%02d/%04d",
                            month + 1, dayOfMonth, year);
                    etDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
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
            selectedProfileId = record.profileId;
            etFullName.setText(record.fullName != null ? record.fullName : "", false);
            etDate.setText(record.dateOfRegistration != null ? record.dateOfRegistration : "");
            etAddress.setText(record.completeAddress != null ? record.completeAddress : "");
            etAge.setText(String.valueOf(record.age));
            etRemarks.setText(record.remarks != null ? record.remarks : "");
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
        String date     = etDate.getText().toString().trim();
        String address  = etAddress.getText().toString().trim();
        String ageStr   = etAge.getText().toString().trim();
        String remarks  = etRemarks.getText().toString().trim();
        String sex      = spSex.getSelectedItem().toString();

        // ── Validate required fields ──────────────────────────────────────────
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }
        if (date.isEmpty()) {
            etDate.setError("Date is required");
            etDate.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }
        if (ageStr.isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }

        // ── FIX: safely parse age — catches the NumberFormatException ─────────
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            etAge.setError("Please enter a valid age (numbers only)");
            etAge.requestFocus();
            Toast.makeText(getContext(),
                    "Invalid age — numbers only", Toast.LENGTH_SHORT).show();
            return;
        }

        if (age < 0 || age > 12) {          // infant: 0–12 months; adjust as needed
            etAge.setError("Age must be between 0 and 12 months");
            etAge.requestFocus();
            return;
        }
        // ─────────────────────────────────────────────────────────────────────

        new Thread(() -> {
            if (recordId == -1) {
                // Insert new record
                InfantDeathRecord newRecord = new InfantDeathRecord(
                        date, fullName, address, age, sex, remarks, selectedProfileId);
                db.infantDeathDao().insert(newRecord);
            } else {
                // Update existing record
                record.dateOfRegistration = date;
                record.fullName           = fullName;
                record.completeAddress    = address;
                record.age                = age;
                record.sex                = sex;
                record.remarks            = remarks;
                record.profileId          = selectedProfileId;
                db.infantDeathDao().update(record);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Record saved successfully", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                });
            }
        }).start();
    }
}