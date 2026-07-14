package com.android.hfsis;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.FamilyPlanningRecord;
import com.android.hfsis.model.HouseholdProfile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class FamilyPlanningFragment extends Fragment {

    private long selectedProfileId = -1;
    private int existingRecordId = -1;
    private boolean isEditMode = false;

    private EditText spinnerRegDate;
    private EditText etFamilySerial;
    private AutoCompleteTextView etFullName;
    private EditText etAddress, etAge, etBirthDate;
    private Spinner spinnerAgeGroup, spinnerClientType, spinnerSource, spinnerPrevMethod, spinnerClientMethodUsed;
    private Button btnSubmitFP;
    private ScrollView fpScrollView;
    private LinearLayout fpContainer;

    public FamilyPlanningFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_family_planning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fpScrollView = view.findViewById(R.id.fpScrollView);
        fpContainer = view.findViewById(R.id.fpContainer);

        spinnerRegDate = view.findViewById(R.id.spinnerRegDate);
        etFamilySerial = view.findViewById(R.id.etFamilySerial);
        etFullName = view.findViewById(R.id.etFullName);
        etAddress = view.findViewById(R.id.etAddress);
        etAge = view.findViewById(R.id.etAge);
        etBirthDate = view.findViewById(R.id.etBirthDate);

        spinnerAgeGroup = view.findViewById(R.id.spinnerAgeGroup);
        spinnerClientType = view.findViewById(R.id.spinnerClientType);
        spinnerClientMethodUsed = view.findViewById(R.id.spinnerClientMethodUsed);
        spinnerSource = view.findViewById(R.id.spinnerSource);
        spinnerPrevMethod = view.findViewById(R.id.spinnerPrevMethod);
        btnSubmitFP = view.findViewById(R.id.btnSubmitFP);

        setCurrentDateAsDefault();
        populateFPFormSpinners();
        loadHouseholdNamesAutoComplete();
        setupDynamicFormListeners();

        // CHECK IF IN EDIT MODE
        if (getArguments() != null && getArguments().containsKey("RECORD_ID")) {
            existingRecordId = getArguments().getInt("RECORD_ID");
            isEditMode = true;
            btnSubmitFP.setText("Update Client Record"); // Change button action title
            loadExistingRecordData(existingRecordId);
        }

        if (fpScrollView != null && fpContainer != null) {
            setupKeyboardFocusAutoScroll();
        }
    }

    private void setCurrentDateAsDefault() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        spinnerRegDate.setText(sdf.format(Calendar.getInstance().getTime()));
    }

    private void populateFPFormSpinners() {
        String[] ageGroupOptions = {"A - 10-14 years old", "B - 15-19 years old", "C - 20-49 years old"};
        String[] clientTypeOptions = {"NA = New Acceptors", "BC = Current Users", "OA = Other Acceptors", "CU-CM = Changing Method", "CU-CC = Changing Clinic", "CU-RS = Restarter"};
        String[] sourceOptions = {"Public", "Private"};
        String[] methodOptions = {"NONE", "BTL = Bilateral Tubal Ligation", "NSV = No-Scalpel Vasectomy", "CON = Condom", "Pills-POP = Progestin Only Pills",
                "Pills-COC = Combined Oral Contraceptives", "INJ = DMPA (Injectables)", "IMP-I = Implant (Interval)", "IMP-PP = Implant (Postpartum)",
                "IUD-I = IUD Interval", "IUD-PP = IUD Postpartum", "NFP-LAM = Lactational Amenorrhea Method", "NFP-BBT = Basal Body Temperature",
                "NFP-CMM = Cervical Mucus Method", "NFP-SDM = Standard Days Method"};

        if (getContext() != null) {
            spinnerAgeGroup.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, ageGroupOptions));
            spinnerClientType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, clientTypeOptions));
            spinnerSource.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, sourceOptions));
            spinnerPrevMethod.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, methodOptions));
            spinnerClientMethodUsed.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, methodOptions));
        }
    }

    private void loadHouseholdNamesAutoComplete() {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            List<String> combinedMemberNames = db.householdProfileDao().getAllHouseholdNames();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (combinedMemberNames != null && !combinedMemberNames.isEmpty() && isAdded()) {
                        etFullName.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, combinedMemberNames));
                    }
                });
            }
        });
    }

    private void loadExistingRecordData(int recordId) {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            FamilyPlanningRecord record = db.familyPlanningDao().getRecordById(recordId);

            if (record != null) {
                HouseholdProfile profile = db.householdProfileDao().getProfileById(record.profileId);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;

                        selectedProfileId = record.profileId;
                        spinnerRegDate.setText(record.registrationDate);
                        etFamilySerial.setText(record.familySerialNumber);
                        etAddress.setText(record.address);
                        etAge.setText(String.valueOf(record.age));
                        etBirthDate.setText(record.birthDate);

                        // Populate the autocomplete text view with patient full name
                        if (profile != null) {
                            String fullName = profile.memberLastName + ", " + profile.memberFirstName;
                            if (profile.memberMiddleName != null && !profile.memberMiddleName.isEmpty()) {
                                fullName += " " + profile.memberMiddleName;
                            }
                            etFullName.setText(fullName, false);
                        }

                        // Set spinner selection drops matching database values
                        setSpinnerValue(spinnerAgeGroup, record.ageGroupCategory);
                        setSpinnerValue(spinnerClientType, record.clientType);
                        setSpinnerValue(spinnerSource, record.commoditySource);
                        setSpinnerValue(spinnerPrevMethod, record.previousMethod);
                        setSpinnerValue(spinnerClientMethodUsed, record.methodUsed); // FIX: Populates the client method used spinner
                    });
                }
            }
        });
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private void setupDynamicFormListeners() {
        etFullName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            autofillClientData(selectedName);
        });

        spinnerRegDate.setOnClickListener(v -> showDatePickerDialog(spinnerRegDate));
        btnSubmitFP.setOnClickListener(v -> saveFamilyPlanningRecord());
    }

    private void autofillClientData(String selectedFullName) {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            HouseholdProfile profile = db.householdProfileDao().getProfileByCalculatedName(selectedFullName);

            if (getActivity() != null && profile != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    selectedProfileId = profile.id;

                    String derivedAddress = "";
                    if (profile.sitio != null && !profile.sitio.isEmpty()) derivedAddress += profile.sitio + ", ";
                    if (profile.barangay != null && !profile.barangay.isEmpty()) derivedAddress += profile.barangay;
                    if (profile.municipality != null && !profile.municipality.isEmpty()) derivedAddress +=", " + profile.municipality;
                    if (profile.region != null && !profile.region.isEmpty()) derivedAddress +=", " + profile.region;

                    etAddress.setText(derivedAddress.trim());
                    etBirthDate.setText(profile.dob != null ? profile.dob : "");
                    etFamilySerial.setText(profile.hhNumber != null ? profile.hhNumber : "");

                    if (profile.dob != null && !profile.dob.isEmpty()) {
                        calculateAndSetAge(profile.dob);
                    } else {
                        etAge.setText("");
                    }

                    etAddress.setError(null);
                    etBirthDate.setError(null);
                    etFamilySerial.setError(null);
                    etFullName.setError(null);
                });
            }
        });
    }

    private void calculateAndSetAge(String dateOfBirthStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(sdf.parse(dateOfBirthStr));

            Calendar todayCalendar = Calendar.getInstance();
            int computedAge = todayCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);

            if (todayCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                computedAge--;
            }

            if (computedAge >= 0) {
                etAge.setText(String.valueOf(computedAge));
                if (computedAge >= 10 && computedAge <= 14) spinnerAgeGroup.setSelection(0);
                else if (computedAge >= 15 && computedAge <= 19) spinnerAgeGroup.setSelection(1);
                else if (computedAge >= 20 && computedAge <= 49) spinnerAgeGroup.setSelection(2);
            }
        } catch (Exception ignored) {
            etAge.setText("");
        }
    }

    private void showDatePickerDialog(EditText targetField) {
        if (getContext() == null) return;

        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, (selectedMonth + 1), selectedDay);
                    targetField.setText(formattedDate);
                    targetField.setError(null);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupKeyboardFocusAutoScroll() {
        ViewCompat.setOnApplyWindowInsetsListener(fpScrollView, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int defaultPadding = (int) (16 * getResources().getDisplayMetrics().density);
            fpContainer.setPadding(defaultPadding, defaultPadding, defaultPadding, imeInsets.bottom + defaultPadding);
            return insets;
        });
    }

    private void saveFamilyPlanningRecord() {
        String registrationDate = spinnerRegDate.getText().toString().trim();
        String serialNum = etFamilySerial.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        if (registrationDate.isEmpty()) {
            spinnerRegDate.setError("Registration Date is required");
            return;
        }

        if (selectedProfileId == -1) {
            etFullName.setError("Please select a valid client from the dropdown list");
            etFullName.requestFocus();
            return;
        }

        FamilyPlanningRecord record = new FamilyPlanningRecord();

        // If editing, make sure Room tracks the same row identification key
        if (isEditMode) {
            record.id = existingRecordId;
        }
        String PREFS_NAME = "AppPrefs";
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        record.profileId = selectedProfileId;
        record.registrationDate = registrationDate;
        record.familySerialNumber = serialNum;
        record.address = address;
        record.age = ageStr.isEmpty() ? 0 : Integer.parseInt(ageStr);
        record.birthDate = birthDate;
        record.userId = userId;

        if (spinnerAgeGroup.getSelectedItem() != null) record.ageGroupCategory = spinnerAgeGroup.getSelectedItem().toString();
        if (spinnerClientType.getSelectedItem() != null) record.clientType = spinnerClientType.getSelectedItem().toString();
        if (spinnerSource.getSelectedItem() != null) record.commoditySource = spinnerSource.getSelectedItem().toString();
        if (spinnerPrevMethod.getSelectedItem() != null) record.previousMethod = spinnerPrevMethod.getSelectedItem().toString();
        if (spinnerClientMethodUsed.getSelectedItem() != null) record.methodUsed = spinnerClientMethodUsed.getSelectedItem().toString();

        persistFamilyPlanningData(record);
    }

    private void persistFamilyPlanningData(FamilyPlanningRecord record) {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());

            if (isEditMode) {
                db.familyPlanningDao().update(record);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Record updated successfully", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            } else {
                // Execute standard Room Insert option path
                long insertedId = db.familyPlanningDao().insertRecord(record);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (insertedId != -1) {
                            Toast.makeText(getActivity(), "Record saved successfully", Toast.LENGTH_SHORT).show();
                            resetFormFields();
                        } else {
                            Toast.makeText(getActivity(), "Failed to save record", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void resetFormFields() {
        selectedProfileId = -1;
        existingRecordId = -1;
        isEditMode = false;
        btnSubmitFP.setText("Save Client Record");
        setCurrentDateAsDefault();
        etFamilySerial.setText("");
        etFullName.setText("", false);
        etAddress.setText("");
        etAge.setText("");
        etBirthDate.setText("");

        spinnerAgeGroup.setSelection(0);
        spinnerClientType.setSelection(0);
        spinnerSource.setSelection(0);
        spinnerPrevMethod.setSelection(0);
        spinnerClientMethodUsed.setSelection(0); // FIX: Resets the client method used spinner

        if (fpScrollView != null) {
            fpScrollView.smoothScrollTo(0, 0);
        }
    }
}