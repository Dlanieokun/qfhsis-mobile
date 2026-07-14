package com.android.hfsis.child;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.child.ChildImmunizationSchoolRecord;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ChildImmunizationSchoolFragment extends Fragment {

    // Demographics UI Elements
    private EditText etRegDate, etFamilySerialNumber, etDob, etAgeYears, etAddress;
    private AutoCompleteTextView etChildName;
    private RadioGroup rgSex, rgGradeLevel, rgHpvCompleted;
    private RadioButton rbMale, rbFemale, rbHpvCompletedNo, rbHpvCompletedYes;
    private RadioButton rbGradeA, rbGradeB, rbGradeC, rbGradeD;

    // Immunization Dates & System Actions Elements
    private EditText etTdDate, etMrDate, etHpv1SbiDate, etHpv1CbiDate, etHpv2CbiDate, etHpvCompletedDate, etRemarks;
    private TextInputLayout tilRegDate, tilDob, tilTdDate, tilMrDate, tilHpv1SbiDate, tilHpv1CbiDate, tilHpv2CbiDate, tilHpvCompletedDate;
    private Button btnSave;

    // Architecture & Database State variables
    private DatabaseHelper database;
    private List<HouseholdProfile> householdList = new ArrayList<>();
    private long selectedProfileId = -1;
    private long editingRecordId = -1;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_immunization_school, container, false);

        database = DatabaseHelper.getInstance(getContext());

        if (getArguments() != null && getArguments().containsKey("EDIT_RECORD_ID")) {
            editingRecordId = getArguments().getLong("EDIT_RECORD_ID");
            isEditMode = true;
        }

        initViews(view);
        setupDatePickers();
        loadHouseholdProfilesAutocomplete();

        if (isEditMode) {
            loadExistingRecordDetails();
        }else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etRegDate.setText(sdf.format(new Date()));
        }

        btnSave.setOnClickListener(v -> saveRecord());
        return view;
    }

    private void initViews(View v) {
        etRegDate = v.findViewById(R.id.etRegDate);
        tilRegDate = v.findViewById(R.id.tilRegDate);
        etFamilySerialNumber = v.findViewById(R.id.etFamilySerialNumber);
        etChildName = v.findViewById(R.id.etChildName);
        etDob = v.findViewById(R.id.etDob);
        tilDob = v.findViewById(R.id.tilDob);
        etAgeYears = v.findViewById(R.id.etAgeYears);
        etAddress = v.findViewById(R.id.etAddress);

        rgSex = v.findViewById(R.id.rgSex);
        rbMale = v.findViewById(R.id.rbMale);
        rbFemale = v.findViewById(R.id.rbFemale);

        rgGradeLevel = v.findViewById(R.id.rgGradeLevel);
        rbGradeA = v.findViewById(R.id.rbGradeA);
        rbGradeB = v.findViewById(R.id.rbGradeB);
        rbGradeC = v.findViewById(R.id.rbGradeC);
        rbGradeD = v.findViewById(R.id.rbGradeD);

        rgHpvCompleted = v.findViewById(R.id.rgHpvCompleted);
        rbHpvCompletedNo = v.findViewById(R.id.rbHpvCompletedNo);
        rbHpvCompletedYes = v.findViewById(R.id.rbHpvCompletedYes);

        etTdDate = v.findViewById(R.id.etTdDate);
        tilTdDate = v.findViewById(R.id.tilTdDate);
        etMrDate = v.findViewById(R.id.etMrDate);
        tilMrDate = v.findViewById(R.id.tilMrDate);
        etHpv1SbiDate = v.findViewById(R.id.etHpv1SbiDate);
        tilHpv1SbiDate = v.findViewById(R.id.tilHpv1SbiDate);
        etHpv1CbiDate = v.findViewById(R.id.etHpv1CbiDate);
        tilHpv1CbiDate = v.findViewById(R.id.tilHpv1CbiDate);
        etHpv2CbiDate = v.findViewById(R.id.etHpv2CbiDate);
        tilHpv2CbiDate = v.findViewById(R.id.tilHpv2CbiDate);
        etHpvCompletedDate = v.findViewById(R.id.etHpvCompletedDate);
        tilHpvCompletedDate = v.findViewById(R.id.tilHpvCompletedDate);
        etRemarks = v.findViewById(R.id.etRemarks);
        btnSave = v.findViewById(R.id.btnSave);

        if (isEditMode) {
            btnSave.setText("Update Record");
        }

        rgHpvCompleted.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbHpvCompletedYes) {
                tilHpvCompletedDate.setVisibility(View.VISIBLE);
            } else {
                tilHpvCompletedDate.setVisibility(View.GONE);
                etHpvCompletedDate.setText("");
            }
        });
    }

    private void setupDatePickers() {
        bindDatePicker(tilRegDate, etRegDate);
        bindDatePicker(tilDob, etDob);
        bindDatePicker(tilTdDate, etTdDate);
        bindDatePicker(tilMrDate, etMrDate);
        bindDatePicker(tilHpv1SbiDate, etHpv1SbiDate);
        bindDatePicker(tilHpv1CbiDate, etHpv1CbiDate);
        bindDatePicker(tilHpv2CbiDate, etHpv2CbiDate);
        bindDatePicker(tilHpvCompletedDate, etHpvCompletedDate);
    }

    private void bindDatePicker(TextInputLayout layout, EditText editText) {
        if (layout == null || editText == null) return;
        layout.setEndIconOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = sdf.format(new Date(selection));
                editText.setText(formattedDate);

                if (editText.getId() == R.id.etDob) {
                    calculateAgeInYears(formattedDate);
                }
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER_" + editText.getId());
        });
    }

    private void loadHouseholdProfilesAutocomplete() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (database != null) {
                List<HouseholdProfile> profiles = database.householdProfileDao().getAllProfiles();
                if (profiles != null && isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        householdList = profiles;
                        List<String> namesList = new ArrayList<>();

                        for (HouseholdProfile p : profiles) {
                            String mid = (p.memberMiddleName != null && !p.memberMiddleName.trim().isEmpty()) ? " " + p.memberMiddleName.trim() : "";
                            String fullName = p.memberFirstName.trim() + mid + " " + p.memberLastName.trim();
                            namesList.add(fullName);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                namesList
                        );
                        etChildName.setAdapter(adapter);

                        etChildName.setOnItemClickListener((parent, view, position, id) -> {
                            String selectedName = (String) parent.getItemAtPosition(position);

                            for (int i = 0; i < namesList.size(); i++) {
                                if (namesList.get(i).equals(selectedName)) {
                                    HouseholdProfile profile = householdList.get(i);
                                    selectedProfileId = profile.id;

                                    if (etFamilySerialNumber != null) {
                                        etFamilySerialNumber.setText(profile.hhNumber);
                                    }
                                    if (etDob != null) {
                                        etDob.setText(profile.dob);
                                        calculateAgeInYears(profile.dob);
                                    }
                                    if (etAddress != null) {
                                        String sitioPart = (profile.sitio != null && !profile.sitio.isEmpty()) ? profile.sitio : "";
                                        String brgyPart = (profile.barangay != null) ? ", " + profile.barangay : "";
                                        String munPart = (profile.municipality != null) ? ", " + profile.municipality : "";
                                        String provPart = (profile.province != null) ? ", " + profile.province : "";
                                        String regPart = (profile.region != null) ? ", " + profile.region : "";
                                        etAddress.setText(sitioPart + brgyPart + munPart + provPart + regPart);
                                    }
                                    if (rgSex != null) {
                                        if ("Male".equalsIgnoreCase(profile.sex) && rbMale != null) {
                                            rbMale.setChecked(true);
                                        } else if ("Female".equalsIgnoreCase(profile.sex) && rbFemale != null) {
                                            rbFemale.setChecked(true);
                                        }
                                    }
                                    break;
                                }
                            }
                        });
                    });
                }
            }
        });
    }

    private void loadExistingRecordDetails() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (database != null) {
                ChildImmunizationSchoolRecord record = database.childImmunizationSchoolDao().getRecordById(editingRecordId);
                if (record != null && isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        selectedProfileId = record.getProfileId();
                        etRegDate.setText(record.getRegistrationDate());
                        etFamilySerialNumber.setText(record.getFamilySerialNumber());
                        etChildName.setText(record.getChildName());
                        etDob.setText(record.getDateOfBirth());
                        etAgeYears.setText(record.getAgeYears());
                        etAddress.setText(record.getAddress());

                        if ("Male".equalsIgnoreCase(record.getSex())) {
                            rbMale.setChecked(true);
                        } else if ("Female".equalsIgnoreCase(record.getSex())) {
                            rbFemale.setChecked(true);
                        }

                        String grade = record.getGradeLevel();
                        if ("A".equals(grade)) rbGradeA.setChecked(true);
                        else if ("B".equals(grade)) rbGradeB.setChecked(true);
                        else if ("C".equals(grade)) rbGradeC.setChecked(true);
                        else if ("D".equals(grade)) rbGradeD.setChecked(true);

                        etTdDate.setText(record.getTdDate());
                        etMrDate.setText(record.getMrDate());
                        etHpv1SbiDate.setText(record.getHpv1SbiDate());
                        etHpv1CbiDate.setText(record.getHpv1CbiDate());
                        etHpv2CbiDate.setText(record.getHpv2CbiDate());

                        if (record.getHpvCompleted() == 1) {
                            rbHpvCompletedYes.setChecked(true);
                            tilHpvCompletedDate.setVisibility(View.VISIBLE);
                            etHpvCompletedDate.setText(record.getHpvCompletedDate());
                        } else {
                            rbHpvCompletedNo.setChecked(true);
                            tilHpvCompletedDate.setVisibility(View.GONE);
                        }

                        etRemarks.setText(record.getRemarks());
                    });
                }
            }
        });
    }

    private void calculateAgeInYears(String dobString) {
        if (dobString == null || dobString.isEmpty() || etAgeYears == null) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthDate = sdf.parse(dobString);
            if (birthDate != null) {
                Calendar birth = Calendar.getInstance();
                birth.setTime(birthDate);
                Calendar today = Calendar.getInstance();

                int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }
                etAgeYears.setText(String.valueOf(Math.max(0, age)));
            }
        } catch (Exception ignored) {}
    }

    private void saveRecord() {
        String childName = etChildName.getText().toString().trim();
        if (childName.isEmpty()) {
            Toast.makeText(getContext(), "Child Full Name is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            ChildImmunizationSchoolRecord record;
            if (isEditMode) {
                record = database.childImmunizationSchoolDao().getRecordById(editingRecordId);
            } else {
                record = new ChildImmunizationSchoolRecord();
            }

            if (record != null) {
                String PREFS_NAME = "AppPrefs";
                SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);
                record.userId = userId;
                record.setProfileId(selectedProfileId);
                record.setRegistrationDate(etRegDate.getText().toString());
                record.setFamilySerialNumber(etFamilySerialNumber.getText().toString());
                record.setChildName(childName);
                record.setDateOfBirth(etDob.getText().toString());
                record.setAgeYears(etAgeYears.getText().toString());
                record.setAddress(etAddress.getText().toString());

                String sex = "";
                if (rbMale.isChecked()) sex = "Male";
                else if (rbFemale.isChecked()) sex = "Female";
                record.setSex(sex);

                String gradeLevel = "";
                int gradId = rgGradeLevel.getCheckedRadioButtonId();
                if (gradId == R.id.rbGradeA) gradeLevel = "A";
                else if (gradId == R.id.rbGradeB) gradeLevel = "B";
                else if (gradId == R.id.rbGradeC) gradeLevel = "C";
                else if (gradId == R.id.rbGradeD) gradeLevel = "D";
                record.setGradeLevel(gradeLevel);

                record.setTdDate(etTdDate.getText().toString());
                record.setMrDate(etMrDate.getText().toString());
                record.setHpv1SbiDate(etHpv1SbiDate.getText().toString());
                record.setHpv1CbiDate(etHpv1CbiDate.getText().toString());
                record.setHpv2CbiDate(etHpv2CbiDate.getText().toString());

                int hpvCompleted = 0;
                String hpvCompletedDate = "";
                if (rbHpvCompletedYes.isChecked()) {
                    hpvCompleted = 1;
                    hpvCompletedDate = etHpvCompletedDate.getText().toString();
                }
                record.setHpvCompleted(hpvCompleted);
                record.setHpvCompletedDate(hpvCompletedDate);
                record.setRemarks(etRemarks.getText().toString());

                if (database != null) {
                    if (isEditMode) {
                        database.childImmunizationSchoolDao().update(record);
                    } else {
                        database.childImmunizationSchoolDao().insert(record);
                    }

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), isEditMode ? "Record updated successfully!" : "Record saved successfully!", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        });
                    }
                }
            }
        });
    }
}