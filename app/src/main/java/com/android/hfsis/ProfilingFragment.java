package com.android.hfsis;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.address.Barangay;
import com.android.hfsis.model.address.Municipality;
import com.android.hfsis.model.address.Province;
import com.android.hfsis.model.address.Region;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ProfilingFragment extends Fragment {

    // Household Structure Field Declarations
    private EditText etSitio, etHHNumber, etRespondent;
    private AutoCompleteTextView etRegion, etProvince, etMunicipality, etBarangay;
    private Spinner spinnerSocioStatus, spinnerWaterSource, spinnerToiletType;

    // Member Specific Field Declarations
    private EditText etFamilyNumber, etMemberLastName, etMemberMiddleName, etMemberFirstName, etDOB, etPhilhealthID, etFPMethodUsed, etReligion;
    private TextInputLayout tilDOB;
    private Spinner spinnerRelationship, spinnerSex, spinnerPhilType, spinnerPhilCategory;
    private CheckBox cbHPN, cbDM, cbTB, cbFPMethod;
    private Button btnSubmitProfiling;

    // View Container Wrappers
    private ScrollView profilingScrollView;
    private LinearLayout profilingContainer;

    // Integrated AutoComplete Dropdown View
    private AutoCompleteTextView etEducation;

    // Options mapping datasets
    private final String[] educationOptions = new String[]{
            "None", "Kinder", "Elementary Student", "Elementary Under Graduate", "Elementary Graduate",
            "High School Student", "High School Under Graduate", "High School Graduate",
            "Vocational Course", "College Student", "College Under Graduate", "College Graduate", "Postgraduate"
    };

    private final String[] socioOptions = new String[]{"NON NHTS", "NHTS -4PS", "NHTS Non 4Ps"};
    private final String[] waterOptions = new String[]{"Level 1", "Level 2", "Level 3", "Others"};
    private final String[] toiletOptions = new String[]{
            "Poor/ Flush toilet connected to septic tank", "Water-sealed toilet without septic tank",
            "Poor/ Flush toilet connected to community sewerage system", "Open Pit latrine",
            "Overhung latrine", "Ventilated improved pit latrine", "No Toilet"
    };
    private final String[] relationOptions = new String[]{
            "Head", "Spouse", "Son", "Daughter", "Brother", "Sister", "Father", "Mother",
            "Grandmother", "Grandfather", "Uncle", "Autie", "Brother in Law", "Sister in Law",
            "Mother In Law", "Father In Law", "Grand Son", "Grand Daughther", "Cousin",
            "Niece", "Nephew", "Step Sister/Brother", "Daughter /Son in Law", "Other"
    };
    private final String[] sexOptions = new String[]{"Male", "Female"};
    private final String[] philTypeOptions = new String[]{"Member", "Dependent", "≥ 21"};
    private final String[] philCategoryOptions = new String[]{"FE-Private", "FE-Government", "IE", "NHTS", "SC", "IP", "Unknown"};

    // Cascading Address Lookup Datasets (loaded from local Room DB)
    private List<Region> regionList = new ArrayList<>();
    private List<Province> provinceList = new ArrayList<>();
    private List<Municipality> municipalityList = new ArrayList<>();
    private List<Barangay> barangayList = new ArrayList<>();

    // Currently selected parent codes, used to filter the next dropdown level
    private String selectedRegCode = null;
    private String selectedProvCode = null;
    private String selectedCitymunCode = null;

    // Edit Operation Context States
    private long profileId = -1;
    private boolean isEditMode = false;
    private HouseholdProfile existingProfile;

    public ProfilingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profiling, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Shell Containers
        profilingScrollView = view.findViewById(R.id.profilingScrollView);
        profilingContainer = view.findViewById(R.id.profilingContainer);

        // Bind Individual Layout Component References
        etSitio = view.findViewById(R.id.etSitio);
        etRegion = view.findViewById(R.id.etRegion);
        etProvince = view.findViewById(R.id.etProvince);
        etMunicipality = view.findViewById(R.id.etMunicipality);
        etBarangay = view.findViewById(R.id.etBarangay);
        etHHNumber = view.findViewById(R.id.etHHNumber);
        etRespondent = view.findViewById(R.id.etRespondent);

        spinnerSocioStatus = view.findViewById(R.id.spinnerSocioStatus);
        spinnerWaterSource = view.findViewById(R.id.spinnerWaterSource);
        spinnerToiletType = view.findViewById(R.id.spinnerToiletType);

        etFamilyNumber = view.findViewById(R.id.etFamilyNumber);
        etMemberLastName = view.findViewById(R.id.etMemberLastName);
        etMemberMiddleName = view.findViewById(R.id.etMemberMiddleName);
        etMemberFirstName = view.findViewById(R.id.etMemberFirstName);
        spinnerRelationship = view.findViewById(R.id.spinnerRelationship);
        spinnerSex = view.findViewById(R.id.spinnerSex);

        tilDOB = view.findViewById(R.id.tilDOB);
        etDOB = view.findViewById(R.id.etDOB);

        etPhilhealthID = view.findViewById(R.id.etPhilhealthID);
        spinnerPhilType = view.findViewById(R.id.spinnerPhilType);
        spinnerPhilCategory = view.findViewById(R.id.spinnerPhilCategory);

        cbHPN = view.findViewById(R.id.cbHPN);
        cbDM = view.findViewById(R.id.cbDM);
        cbTB = view.findViewById(R.id.cbTB);

        cbFPMethod = view.findViewById(R.id.cbFPMethod);
        etFPMethodUsed = view.findViewById(R.id.etFPMethodUsed);
        etEducation = view.findViewById(R.id.etEducation);
        etReligion = view.findViewById(R.id.etReligion);

        btnSubmitProfiling = view.findViewById(R.id.btnSubmitProfiling);

        // Bind Data Adapters
        setupFormAdapters();

        // Load Region/Province/Municipality/Barangay AutoComplete Dropdown Data
        setupAddressDropdowns();

        // Make tapping an address field show its full list immediately (not just on typing)
        setupExposedDropdownTapBehavior();

        // Bind Listeners and Functional Features
        setupDynamicFormListeners();

        // Initialize Window Inset Dynamic Layout Scaling Listener
        setupKeyboardFocusAutoScroll();

        // Check if Fragment was initialized with a target profile ID bundle for modification
        if (getArguments() != null && getArguments().containsKey("PROFILE_ID")) {
            profileId = getArguments().getLong("PROFILE_ID");
            isEditMode = true;
            btnSubmitProfiling.setText("Update Profile Document");
            loadProfileForEditing();
        }
    }

    private void setupFormAdapters() {
        if (getContext() != null) {
            spinnerSocioStatus.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, socioOptions));
            spinnerWaterSource.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, waterOptions));
            spinnerToiletType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, toiletOptions));
            spinnerRelationship.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, relationOptions));
            spinnerSex.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, sexOptions));
            spinnerPhilType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, philTypeOptions));
            spinnerPhilCategory.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, philCategoryOptions));

            ArrayAdapter<String> eduAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, educationOptions);
            etEducation.setAdapter(eduAdapter);
        }
    }

    private void setupAddressDropdowns() {
        if (getContext() == null) return;

        etProvince.setEnabled(false);
        etMunicipality.setEnabled(false);
        etBarangay.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            regionList = db.regionDao().getAllRegions();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String[] regionNames = new String[regionList.size()];
                    for (int i = 0; i < regionList.size(); i++) regionNames[i] = regionList.get(i).regDesc;

                    ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, regionNames);
                    etRegion.setAdapter(regionAdapter);
                    etRegion.setThreshold(0);
                });
            }
        });

        etRegion.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedRegCode = findRegCodeByDesc(selectedName);

            etProvince.setText("", false);
            etMunicipality.setText("", false);
            etBarangay.setText("", false);
            selectedProvCode = null;
            selectedCitymunCode = null;
            etMunicipality.setEnabled(false);
            etBarangay.setEnabled(false);

            etProvince.setEnabled(selectedRegCode != null);
            if (selectedRegCode != null) {
                loadProvincesForRegion(selectedRegCode);
            }
        });

        etProvince.setOnItemClickListener((parent, view, position, id) -> {
            String HelloselectedName = (String) parent.getItemAtPosition(position);
            selectedProvCode = findProvCodeByDesc(HelloselectedName);

            etMunicipality.setText("", false);
            etBarangay.setText("", false);
            selectedCitymunCode = null;
            etBarangay.setEnabled(false);

            etMunicipality.setEnabled(selectedProvCode != null);
            if (selectedProvCode != null) {
                loadMunicipalitiesForProvince(selectedProvCode);
            }
        });

        etMunicipality.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedCitymunCode = findCitymunCodeByDesc(selectedName);

            etBarangay.setText("", false);
            etBarangay.setEnabled(selectedCitymunCode != null);
            if (selectedCitymunCode != null) {
                loadBarangaysForMunicipality(selectedCitymunCode);
            }
        });
    }

    private void setupExposedDropdownTapBehavior() {
        attachTapToShowDropdown(etRegion);
        attachTapToShowDropdown(etProvince);
        attachTapToShowDropdown(etMunicipality);
        attachTapToShowDropdown(etBarangay);
    }

    private void attachTapToShowDropdown(AutoCompleteTextView field) {
        field.setOnClickListener(v -> {
            if (field.isEnabled() && field.getAdapter() != null) {
                field.showDropDown();
            }
        });
    }

    private boolean isAddressDropdownField(View v) {
        int id = v.getId();
        return id == R.id.etRegion || id == R.id.etProvince
                || id == R.id.etMunicipality || id == R.id.etBarangay;
    }

    private void loadProvincesForRegion(String regCode) {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            provinceList = db.provinceDao().getProvincesByRegion(regCode);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String[] provinceNames = new String[provinceList.size()];
                    for (int i = 0; i < provinceList.size(); i++) provinceNames[i] = provinceList.get(i).provDesc;

                    ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, provinceNames);
                    etProvince.setAdapter(provinceAdapter);
                    etProvince.setThreshold(0);
                });
            }
        });
    }

    private void loadMunicipalitiesForProvince(String provCode) {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            municipalityList = db.municipalityDao().getMunicipalitiesByProvince(provCode);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String[] munNames = new String[municipalityList.size()];
                    for (int i = 0; i < municipalityList.size(); i++) munNames[i] = municipalityList.get(i).citymunDesc;

                    ArrayAdapter<String> munAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, munNames);
                    etMunicipality.setAdapter(munAdapter);
                    etMunicipality.setThreshold(0);
                });
            }
        });
    }

    private void loadBarangaysForMunicipality(String citymunCode) {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            barangayList = db.barangayDao().getBarangaysByMunicipality(citymunCode);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String[] brgyNames = new String[barangayList.size()];
                    for (int i = 0; i < barangayList.size(); i++) brgyNames[i] = barangayList.get(i).brgyDesc;

                    ArrayAdapter<String> brgyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, brgyNames);
                    etBarangay.setAdapter(brgyAdapter);
                    etBarangay.setThreshold(0);
                });
            }
        });
    }

    private String findRegCodeByDesc(String desc) {
        for (Region r : regionList) {
            if (r.regDesc != null && r.regDesc.equals(desc)) return r.regCode;
        }
        return null;
    }

    private String findProvCodeByDesc(String desc) {
        for (Province p : provinceList) {
            if (p.provDesc != null && p.provDesc.equals(desc)) return p.provCode;
        }
        return null;
    }

    private String findCitymunCodeByDesc(String desc) {
        for (Municipality m : municipalityList) {
            if (m.citymunDesc != null && m.citymunDesc.equals(desc)) return m.citymunCode;
        }
        return null;
    }

    private void setupDynamicFormListeners() {
        // Formats keyboard inputs while typing manually
        etDOB.addTextChangedListener(new DateFormattingWatcher(etDOB));

        // Opens calendar date picker only when clicking the end icon
        tilDOB.setEndIconOnClickListener(v -> showDatePickerDialog(etDOB));

        etFPMethodUsed.setEnabled(cbFPMethod.isChecked());
        cbFPMethod.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etFPMethodUsed.setEnabled(isChecked);
            if (!isChecked) {
                etFPMethodUsed.setText("");
                etFPMethodUsed.setError(null);
            }
        });

        btnSubmitProfiling.setOnClickListener(v -> saveProfilingData());
    }

    private void showDatePickerDialog(EditText targetEditText) {
        if (getContext() == null) return;

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selYear, selMonth, selDay) -> {
                    Calendar selCalendar = Calendar.getInstance();
                    selCalendar.set(Calendar.YEAR, selYear);
                    selCalendar.set(Calendar.MONTH, selMonth);
                    selCalendar.set(Calendar.DAY_OF_MONTH, selDay);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    targetEditText.setText(sdf.format(selCalendar.getTime()));
                    targetEditText.setError(null);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupKeyboardFocusAutoScroll() {
        ViewCompat.setOnApplyWindowInsetsListener(profilingScrollView, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int keyboardHeight = imeInsets.bottom;

            int defaultPadding = (int) (16 * getResources().getDisplayMetrics().density);
            profilingContainer.setPadding(defaultPadding, defaultPadding, defaultPadding, keyboardHeight + defaultPadding);

            return insets;
        });

        View[] inputFields = new View[]{
                etSitio, etRegion, etProvince, etMunicipality, etBarangay, etHHNumber, etRespondent,
                etFamilyNumber, etMemberLastName, etMemberMiddleName, etMemberFirstName,
                etDOB, etPhilhealthID, etFPMethodUsed, etEducation, etReligion
        };

        for (View field : inputFields) {
            if (field != null) {
                field.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        if (v.getId() == R.id.etEducation && etEducation.getText().toString().isEmpty()) {
                            etEducation.post(() -> {
                                if (isAdded() && etEducation.getWindowToken() != null) {
                                    etEducation.showDropDown();
                                }
                            });
                        }

                        if (isAddressDropdownField(v) && v instanceof AutoCompleteTextView) {
                            AutoCompleteTextView actv = (AutoCompleteTextView) v;
                            if (actv.getText().toString().isEmpty()) {
                                actv.post(() -> {
                                    if (isAdded() && actv.getWindowToken() != null
                                            && actv.isEnabled() && actv.getAdapter() != null) {
                                        actv.showDropDown();
                                    }
                                });
                            }
                        }

                        profilingScrollView.postDelayed(() -> {
                            if (!isAdded() || v.getWindowToken() == null) return;

                            int[] viewLoc = new int[2];
                            v.getLocationInWindow(viewLoc);
                            int viewY = viewLoc[1];

                            int[] scrollLoc = new int[2];
                            profilingScrollView.getLocationInWindow(scrollLoc);
                            int scrollY = scrollLoc[1];

                            profilingScrollView.smoothScrollBy(0, viewY - scrollY - 150);
                        }, 200);
                    }
                });
            }
        }
    }

    private void loadProfileForEditing() {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            existingProfile = db.householdProfileDao().getProfileById(profileId);

            if (existingProfile != null && getActivity() != null) {
                getActivity().runOnUiThread(this::prePopulateFormFields);
            }
        });
    }

    private void prePopulateFormFields() {
        if (existingProfile == null) return;

        etSitio.setText(existingProfile.sitio);
        etHHNumber.setText(existingProfile.hhNumber);
        etRespondent.setText(existingProfile.respondent);
        etFamilyNumber.setText(existingProfile.familyNumber);
        etMemberLastName.setText(existingProfile.memberLastName);
        etMemberMiddleName.setText(existingProfile.memberMiddleName);
        etMemberFirstName.setText(existingProfile.memberFirstName);
        etDOB.setText(existingProfile.dob);
        etPhilhealthID.setText(existingProfile.philhealthId);
        etFPMethodUsed.setText(existingProfile.fpMethodUsed);

        etEducation.setText(existingProfile.education, false);
        etReligion.setText(existingProfile.religion);

        prePopulateAddressFields();

        setSpinnerSelectionByValue(spinnerSocioStatus, existingProfile.socioStatus);
        setSpinnerSelectionByValue(spinnerWaterSource, existingProfile.waterSource);
        setSpinnerSelectionByValue(spinnerToiletType, existingProfile.toiletType);
        setSpinnerSelectionByValue(spinnerRelationship, existingProfile.relationship);
        setSpinnerSelectionByValue(spinnerSex, existingProfile.sex);
        setSpinnerSelectionByValue(spinnerPhilType, existingProfile.philType);
        setSpinnerSelectionByValue(spinnerPhilCategory, existingProfile.philCategory);

        cbHPN.setChecked(existingProfile.hpn);
        cbDM.setChecked(existingProfile.dm);
        cbTB.setChecked(existingProfile.tb);
        cbFPMethod.setChecked(existingProfile.fpMethod);

        etFPMethodUsed.setEnabled(existingProfile.fpMethod);
    }

    private void prePopulateAddressFields() {
        if (existingProfile == null || getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());

            if (regionList.isEmpty()) regionList = db.regionDao().getAllRegions();
            String regCode = findRegCodeByDesc(existingProfile.region);
            if (regCode == null) regCode = matchCodeIgnoreCase(regionList, existingProfile.region);
            selectedRegCode = regCode;

            List<Province> provinces = (regCode != null) ? db.provinceDao().getProvincesByRegion(regCode) : new ArrayList<>();
            provinceList = provinces;
            String provCode = findProvCodeByDesc(existingProfile.province);
            selectedProvCode = provCode;

            List<Municipality> municipalities = (provCode != null) ? db.municipalityDao().getMunicipalitiesByProvince(provCode) : new ArrayList<>();
            municipalityList = municipalities;
            String citymunCode = findCitymunCodeByDesc(existingProfile.municipality);
            selectedCitymunCode = citymunCode;

            List<Barangay> barangays = (citymunCode != null) ? db.barangayDao().getBarangaysByMunicipality(citymunCode) : new ArrayList<>();
            barangayList = barangays;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    etRegion.setText(existingProfile.region, false);

                    String[] provinceNames = new String[provinceList.size()];
                    for (int i = 0; i < provinceList.size(); i++) provinceNames[i] = provinceList.get(i).provDesc;
                    etProvince.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, provinceNames));
                    etProvince.setThreshold(0);
                    etProvince.setEnabled(!provinceList.isEmpty());
                    etProvince.setText(existingProfile.province, false);

                    String[] munNames = new String[municipalityList.size()];
                    for (int i = 0; i < municipalityList.size(); i++) munNames[i] = municipalityList.get(i).citymunDesc;
                    etMunicipality.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, munNames));
                    etMunicipality.setThreshold(0);
                    etMunicipality.setEnabled(!municipalityList.isEmpty());
                    etMunicipality.setText(existingProfile.municipality, false);

                    String[] brgyNames = new String[barangayList.size()];
                    for (int i = 0; i < barangayList.size(); i++) brgyNames[i] = barangayList.get(i).brgyDesc;
                    etBarangay.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, brgyNames));
                    etBarangay.setThreshold(0);
                    etBarangay.setEnabled(!barangayList.isEmpty());
                    etBarangay.setText(existingProfile.barangay, false);
                });
            }
        });
    }

    private String matchCodeIgnoreCase(List<Region> regions, String desc) {
        if (desc == null) return null;
        for (Region r : regions) {
            if (r.regDesc != null && r.regDesc.trim().equalsIgnoreCase(desc.trim())) return r.regCode;
        }
        return null;
    }

    private String setSpinnerSelectionByValue(Spinner spinner, String value) {
        if (value == null || spinner.getAdapter() == null) return null;

        String cleanValue = value.trim().toLowerCase(Locale.getDefault());
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            String itemText = spinner.getAdapter().getItem(i).toString().trim().toLowerCase(Locale.getDefault());
            if (itemText.equals(cleanValue) || itemText.contains(cleanValue) || cleanValue.contains(itemText)) {
                spinner.setSelection(i);
                break;
            }
        }
        return null;
    }

    private void saveProfilingData() {
        String hhNum = etHHNumber.getText().toString().trim();
        String memberLastName = etMemberLastName.getText().toString().trim();
        String memberFirstName = etMemberFirstName.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String fpMethodUsed = etFPMethodUsed.getText().toString().trim();

        if (hhNum.isEmpty()) {
            etHHNumber.setError("Household Number is required");
            etHHNumber.requestFocus();
            return;
        }
        if (memberLastName.isEmpty()) {
            etMemberLastName.setError("Last name is required");
            etMemberLastName.requestFocus();
            return;
        }
        if (memberFirstName.isEmpty()) {
            etMemberFirstName.setError("First name is required");
            etMemberFirstName.requestFocus();
            return;
        }
        if (dob.isEmpty()) {
            etDOB.setError("Date of Birth is required");
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Please input/select Date of Birth", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (cbFPMethod.isChecked() && fpMethodUsed.isEmpty()) {
            etFPMethodUsed.setError("Please specify the family planning method used");
            etFPMethodUsed.requestFocus();
            return;
        }

        HouseholdProfile profile = isEditMode ? existingProfile : new HouseholdProfile();

        profile.sitio = etSitio.getText().toString().trim();
        profile.region = etRegion.getText().toString().trim();
        profile.province = etProvince.getText().toString().trim();
        profile.municipality = etMunicipality.getText().toString().trim();
        profile.barangay = etBarangay.getText().toString().trim();
        profile.hhNumber = hhNum;
        profile.respondent = etRespondent.getText().toString().trim();

        if (spinnerSocioStatus.getSelectedItem() != null) profile.socioStatus = spinnerSocioStatus.getSelectedItem().toString();
        if (spinnerWaterSource.getSelectedItem() != null) profile.waterSource = spinnerWaterSource.getSelectedItem().toString();
        if (spinnerToiletType.getSelectedItem() != null) profile.toiletType = spinnerToiletType.getSelectedItem().toString();

        profile.familyNumber = etFamilyNumber.getText().toString().trim();
        profile.memberLastName = memberLastName;
        profile.memberMiddleName = etMemberMiddleName.getText().toString().trim();
        profile.memberFirstName = memberFirstName;

        if (spinnerRelationship.getSelectedItem() != null) profile.relationship = spinnerRelationship.getSelectedItem().toString();
        if (spinnerSex.getSelectedItem() != null) profile.sex = spinnerSex.getSelectedItem().toString();
        profile.dob = dob;

        profile.philhealthId = etPhilhealthID.getText().toString().trim();
        if (spinnerPhilType.getSelectedItem() != null) profile.philType = spinnerPhilType.getSelectedItem().toString();
        if (spinnerPhilCategory.getSelectedItem() != null) profile.philCategory = spinnerPhilCategory.getSelectedItem().toString();

        profile.hpn = cbHPN.isChecked();
        profile.dm = cbDM.isChecked();
        profile.tb = cbTB.isChecked();

        profile.fpMethod = cbFPMethod.isChecked();
        profile.fpMethodUsed = fpMethodUsed;
        profile.education = etEducation.getText().toString().trim();
        profile.religion = etReligion.getText().toString().trim();

        persistProfileData(profile);
    }

    private void persistProfileData(HouseholdProfile profile) {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            long operationalResult;

            if (isEditMode) {
                db.householdProfileDao().updateProfile(profile);
                operationalResult = 1;
            } else {
                operationalResult = db.householdProfileDao().insertProfile(profile);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (operationalResult >= 0) {
                        String feedbackMsg = isEditMode ? "Profile updated successfully" : "Profile added successfully";
                        Toast.makeText(getActivity(), feedbackMsg, Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        String failureMsg = isEditMode ? "Failed to update profile" : "Failed to add profile";
                        Toast.makeText(getActivity(), failureMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void resetFormFields() {
        etSitio.setText("");
        etRegion.setText("", false);
        etProvince.setText("", false);
        etMunicipality.setText("", false);
        etBarangay.setText("", false);
        etHHNumber.setText("");
        etRespondent.setText("");
        etFamilyNumber.setText("");
        etMemberLastName.setText("");
        etMemberMiddleName.setText("");
        etMemberFirstName.setText("");
        etDOB.setText("");
        etPhilhealthID.setText("");
        etFPMethodUsed.setText("");
        etEducation.setText("", false);
        etReligion.setText("");
        cbHPN.setChecked(false);
        cbDM.setChecked(false);
        cbTB.setChecked(false);
        cbFPMethod.setChecked(false);

        selectedRegCode = null;
        selectedProvCode = null;
        selectedCitymunCode = null;
        etProvince.setEnabled(false);
        etMunicipality.setEnabled(false);
        etBarangay.setEnabled(false);

        spinnerSocioStatus.setSelection(0);
        spinnerWaterSource.setSelection(0);
        spinnerToiletType.setSelection(0);
        spinnerRelationship.setSelection(0);
        spinnerSex.setSelection(0);
        spinnerPhilType.setSelection(0);
        spinnerPhilCategory.setSelection(0);

        profilingScrollView.smoothScrollTo(0, 0);
    }

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