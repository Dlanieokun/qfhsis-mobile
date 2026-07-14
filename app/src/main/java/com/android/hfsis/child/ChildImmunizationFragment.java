package com.android.hfsis.child;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.child.ChildImmunizationRecord;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;

public class ChildImmunizationFragment extends Fragment {

    // Record tracking properties
    private long selectedProfileId = -1;
    private long editingRecordId = -1;
    private boolean isEditMode = false;

    // Traditional Demographics UI Elements
    private EditText etRegDate, etFamilySerialNumber, etDob, etAgeMonths, etMotherName, etAddress;
    private AutoCompleteTextView etChildName;
    private RadioButton rbMale, rbFemale;
    private CheckBox cbTd2Mother, cbTd3To5Mother;

    // BCG
    private EditText etBcgWithin24hAge, etBcgWithin24hDate, etBcgLateAge, etBcgLateDate;

    // Hepatitis B
    private EditText etHepaBWithin24hAge, etHepaBWithin24hDate, etHepaBLateAge, etHepaBLateDate;

    // Pentavalent (DPT-HiB-HepB)
    private EditText etDpt1Age, etDpt1Date, etDpt2Age, etDpt2Date, etDpt3Age, etDpt3Date;

    // Oral Polio Vaccine (OPV)
    private EditText etOpv1Age, etOpv1Date, etOpv2Age, etOpv2Date, etOpv3Age, etOpv3Date;

    // Inactivated Polio Vaccine (IPV)
    private EditText etIpv1Age, etIpv1Date, etIpv2Age, etIpv2Date;

    // Pneumococcal Conjugate Vaccine (PCV)
    private EditText etPcv1Age, etPcv1Date, etPcv2Age, etPcv2Date, etPcv3Age, etPcv3Date;

    // Measles, Mumps, Rubella (MMR)
    private EditText etMmr1Age, etMmr1Date, etMmr2Age, etMmr2Date;

    // Completion Indicators Checkboxes
    private CheckBox cbFicBcg, cbFicDpt3, cbFicOpv3, cbFicMmr2;
    private EditText etFicDate;
    private CheckBox cbCicBcg, cbCicDpt3, cbCicOpv3, cbCicMmr2;
    private EditText etCicDate;

    private EditText etRemarks;
    private Button btnSave;

