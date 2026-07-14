package com.android.hfsis.idpcs.rabies;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.idpcs.rabies.RabiesRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class RabiesFragment extends Fragment {

    private static final String DATE_PATTERN = "MM/dd/yy";
    private static final String TIME_PATTERN = "hh:mm a";

    // Patient Information
    private AutoCompleteTextView etName; // AutoCompleteTextView for patient name
    private EditText etAge;
    private RadioGroup rgSex;
    private Spinner spinnerCivilStatus;
    private EditText etAddress;
    private EditText etBirthdate;
    private EditText etBirthPlace;
    private EditText etContactNo;
    private EditText etPhilhealthNo;
    private EditText etWeight;
    private EditText etBloodPressure;

    // History of Exposure
    private EditText etDateOfBite;
    private EditText etTimeOfBite;
    private EditText etPlaceOfBite;

    // Nature of Injury
    private CheckBox cbInjuryScratch;
    private CheckBox cbInjuryAbrasion;
    private CheckBox cbInjuryLaceration;
    private CheckBox cbInjuryPunctured;
    private CheckBox cbInjuryAvulsed;
    private CheckBox cbInjuryOthers;
    private EditText etInjuryOthersSpecify;

    // Wound & Animal Information
    private RadioGroup rgWoundStatus;
    private RadioGroup rgWoundWashing;
    private RadioGroup rgBitingAnimal;
    private EditText etBitingAnimalOthersSpecify;
    private RadioGroup rgOwnershipStatus;
    private RadioGroup rgAnimalStatusAtBite;
    private RadioGroup rgAnimalStatusAtConsult;
    private EditText etAnimalDiedDate;
    private RadioGroup rgAnimalVaccination;
    private EditText etAnimalVaccinationDate;

    // Patient's Condition to Consider
    private CheckBox cbConditionEpilepsy;
    private CheckBox cbConditionDm;
    private CheckBox cbConditionHypertension;
    private CheckBox cbConditionAsthma;
    private CheckBox cbConditionAlcoholic;
    private CheckBox cbConditionEggAllergy;

    // Medical Treatment: PVRV
    private EditText etPvrvDay0Date;
    private EditText etPvrvDay0Batch;
    private EditText etPvrvDay3Date;
    private EditText etPvrvDay3Batch;
    private EditText etPvrvDay7Date;
    private EditText etPvrvDay7Batch;
    private EditText etPvrvDay28Date;
    private EditText etPvrvDay28Batch;
    private EditText etPvrvOutcome;

    // Medical Treatment: PCEV
    private EditText etPcevDay0Date;
    private EditText etPcevDay0Batch;
    private EditText etPcevDay3Date;
    private EditText etPcevDay3Batch;
    private EditText etPcevDay7Date;
    private EditText etPcevDay7Batch;
    private EditText etPcevDay28Date;
    private EditText etPcevDay28Batch;
    private EditText etPcevOutcome;

    // Additional Treatment & Impression
    private EditText etErig;
    private EditText etHrig;
    private EditText etTetanusToxoidDate;
    private EditText etAtsDose;
    private EditText etAtsDate;
    private EditText etImpression;

    private Button btnSave;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.US);

    private long currentRecordId = 0;
    private long selectedProfileId = -1; // ---> ADDED VARIABLE TO TRACK PROFILE ID
    private DatabaseHelper database;

    public RabiesFragment() {
        // Required empty public constructor
    }

    public static RabiesFragment newInstance() {
        return new RabiesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = DatabaseHelper.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rabies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupCivilStatusSpinner();
        setupDatePickers();
        setupTimePicker();
        setupNameAutocomplete();
        setupSaveButton();

        // Detect Edit Mode Bundle
        if (getArguments() != null && getArguments().containsKey("EDIT_RECORD_ID")) {
            currentRecordId = getArguments().getLong("EDIT_RECORD_ID");
            loadRecordForEditing(currentRecordId);
        }
    }

    private void bindViews(View view) {
        etName = view.findViewById(R.id.etName);
        etAge = view.findViewById(R.id.etAge);
        rgSex = view.findViewById(R.id.rgSex);
        spinnerCivilStatus = view.findViewById(R.id.spinnerCivilStatus);
        etAddress = view.findViewById(R.id.etAddress);
        etBirthdate = view.findViewById(R.id.etBirthdate);
        etBirthPlace = view.findViewById(R.id.etBirthPlace);
        etContactNo = view.findViewById(R.id.etContactNo);
        etPhilhealthNo = view.findViewById(R.id.etPhilhealthNo);
        etWeight = view.findViewById(R.id.etWeight);
        etBloodPressure = view.findViewById(R.id.etBloodPressure);

        etDateOfBite = view.findViewById(R.id.etDateOfBite);
        etTimeOfBite = view.findViewById(R.id.etTimeOfBite);
        etPlaceOfBite = view.findViewById(R.id.etPlaceOfBite);

        cbInjuryScratch = view.findViewById(R.id.cbInjuryScratch);
        cbInjuryAbrasion = view.findViewById(R.id.cbInjuryAbrasion);
        cbInjuryLaceration = view.findViewById(R.id.cbInjuryLaceration);
        cbInjuryPunctured = view.findViewById(R.id.cbInjuryPunctured);
        cbInjuryAvulsed = view.findViewById(R.id.cbInjuryAvulsed);
        cbInjuryOthers = view.findViewById(R.id.cbInjuryOthers);
        etInjuryOthersSpecify = view.findViewById(R.id.etInjuryOthersSpecify);

        rgWoundStatus = view.findViewById(R.id.rgWoundStatus);
        rgWoundWashing = view.findViewById(R.id.rgWoundWashing);
        rgBitingAnimal = view.findViewById(R.id.rgBitingAnimal);
        etBitingAnimalOthersSpecify = view.findViewById(R.id.etBitingAnimalOthersSpecify);
        rgOwnershipStatus = view.findViewById(R.id.rgOwnershipStatus);
        rgAnimalStatusAtBite = view.findViewById(R.id.rgAnimalStatusAtBite);
        rgAnimalStatusAtConsult = view.findViewById(R.id.rgAnimalStatusAtConsult);
        etAnimalDiedDate = view.findViewById(R.id.etAnimalDiedDate);
        rgAnimalVaccination = view.findViewById(R.id.rgAnimalVaccination);
        etAnimalVaccinationDate = view.findViewById(R.id.etAnimalVaccinationDate);

        cbConditionEpilepsy = view.findViewById(R.id.cbConditionEpilepsy);
        cbConditionDm = view.findViewById(R.id.cbConditionDm);
        cbConditionHypertension = view.findViewById(R.id.cbConditionHypertension);
        cbConditionAsthma = view.findViewById(R.id.cbConditionAsthma);
        cbConditionAlcoholic = view.findViewById(R.id.cbConditionAlcoholic);
        cbConditionEggAllergy = view.findViewById(R.id.cbConditionEggAllergy);

        etPvrvDay0Date = view.findViewById(R.id.etPvrvDay0Date);
        etPvrvDay0Batch = view.findViewById(R.id.etPvrvDay0Batch);
        etPvrvDay3Date = view.findViewById(R.id.etPvrvDay3Date);
        etPvrvDay3Batch = view.findViewById(R.id.etPvrvDay3Batch);
        etPvrvDay7Date = view.findViewById(R.id.etPvrvDay7Date);
        etPvrvDay7Batch = view.findViewById(R.id.etPvrvDay7Batch);
        etPvrvDay28Date = view.findViewById(R.id.etPvrvDay28Date);
        etPvrvDay28Batch = view.findViewById(R.id.etPvrvDay28Batch);
        etPvrvOutcome = view.findViewById(R.id.etPvrvOutcome);

        etPcevDay0Date = view.findViewById(R.id.etPcevDay0Date);
        etPcevDay0Batch = view.findViewById(R.id.etPcevDay0Batch);
        etPcevDay3Date = view.findViewById(R.id.etPcevDay3Date);
        etPcevDay3Batch = view.findViewById(R.id.etPcevDay3Batch);
        etPcevDay7Date = view.findViewById(R.id.etPcevDay7Date);
        etPcevDay7Batch = view.findViewById(R.id.etPcevDay7Batch);
        etPcevDay28Date = view.findViewById(R.id.etPcevDay28Date);
        etPcevDay28Batch = view.findViewById(R.id.etPcevDay28Batch);
        etPcevOutcome = view.findViewById(R.id.etPcevOutcome);

        etErig = view.findViewById(R.id.etErig);
        etHrig = view.findViewById(R.id.etHrig);
        etTetanusToxoidDate = view.findViewById(R.id.etTetanusToxoidDate);
        etAtsDose = view.findViewById(R.id.etAtsDose);
        etAtsDate = view.findViewById(R.id.etAtsDate);
        etImpression = view.findViewById(R.id.etImpression);

        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupNameAutocomplete() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> namesList = database.householdProfileDao().getAllHouseholdNames();
            if (namesList != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            namesList
                    );
                    etName.setAdapter(adapter);
                });
            }
        });

        etName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            autoPopulateFromProfile(selectedName);
        });

        // Wipes profile matching reference link constraint if the user clears input manually
        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    selectedProfileId = -1;
                }
            }
        });
    }

    private void autoPopulateFromProfile(String fullCalculatedName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = database.householdProfileDao().getProfileByCalculatedName(fullCalculatedName);
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {

                    // ---> CAPTURE PROFILE ID <---
                    selectedProfileId = profile.id;

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

                    if ("M".equalsIgnoreCase(profile.sex) || "Male".equalsIgnoreCase(profile.sex)) {
                        rgSex.check(R.id.rbMale);
                    } else if ("F".equalsIgnoreCase(profile.sex) || "Female".equalsIgnoreCase(profile.sex)) {
                        rgSex.check(R.id.rbFemale);
                    }

                    if (!TextUtils.isEmpty(profile.dob)) {
                        etBirthdate.setText(profile.dob);
                        calculateAgeFromDob(profile.dob);
                    }
                });
            }
        });
    }

    private void calculateAgeFromDob(String dobStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Calendar dob = Calendar.getInstance();
            dob.setTime(sdf.parse(dobStr));
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            etAge.setText(String.valueOf(Math.max(0, age)));
        } catch (Exception ignored) {}
    }

    private void setupCivilStatusSpinner() {
        List<String> statuses = new ArrayList<>();
        statuses.add("— Select civil status —");
        statuses.add("Single");
        statuses.add("Married");
        statuses.add("Widowed");
        statuses.add("Separated");
        statuses.add("Divorced");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCivilStatus.setAdapter(adapter);
    }

    private void setupDatePickers() {
        View.OnClickListener dateListener = v -> {
            final EditText targetEt = (EditText) v;
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                targetEt.setText(dateFormat.format(cal.getTime()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        };

        etBirthdate.setOnClickListener(dateListener);
        etDateOfBite.setOnClickListener(dateListener);
        etAnimalDiedDate.setOnClickListener(dateListener);
        etAnimalVaccinationDate.setOnClickListener(dateListener);
        etPvrvDay0Date.setOnClickListener(dateListener);
        etPvrvDay3Date.setOnClickListener(dateListener);
        etPvrvDay7Date.setOnClickListener(dateListener);
        etPvrvDay28Date.setOnClickListener(dateListener);
        etPcevDay0Date.setOnClickListener(dateListener);
        etPcevDay3Date.setOnClickListener(dateListener);
        etPcevDay7Date.setOnClickListener(dateListener);
        etPcevDay28Date.setOnClickListener(dateListener);
        etTetanusToxoidDate.setOnClickListener(dateListener);
        etAtsDate.setOnClickListener(dateListener);
    }

    private void setupTimePicker() {
        etTimeOfBite.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                etTimeOfBite.setText(timeFormat.format(cal.getTime()));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();
        });
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (TextUtils.isEmpty(etName.getText().toString().trim())) {
                etName.setError("Patient name is required");
                return;
            }
            if (TextUtils.isEmpty(etAge.getText().toString().trim())) {
                etAge.setError("Age is required");
                return;
            }
            RabiesRecord record = compileRecord();
            String PREFS_NAME = "AppPrefs";
            SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            record.setUserId(userId);
            onRecordSaved(record);
        });
    }

    private RabiesRecord compileRecord() {
        RabiesRecord record = new RabiesRecord();

        if (currentRecordId > 0) {
            record.setId(currentRecordId); // Retain ID for DB update mapping
        }

        // ---> SET PROFILE ID ON RECORD <---
        record.setProfileId(selectedProfileId);

        record.setName(etName.getText().toString().trim());

        int age = 0;
        try {
            age = Integer.parseInt(etAge.getText().toString().trim());
        } catch (NumberFormatException ignored) {}
        record.setAge(age);

        record.setSex(radioGroupLabel(rgSex));

        int civilStatusPosition = spinnerCivilStatus.getSelectedItemPosition();
        record.setCivilStatus(civilStatusPosition > 0 ? (String) spinnerCivilStatus.getSelectedItem() : null);

        record.setAddress(etAddress.getText().toString().trim());
        record.setBirthdate(etBirthdate.getText().toString().trim());
        record.setBirthPlace(etBirthPlace.getText().toString().trim());
        record.setContactNo(etContactNo.getText().toString().trim());
        record.setPhilhealthNo(etPhilhealthNo.getText().toString().trim());
        record.setWeightKg(etWeight.getText().toString().trim());
        record.setBloodPressure(etBloodPressure.getText().toString().trim());

        record.setDateOfBite(etDateOfBite.getText().toString().trim());
        record.setTimeOfBite(etTimeOfBite.getText().toString().trim());
        record.setPlaceOfBite(etPlaceOfBite.getText().toString().trim());

        record.setInjuryScratch(cbInjuryScratch.isChecked());
        record.setInjuryAbrasion(cbInjuryAbrasion.isChecked());
        record.setInjuryLaceration(cbInjuryLaceration.isChecked());
        record.setInjuryPunctured(cbInjuryPunctured.isChecked());
        record.setInjuryAvulsed(cbInjuryAvulsed.isChecked());
        record.setInjuryOthers(cbInjuryOthers.isChecked());
        record.setInjuryOthersSpecify(etInjuryOthersSpecify.getText().toString().trim());

        record.setWoundStatus(radioGroupLabel(rgWoundStatus));
        record.setWoundWashing(radioGroupLabel(rgWoundWashing));
        record.setBitingAnimal(radioGroupLabel(rgBitingAnimal));
        record.setBitingAnimalOthersSpecify(etBitingAnimalOthersSpecify.getText().toString().trim());
        record.setOwnershipStatus(radioGroupLabel(rgOwnershipStatus));
        record.setAnimalStatusAtBite(radioGroupLabel(rgAnimalStatusAtBite));
        record.setAnimalStatusAtConsult(radioGroupLabel(rgAnimalStatusAtConsult));
        record.setAnimalDiedDate(etAnimalDiedDate.getText().toString().trim());
        record.setAnimalVaccination(radioGroupLabel(rgAnimalVaccination));
        record.setAnimalVaccinationDate(etAnimalVaccinationDate.getText().toString().trim());

        record.setConditionEpilepsy(cbConditionEpilepsy.isChecked());
        record.setConditionDm(cbConditionDm.isChecked());
        record.setConditionHypertension(cbConditionHypertension.isChecked());
        record.setConditionAsthma(cbConditionAsthma.isChecked());
        record.setConditionAlcoholic(cbConditionAlcoholic.isChecked());
        record.setConditionEggAllergy(cbConditionEggAllergy.isChecked());

        record.setPvrvDay0Date(etPvrvDay0Date.getText().toString().trim());
        record.setPvrvDay0Batch(etPvrvDay0Batch.getText().toString().trim());
        record.setPvrvDay3Date(etPvrvDay3Date.getText().toString().trim());
        record.setPvrvDay3Batch(etPvrvDay3Batch.getText().toString().trim());
        record.setPvrvDay7Date(etPvrvDay7Date.getText().toString().trim());
        record.setPvrvDay7Batch(etPvrvDay7Batch.getText().toString().trim());
        record.setPvrvDay28Date(etPvrvDay28Date.getText().toString().trim());
        record.setPvrvDay28Batch(etPvrvDay28Batch.getText().toString().trim());
        record.setPvrvOutcome(etPvrvOutcome.getText().toString().trim());

        record.setPcevDay0Date(etPcevDay0Date.getText().toString().trim());
        record.setPcevDay0Batch(etPcevDay0Batch.getText().toString().trim());
        record.setPcevDay3Date(etPcevDay3Date.getText().toString().trim());
        record.setPcevDay3Batch(etPcevDay3Batch.getText().toString().trim());
        record.setPcevDay7Date(etPcevDay7Date.getText().toString().trim());
        record.setPcevDay7Batch(etPcevDay7Batch.getText().toString().trim());
        record.setPcevDay28Date(etPcevDay28Date.getText().toString().trim());
        record.setPcevDay28Batch(etPcevDay28Batch.getText().toString().trim());
        record.setPcevOutcome(etPcevOutcome.getText().toString().trim());

        record.setErig(etErig.getText().toString().trim());
        record.setHrig(etHrig.getText().toString().trim());
        record.setTetanusToxoidDate(etTetanusToxoidDate.getText().toString().trim());
        record.setAtsDose(etAtsDose.getText().toString().trim());
        record.setAtsDate(etAtsDate.getText().toString().trim());
        record.setImpression(etImpression.getText().toString().trim());

        return record;
    }

    private String radioGroupLabel(RadioGroup group) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == -1) return null;
        RadioButton button = group.findViewById(checkedId);
        return button != null ? button.getText().toString() : null;
    }

    private void onRecordSaved(RabiesRecord record) {
        new Thread(() -> {
            try {
                long id = database.rabiesDao().insertOrUpdate(record);
                currentRecordId = id;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "Treatment record saved/updated successfully.",
                                Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to commit record data.", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

    private void loadRecordForEditing(long recordId) {
        new Thread(() -> {
            try {
                RabiesRecord record = database.rabiesDao().getRecordById((int) recordId);
                if (record != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> populateUiFromRecord(record));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populateUiFromRecord(RabiesRecord record) {
        // ---> LOAD PROFILE ID <---
        selectedProfileId = record.getProfileId();

        etName.setText(record.getName(), false);
        etAge.setText(String.valueOf(record.getAge())); // Converted int to String correctly

        setRadioGroupValue(rgSex, record.getSex());
        setSpinnerValue(spinnerCivilStatus, record.getCivilStatus());
        etAddress.setText(record.getAddress());
        etBirthdate.setText(record.getBirthdate());
        etBirthPlace.setText(record.getBirthPlace());
        etContactNo.setText(record.getContactNo());
        etPhilhealthNo.setText(record.getPhilhealthNo());
        etWeight.setText(record.getWeightKg());
        etBloodPressure.setText(record.getBloodPressure());

        etDateOfBite.setText(record.getDateOfBite());
        etTimeOfBite.setText(record.getTimeOfBite());
        etPlaceOfBite.setText(record.getPlaceOfBite());

        cbInjuryScratch.setChecked(record.isInjuryScratch());
        cbInjuryAbrasion.setChecked(record.isInjuryAbrasion());
        cbInjuryLaceration.setChecked(record.isInjuryLaceration());
        cbInjuryPunctured.setChecked(record.isInjuryPunctured());
        cbInjuryAvulsed.setChecked(record.isInjuryAvulsed());
        cbInjuryOthers.setChecked(record.isInjuryOthers());
        etInjuryOthersSpecify.setText(record.getInjuryOthersSpecify());

        setRadioGroupValue(rgWoundStatus, record.getWoundStatus());
        setRadioGroupValue(rgWoundWashing, record.getWoundWashing());
        setRadioGroupValue(rgBitingAnimal, record.getBitingAnimal());
        etBitingAnimalOthersSpecify.setText(record.getBitingAnimalOthersSpecify());
        setRadioGroupValue(rgOwnershipStatus, record.getOwnershipStatus());
        setRadioGroupValue(rgAnimalStatusAtBite, record.getAnimalStatusAtBite());
        setRadioGroupValue(rgAnimalStatusAtConsult, record.getAnimalStatusAtConsult());
        etAnimalDiedDate.setText(record.getAnimalDiedDate());
        setRadioGroupValue(rgAnimalVaccination, record.getAnimalVaccination());
        etAnimalVaccinationDate.setText(record.getAnimalVaccinationDate());

        cbConditionEpilepsy.setChecked(record.isConditionEpilepsy());
        cbConditionDm.setChecked(record.isConditionDm());
        cbConditionHypertension.setChecked(record.isConditionHypertension());
        cbConditionAsthma.setChecked(record.isConditionAsthma());
        cbConditionAlcoholic.setChecked(record.isConditionAlcoholic());
        cbConditionEggAllergy.setChecked(record.isConditionEggAllergy());

        etPvrvDay0Date.setText(record.getPvrvDay0Date());
        etPvrvDay0Batch.setText(record.getPvrvDay0Batch());
        etPvrvDay3Date.setText(record.getPvrvDay3Date());
        etPvrvDay3Batch.setText(record.getPvrvDay3Batch());
        etPvrvDay7Date.setText(record.getPvrvDay7Date());
        etPvrvDay7Batch.setText(record.getPvrvDay7Batch());
        etPvrvDay28Date.setText(record.getPvrvDay28Date());
        etPvrvDay28Batch.setText(record.getPvrvDay28Batch());
        etPvrvOutcome.setText(record.getPvrvOutcome());

        etPcevDay0Date.setText(record.getPcevDay0Date());
        etPcevDay0Batch.setText(record.getPcevDay0Batch());
        etPcevDay3Date.setText(record.getPcevDay3Date());
        etPcevDay3Batch.setText(record.getPcevDay3Batch());
        etPcevDay7Date.setText(record.getPcevDay7Date());
        etPcevDay7Batch.setText(record.getPcevDay7Batch());
        etPcevDay28Date.setText(record.getPcevDay28Date());
        etPcevDay28Batch.setText(record.getPcevDay28Batch());
        etPcevOutcome.setText(record.getPcevOutcome());

        etErig.setText(record.getErig());
        etHrig.setText(record.getHrig());
        etTetanusToxoidDate.setText(record.getTetanusToxoidDate());
        etAtsDose.setText(record.getAtsDose());
        etAtsDate.setText(record.getAtsDate());
        etImpression.setText(record.getImpression());

        btnSave.setText("Update Record");
    }

    private void setRadioGroupValue(RadioGroup group, String value) {
        if (value == null) return;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                if (value.equalsIgnoreCase(rb.getText().toString())) {
                    rb.setChecked(true);
                    break;
                }
            }
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null || spinner.getAdapter() == null) return;
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            if (spinner.getAdapter().getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}