    private DatabaseHelper database;
    private List<HouseholdProfile> householdList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_immunization, container, false);
        database = DatabaseHelper.getInstance(getContext());
        initViews(view);
        setupDatePickers();

        // Load dataset suggestions and map item selection events
        loadHouseholdProfilesAutocomplete();

        // Check if loading an existing entry for edits
        if (getArguments() != null && getArguments().containsKey("EDIT_RECORD_ID")) {
            editingRecordId = getArguments().getLong("EDIT_RECORD_ID");
            isEditMode = true;
            loadRecordForEditing(editingRecordId);
        }else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etRegDate.setText(sdf.format(new Date()));
        }

        btnSave.setOnClickListener(v -> saveImmunizationRecord());
        return view;
    }

    private void initViews(View v) {
        etRegDate = v.findViewById(R.id.etRegDate);
        etFamilySerialNumber = v.findViewById(R.id.etFamilySerialNumber);
        etChildName = v.findViewById(R.id.etChildName);
        etDob = v.findViewById(R.id.etDob);
        etAgeMonths = v.findViewById(R.id.etAgeMonths);
        rbMale = v.findViewById(R.id.rbMale);
        rbFemale = v.findViewById(R.id.rbFemale);
        etMotherName = v.findViewById(R.id.etMotherName);
        etAddress = v.findViewById(R.id.etAddress);

        cbTd2Mother = v.findViewById(R.id.cbTd2Mother);
        cbTd3To5Mother = v.findViewById(R.id.cbTd3To5Mother);

        etBcgWithin24hAge = v.findViewById(R.id.etBcgWithin24hAge);
        etBcgWithin24hDate = v.findViewById(R.id.etBcgWithin24hDate);
        etBcgLateAge = v.findViewById(R.id.etBcgLateAge);
        etBcgLateDate = v.findViewById(R.id.etBcgLateDate);

        etHepaBWithin24hAge = v.findViewById(R.id.etHepaBWithin24hAge);
        etHepaBWithin24hDate = v.findViewById(R.id.etHepaBWithin24hDate);
        etHepaBLateAge = v.findViewById(R.id.etHepaBLateAge);
        etHepaBLateDate = v.findViewById(R.id.etHepaBLateDate);

        etDpt1Age = v.findViewById(R.id.etDpt1Age);
        etDpt1Date = v.findViewById(R.id.etDpt1Date);
        etDpt2Age = v.findViewById(R.id.etDpt2Age);
        etDpt2Date = v.findViewById(R.id.etDpt2Date);
        etDpt3Age = v.findViewById(R.id.etDpt3Age);
        etDpt3Date = v.findViewById(R.id.etDpt3Date);

        etOpv1Age = v.findViewById(R.id.etOpv1Age);
        etOpv1Date = v.findViewById(R.id.etOpv1Date);
        etOpv2Age = v.findViewById(R.id.etOpv2Age);
        etOpv2Date = v.findViewById(R.id.etOpv2Date);
        etOpv3Age = v.findViewById(R.id.etOpv3Age);
        etOpv3Date = v.findViewById(R.id.etOpv3Date);

        etIpv1Age = v.findViewById(R.id.etIpv1Age);
        etIpv1Date = v.findViewById(R.id.etIpv1Date);
        etIpv2Age = v.findViewById(R.id.etIpv2Age);
        etIpv2Date = v.findViewById(R.id.etIpv2Date);

        etPcv1Age = v.findViewById(R.id.etPcv1Age);
        etPcv1Date = v.findViewById(R.id.etPcv1Date);
        etPcv2Age = v.findViewById(R.id.etPcv2Age);
        etPcv2Date = v.findViewById(R.id.etPcv2Date);
        etPcv3Age = v.findViewById(R.id.etPcv3Age);
        etPcv3Date = v.findViewById(R.id.etPcv3Date);

        etMmr1Age = v.findViewById(R.id.etMmr1Age);
        etMmr1Date = v.findViewById(R.id.etMmr1Date);
        etMmr2Age = v.findViewById(R.id.etMmr2Age);
        etMmr2Date = v.findViewById(R.id.etMmr2Date);

        cbFicBcg = v.findViewById(R.id.cbFicBcg);
        cbFicDpt3 = v.findViewById(R.id.cbFicDpt3);
        cbFicOpv3 = v.findViewById(R.id.cbFicOpv3);
        cbFicMmr2 = v.findViewById(R.id.cbFicMmr2);
        etFicDate = v.findViewById(R.id.etFicDate);

        cbCicBcg = v.findViewById(R.id.cbCicBcg);
        cbCicDpt3 = v.findViewById(R.id.cbCicDpt3);
        cbCicOpv3 = v.findViewById(R.id.cbCicOpv3);
        cbCicMmr2 = v.findViewById(R.id.cbCicMmr2);
        etCicDate = v.findViewById(R.id.etCicDate);

        etRemarks = v.findViewById(R.id.etRemarks);
        btnSave = v.findViewById(R.id.btnSave);
    }

    private void setupDatePickers() {
        bindDatePickerAndWatcher(etRegDate);
        bindDatePickerAndWatcher(etDob);
        bindDatePickerAndWatcher(etBcgWithin24hDate);
        bindDatePickerAndWatcher(etBcgLateDate);
        bindDatePickerAndWatcher(etHepaBWithin24hDate);
        bindDatePickerAndWatcher(etHepaBLateDate);
        bindDatePickerAndWatcher(etDpt1Date);
        bindDatePickerAndWatcher(etDpt2Date);
        bindDatePickerAndWatcher(etDpt3Date);
        bindDatePickerAndWatcher(etOpv1Date);
        bindDatePickerAndWatcher(etOpv2Date);
        bindDatePickerAndWatcher(etOpv3Date);
        bindDatePickerAndWatcher(etIpv1Date);
        bindDatePickerAndWatcher(etIpv2Date);
        bindDatePickerAndWatcher(etPcv1Date);
        bindDatePickerAndWatcher(etPcv2Date);
        bindDatePickerAndWatcher(etPcv3Date);
        bindDatePickerAndWatcher(etMmr1Date);
        bindDatePickerAndWatcher(etMmr2Date);
        bindDatePickerAndWatcher(etFicDate);
        bindDatePickerAndWatcher(etCicDate);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void bindDatePickerAndWatcher(EditText editText) {
        if (editText == null) return;

        // Formats keyboard inputs automatically while typing manually
        editText.addTextChangedListener(new DateFormattingWatcher(editText));

        // Tracks touch specifically on the end calendar icon (drawableRight / drawableEnd)
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Check if touch is within the bounds of the end drawable icon
                if (editText.getCompoundDrawables()[2] != null) {
                    int iconWidth = editText.getCompoundDrawables()[2].getBounds().width();
                    if (event.getRawX() >= (editText.getRight() - iconWidth - editText.getPaddingEnd())) {

                        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                                .setTitleText("Select Date")
                                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                                .build();

                        datePicker.addOnPositiveButtonClickListener(selection -> {
                            // FIX: Added UTC bounds synchronization to stop local shifts from changing the date day selection
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String formattedDate = sdf.format(new Date(selection));
                            editText.setText(formattedDate);

                            if (editText.getId() == R.id.etDob) {
                                calculateAgeInMonths(formattedDate);
                            }
                        });

                        datePicker.show(getParentFragmentManager(), "DATE_PICKER_" + editText.getId());
                        return true; // Consume touch event so field doesn't gain text focus immediately
                    }
                }
            }
            return false;
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
                                        calculateAgeInMonths(profile.dob);
                                    }

//                                    if (etAddress != null) {
//                                        String sitioPart = (profile.sitio != null && !profile.sitio.isEmpty()) ? profile.sitio + ", " : "";
//                                        String brgyPart = (profile.barangay != null) ? profile.barangay : "";
//                                        etAddress.setText(sitioPart + brgyPart);
//                                    }
                                    StringBuilder fullAddress = new StringBuilder();
                                    if (profile.sitio != null && !profile.sitio.trim().isEmpty()) {
                                        fullAddress.append(profile.sitio.trim());
                                    }
                                    if (profile.barangay != null && !profile.barangay.trim().isEmpty()) {
                                        fullAddress.append(", ").append(profile.barangay.trim());
                                    }
                                    if (profile.municipality != null && !profile.municipality.trim().isEmpty()) {
                                        fullAddress.append(", ").append(profile.municipality.trim());
                                    }
                                    if (profile.province != null && !profile.province.trim().isEmpty()) {
                                        fullAddress.append(", ").append(profile.province.trim());
                                    }
                                    if (profile.region != null && !profile.region.trim().isEmpty()) {
                                        fullAddress.append(", ").append(profile.region.trim());
                                    }
                                    etAddress.setText(fullAddress);

                                    if (rbMale != null && rbFemale != null) {
                                        if ("Male".equalsIgnoreCase(profile.sex)) {
                                            rbMale.setChecked(true);
                                        } else if ("Female".equalsIgnoreCase(profile.sex)) {
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

    private void calculateAgeInMonths(String dobString) {
        if (dobString == null || dobString.isEmpty() || etAgeMonths == null) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthDate = sdf.parse(dobString);
            if (birthDate != null) {
                Calendar birth = Calendar.getInstance();
                birth.setTime(birthDate);
                Calendar today = Calendar.getInstance();

                int months = (today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)) * 12
                        + (today.get(Calendar.MONTH) - birth.get(Calendar.MONTH));

                etAgeMonths.setText(String.valueOf(Math.max(0, months)));
            }
        } catch (Exception ignored) {}
    }

    private void loadRecordForEditing(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (database != null) {
                ChildImmunizationRecord record = database.childImmunizationDao().getRecordById(id);
                if (record != null && isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        selectedProfileId = record.getProfileId();

                        if (etRegDate != null) etRegDate.setText(record.getRegistrationDate());
                        if (etFamilySerialNumber != null) etFamilySerialNumber.setText(record.getFamilySerialNumber());
                        if (etChildName != null) etChildName.setText(record.getChildName());
                        if (etDob != null) etDob.setText(record.getDateOfBirth());
                        if (etAgeMonths != null) etAgeMonths.setText(record.getAgeMonths());
                        if (etMotherName != null) etMotherName.setText(record.getMotherName());
                        if (etAddress != null) etAddress.setText(record.getAddress());

                        if (rbMale != null && rbFemale != null) {
                            if ("Male".equalsIgnoreCase(record.getSex())) rbMale.setChecked(true);
                            else if ("Female".equalsIgnoreCase(record.getSex())) rbFemale.setChecked(true);
                        }

                        if (cbTd2Mother != null) cbTd2Mother.setChecked(record.isTd2Mother());
                        if (cbTd3To5Mother != null) cbTd3To5Mother.setChecked(record.isTd3To5Mother());

                        if (etBcgWithin24hAge != null) etBcgWithin24hAge.setText(record.getBcgWithin24hAge());
                        if (etBcgWithin24hDate != null) etBcgWithin24hDate.setText(record.getBcgWithin24hDate());
                        if (etBcgLateAge != null) etBcgLateAge.setText(record.getBcgLateAge());
                        if (etBcgLateDate != null) etBcgLateDate.setText(record.getBcgLateDate());

                        if (etHepaBWithin24hAge != null) etHepaBWithin24hAge.setText(record.getHepaBWithin24hAge());
                        if (etHepaBWithin24hDate != null) etHepaBWithin24hDate.setText(record.getHepaBWithin24hDate());
                        if (etHepaBLateAge != null) etHepaBLateAge.setText(record.getHepaBLateAge());
                        if (etHepaBLateDate != null) etHepaBLateDate.setText(record.getHepaBLateDate());

                        if (etDpt1Age != null) etDpt1Age.setText(record.getDpt1Age());
                        if (etDpt1Date != null) etDpt1Date.setText(record.getDpt1Date());
                        if (etDpt2Age != null) etDpt2Age.setText(record.getDpt2Age());
                        if (etDpt2Date != null) etDpt2Date.setText(record.getDpt2Date());
                        if (etDpt3Age != null) etDpt3Age.setText(record.getDpt3Age());
                        if (etDpt3Date != null) etDpt3Date.setText(record.getDpt3Date());

                        if (etOpv1Age != null) etOpv1Age.setText(record.getOpv1Age());
                        if (etOpv1Date != null) etOpv1Date.setText(record.getOpv1Date());
                        if (etOpv2Age != null) etOpv2Age.setText(record.getOpv2Age());
                        if (etOpv2Date != null) etOpv2Date.setText(record.getOpv2Date());
                        if (etOpv3Age != null) etOpv3Age.setText(record.getOpv3Age());
                        if (etOpv3Date != null) etOpv3Date.setText(record.getOpv3Date());

                        if (etIpv1Age != null) etIpv1Age.setText(record.getIpv1Age());
                        if (etIpv1Date != null) etIpv1Date.setText(record.getIpv1Date());
                        if (etIpv2Age != null) etIpv2Age.setText(record.getIpv2Age());
                        if (etIpv2Date != null) etIpv2Date.setText(record.getIpv2Date());

                        if (etPcv1Age != null) etPcv1Age.setText(record.getPcv1Age());
                        if (etPcv1Date != null) etPcv1Date.setText(record.getPcv1Date());
                        if (etPcv2Age != null) etPcv2Age.setText(record.getPcv2Age());
                        if (etPcv2Date != null) etPcv2Date.setText(record.getPcv2Date());
                        if (etPcv3Age != null) etPcv3Age.setText(record.getPcv3Age());
                        if (etPcv3Date != null) etPcv3Date.setText(record.getPcv3Date());

                        if (etMmr1Age != null) etMmr1Age.setText(record.getMmr1Age());
                        if (etMmr1Date != null) etMmr1Date.setText(record.getMmr1Date());
                        if (etMmr2Age != null) etMmr2Age.setText(record.getMmr2Age());
                        if (etMmr2Date != null) etMmr2Date.setText(record.getMmr2Date());

                        if (cbFicBcg != null) cbFicBcg.setChecked(record.isFicBcg());
                        if (cbFicDpt3 != null) cbFicDpt3.setChecked(record.isFicDpt3());
                        if (cbFicOpv3 != null) cbFicOpv3.setChecked(record.isFicOpv3());
                        if (cbFicMmr2 != null) cbFicMmr2.setChecked(record.isFicMmr2());
                        if (etFicDate != null) etFicDate.setText(record.getFicDate());

                        if (cbCicBcg != null) cbCicBcg.setChecked(record.isCicBcg());
                        if (cbCicDpt3 != null) cbCicDpt3.setChecked(record.isCicDpt3());
                        if (cbCicOpv3 != null) cbCicOpv3.setChecked(record.isCicOpv3());
                        if (cbCicMmr2 != null) cbCicMmr2.setChecked(record.isCicMmr2());
                        if (etCicDate != null) etCicDate.setText(record.getCicDate());

                        if (etRemarks != null) etRemarks.setText(record.getRemarks());
                    });
                }
            }
        });
    }

    private void saveImmunizationRecord() {
        String childNameStr = getTextOrEmpty(etChildName);
        if (childNameStr.isEmpty()) {
            Toast.makeText(getContext(), "Child Full Name is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            ChildImmunizationRecord record = new ChildImmunizationRecord();
            if (isEditMode) {
                record.setId(editingRecordId);
            }

            String PREFS_NAME = "AppPrefs";
            SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            record.userId = userId;

            record.setProfileId(selectedProfileId);
            record.setRegistrationDate(getTextOrEmpty(etRegDate));
            record.setFamilySerialNumber(getTextOrEmpty(etFamilySerialNumber));
            record.setChildName(childNameStr);
            record.setDateOfBirth(getTextOrEmpty(etDob));
            record.setAgeMonths(getTextOrEmpty(etAgeMonths));

            String sexStr = "";
            if (rbMale != null && rbMale.isChecked()) sexStr = "Male";
            else if (rbFemale != null && rbFemale.isChecked()) sexStr = "Female";
            record.setSex(sexStr);

            record.setMotherName(getTextOrEmpty(etMotherName));
            record.setAddress(getTextOrEmpty(etAddress));

            record.setTd2Mother(cbTd2Mother != null && cbTd2Mother.isChecked());
            record.setTd3To5Mother(cbTd3To5Mother != null && cbTd3To5Mother.isChecked());

            record.setBcgWithin24hAge(getTextOrEmpty(etBcgWithin24hAge));
            record.setBcgWithin24hDate(getTextOrEmpty(etBcgWithin24hDate));
            record.setBcgLateAge(getTextOrEmpty(etBcgLateAge));
            record.setBcgLateDate(getTextOrEmpty(etBcgLateDate));

            record.setHepaBWithin24hAge(getTextOrEmpty(etHepaBWithin24hAge));
            record.setHepaBWithin24hDate(getTextOrEmpty(etHepaBWithin24hDate));
            record.setHepaBLateAge(getTextOrEmpty(etHepaBLateAge));
            record.setHepaBLateDate(getTextOrEmpty(etHepaBLateDate));

            record.setDpt1Age(getTextOrEmpty(etDpt1Age)); record.setDpt1Date(getTextOrEmpty(etDpt1Date));
            record.setDpt2Age(getTextOrEmpty(etDpt2Age)); record.setDpt2Date(getTextOrEmpty(etDpt2Date));
            record.setDpt3Age(getTextOrEmpty(etDpt3Age)); record.setDpt3Date(getTextOrEmpty(etDpt3Date));

            record.setOpv1Age(getTextOrEmpty(etOpv1Age)); record.setOpv1Date(getTextOrEmpty(etOpv1Date));
            record.setOpv2Age(getTextOrEmpty(etOpv2Age)); record.setOpv2Date(getTextOrEmpty(etOpv2Date));
            record.setOpv3Age(getTextOrEmpty(etOpv3Age)); record.setOpv3Date(getTextOrEmpty(etOpv3Date));

            record.setIpv1Age(getTextOrEmpty(etIpv1Age)); record.setIpv1Date(getTextOrEmpty(etIpv1Date));
            record.setIpv2Age(getTextOrEmpty(etIpv2Age)); record.setIpv2Date(getTextOrEmpty(etIpv2Date));

            record.setPcv1Age(getTextOrEmpty(etPcv1Age)); record.setPcv1Date(getTextOrEmpty(etPcv1Date));
            record.setPcv2Age(getTextOrEmpty(etPcv2Age)); record.setPcv2Date(getTextOrEmpty(etPcv2Date));
            record.setPcv3Age(getTextOrEmpty(etPcv3Age)); record.setPcv3Date(getTextOrEmpty(etPcv3Date));

            record.setMmr1Age(getTextOrEmpty(etMmr1Age)); record.setMmr1Date(getTextOrEmpty(etMmr1Date));
            record.setMmr2Age(getTextOrEmpty(etMmr2Age)); record.setMmr2Date(getTextOrEmpty(etMmr2Date));

            record.setFicBcg(cbFicBcg != null && cbFicBcg.isChecked());
            record.setFicDpt3(cbFicDpt3 != null && cbFicDpt3.isChecked());
            record.setFicOpv3(cbFicOpv3 != null && cbFicOpv3.isChecked());
            record.setFicMmr2(cbFicMmr2 != null && cbFicMmr2.isChecked());
            record.setFicDate(getTextOrEmpty(etFicDate));

            record.setCicBcg(cbCicBcg != null && cbCicBcg.isChecked());
            record.setCicDpt3(cbCicDpt3 != null && cbCicDpt3.isChecked());
            record.setCicOpv3(cbCicOpv3 != null && cbCicOpv3.isChecked());
            record.setCicMmr2(cbCicMmr2 != null && cbCicMmr2.isChecked());
            record.setCicDate(getTextOrEmpty(etCicDate));

            record.setRemarks(getTextOrEmpty(etRemarks));

            if (database != null) {
                if (isEditMode) {
                    database.childImmunizationDao().update(record);
                } else {
                    database.childImmunizationDao().insert(record);
                }

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), isEditMode ? "Record updated successfully!" : "Record saved successfully!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            }
        });
    }

    private String getTextOrEmpty(EditText et) {
        return (et != null && et.getText() != null) ? et.getText().toString().trim() : "";
    }

    // Static nested class to format user text input to standard ISO date structure (yyyy-MM-dd)
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
                if (s.length() == 5 || s.length() == 8) {
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
                if ((i == 3 && len > 4) || (i == 5 && len > 6)) {
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