package com.android.hfsis.vital_statistics.environmental;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.environmental.EnvironmentalHealthModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MasterlistEnvironmentalHealthFragment extends Fragment {

    private long currentRecordId = 0;
    private long selectedProfileId = -1;

    private AutoCompleteTextView etHouseholdHeadName;
    private RadioGroup rgWaterSourceType;
    private TextInputEditText etWaterSourceOthers;
    private RadioGroup rgWaterLocatedInside, rgWaterAvailable12Hours;
    private TextInputEditText etMicrobiologicalTestDate;
    private RadioGroup rgMicrobiologicalResult, rgWaterSafetyPlan;

    private RadioGroup rgSanitationStatus;
    private RadioGroup rgUnsanitaryToilet;
    private RadioGroup rgToiletShared;
    private RadioGroup rgBasicSanitationFacility;
    private TextInputEditText etDisposalDate;
    private CheckBox cbDisposalInSitu, cbDisposalOffSiteDesludged, cbDisposalOffSiteSewer;
    private RadioGroup rgSafelyManagedSanitation;

    private RadioGroup rgSafelyManagedDrinkingWater;
    private TextInputEditText etRemarks;

    private MaterialButton btnClear, btnSave;
    private DatabaseHelper db;

    public MasterlistEnvironmentalHealthFragment() {}

    public static MasterlistEnvironmentalHealthFragment newInstance() {
        return new MasterlistEnvironmentalHealthFragment();
    }

    public void setExistingRecordId(long id) {
        this.currentRecordId = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_masterlist_environmental_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        initializeUiElements(view);
        setupListeners();
        loadHouseholdNamesAutoComplete();

        if (currentRecordId > 0) {
            loadExistingRecordData();
        }

        btnClear.setOnClickListener(v -> clearFormInputs());
        btnSave.setOnClickListener(v -> saveOrUpdateDataRecord());
    }

    private void initializeUiElements(View view) {
        etHouseholdHeadName = (AutoCompleteTextView) view.findViewById(R.id.etHouseholdHeadName);

        rgWaterSourceType = view.findViewById(R.id.rgWaterSourceType);
        etWaterSourceOthers = view.findViewById(R.id.etWaterSourceOthers);
        rgWaterLocatedInside = view.findViewById(R.id.rgWaterLocatedInside);
        rgWaterAvailable12Hours = view.findViewById(R.id.rgWaterAvailable12Hours);
        etMicrobiologicalTestDate = view.findViewById(R.id.etMicrobiologicalTestDate);
        rgMicrobiologicalResult = view.findViewById(R.id.rgMicrobiologicalResult);
        rgWaterSafetyPlan = view.findViewById(R.id.rgWaterSafetyPlan);

        rgSanitationStatus = view.findViewById(R.id.rgSanitationStatus);
        rgUnsanitaryToilet = view.findViewById(R.id.rgUnsanitaryToilet);
        rgToiletShared = view.findViewById(R.id.rgToiletShared);
        rgBasicSanitationFacility = view.findViewById(R.id.rgBasicSanitationFacility);
        etDisposalDate = view.findViewById(R.id.etDisposalDate);
        cbDisposalInSitu = view.findViewById(R.id.cbDisposalInSitu);
        cbDisposalOffSiteDesludged = view.findViewById(R.id.cbDisposalOffSiteDesludged);
        cbDisposalOffSiteSewer = view.findViewById(R.id.cbDisposalOffSiteSewer);
        rgSafelyManagedSanitation = view.findViewById(R.id.rgSafelyManagedSanitation);

        rgSafelyManagedDrinkingWater = view.findViewById(R.id.rgSafelyManagedDrinkingWater);
        etRemarks = view.findViewById(R.id.etRemarks);

        btnClear = view.findViewById(R.id.btnClear);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        etHouseholdHeadName.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            etHouseholdHeadName.setText(selectedName, false);
            resolveProfileId(selectedName);
        });

        bindDateField(etMicrobiologicalTestDate);
        bindDateField(etDisposalDate);
    }

    private void loadHouseholdNamesAutoComplete() {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            // getAllHousehold() returns only profiles where relationship = "Head",
            // which is the correct source for the Household Head Name field.
            List<HouseholdProfile> heads = db.householdProfileDao().getAllHousehold();

            List<String> householdNames = new ArrayList<>();
            if (heads != null) {
                for (HouseholdProfile head : heads) {
                    if (head.memberLastName == null || head.memberFirstName == null) continue;
                    // Build the same format used by getProfileByCalculatedName():
                    // "LastName, FirstName MiddleName"
                    String middleName = head.memberMiddleName != null ? head.memberMiddleName : "";
                    String formattedName = (head.memberLastName + ", " + head.memberFirstName + " " + middleName).trim();
                    householdNames.add(formattedName);
                }
            }

            final List<String> finalNames = householdNames;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!finalNames.isEmpty() && isAdded()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_list_item_1,
                                finalNames
                        );
                        etHouseholdHeadName.setAdapter(adapter);
                    }
                });
            }
        });
    }

    private void resolveProfileId(String selectedName) {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = db.householdProfileDao().getProfileByCalculatedName(selectedName);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    selectedProfileId = (profile != null) ? profile.id : -1;
                });
            }
        });
    }

    private void bindDateField(TextInputEditText field) {
        // Formats keyboard inputs while typing manually
        field.addTextChangedListener(new DateFormattingWatcher(field));

        // Opens calendar date picker only when clicking the end icon
        TextInputLayout til = findParentTextInputLayout(field);
        if (til != null) {
            til.setEndIconOnClickListener(v -> showDatePicker(field));
        }
    }

    private TextInputLayout findParentTextInputLayout(View child) {
        ViewParent parent = child.getParent();
        while (parent != null) {
            if (parent instanceof TextInputLayout) return (TextInputLayout) parent;
            parent = parent.getParent();
        }
        return null;
    }

    // Converts legacy yyyy-MM-dd values (saved before the format change) to MM-dd-yyyy for display
    private String toDisplayDate(String date) {
        if (date != null && date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return date.substring(5, 7) + "-" + date.substring(8, 10) + "-" + date.substring(0, 4);
        }
        return date;
    }

    private void showDatePicker(TextInputEditText etTarget) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    etTarget.setText(String.format(Locale.US, "%02d-%02d-%04d", selectedMonth + 1, selectedDay, selectedYear));
                    etTarget.setError(null);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadExistingRecordData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            EnvironmentalHealthModel record = db.environmentalHealthDao().getRecordById(currentRecordId);
            if (record != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> bindDataModelToUiFields(record));
            }
        });
    }

    private void bindDataModelToUiFields(EnvironmentalHealthModel record) {
        selectedProfileId = record.getProfileId();
        etHouseholdHeadName.setText(record.getHouseholdHeadName(), false);

        if (record.isWaterLevelI()) rgWaterSourceType.check(R.id.rbLevelI);
        else if (record.isWaterLevelII()) rgWaterSourceType.check(R.id.rbLevelII);
        else if (record.isWaterLevelIII()) rgWaterSourceType.check(R.id.rbLevelIII);
        else if (!TextUtils.isEmpty(record.getWaterSourceOthers())) {
            rgWaterSourceType.check(R.id.rbWaterOthers);
            etWaterSourceOthers.setText(record.getWaterSourceOthers());
        }

        rgWaterLocatedInside.check(record.isWaterLocatedInsideDwelling() ? R.id.rbWaterInsideYes : R.id.rbWaterInsideNo);
        rgWaterAvailable12Hours.check(record.isWaterAvailable12Hours() ? R.id.rbWater12HoursYes : R.id.rbWater12HoursNo);
        etMicrobiologicalTestDate.setText(toDisplayDate(record.getMicrobiologicalTestDate()));
        setRadioValue(rgMicrobiologicalResult, record.getMicrobiologicalTestResult(), R.id.rbMicrobiologicalPos, R.id.rbMicrobiologicalNeg);
        setRadioValue(rgWaterSafetyPlan, record.getWaterSafetyPlanOperational(), R.id.rbWaterSafetyPlanYes, R.id.rbWaterSafetyPlanNo);

        String sStatus = record.getSanitationStatus();
        if ("Functional Sanitary".equalsIgnoreCase(sStatus)) rgSanitationStatus.check(R.id.rbSanitaryToilet);
        else if ("Unsanitary".equalsIgnoreCase(sStatus)) rgSanitationStatus.check(R.id.rbUnsanitaryToilet);
        else if ("No Toilet".equalsIgnoreCase(sStatus)) rgSanitationStatus.check(R.id.rbNoToilet);

        setUnsanitaryToiletSelection(record.getUnsanitaryToiletType());
        setRadioValue(rgToiletShared, record.getToiletShared(), R.id.rbToiletSharedYes, R.id.rbToiletSharedNo);
        setRadioValue(rgBasicSanitationFacility, record.getBasicSanitationFacility(), R.id.rbBasicFacilityYes, R.id.rbBasicFacilityNo);

        etDisposalDate.setText(toDisplayDate(record.getDisposalDate()));
        cbDisposalInSitu.setChecked(record.isDisposalInSitu());
        cbDisposalOffSiteDesludged.setChecked(record.isDisposalOffSiteDesludged());
        cbDisposalOffSiteSewer.setChecked(record.isDisposalOffSiteSewer());

        setRadioValue(rgSafelyManagedSanitation, record.getSafelyManagedSanitationService(), R.id.rbSafelyManagedSanitationYes, R.id.rbSafelyManagedSanitationNo);
        setRadioValue(rgSafelyManagedDrinkingWater, record.getSafelyManagedDrinkingWater(), R.id.rbSafelyManagedDrinkingWaterYes, R.id.rbSafelyManagedDrinkingWaterNo);
        etRemarks.setText(record.getRemarks());
    }

    private void saveOrUpdateDataRecord() {
        String name = getTextFrom(etHouseholdHeadName);
        if (TextUtils.isEmpty(name)) {
            etHouseholdHeadName.setError("Household Head Name is required");
            return;
        }

        if (selectedProfileId == -1) {
            etHouseholdHeadName.setError("Please select a valid household head from the dropdown list");
            etHouseholdHeadName.requestFocus();
            return;
        }

        EnvironmentalHealthModel record = new EnvironmentalHealthModel();
        if (currentRecordId > 0) {
            record.setId(currentRecordId);
        }

        record.setHouseholdHeadName(name);
        record.setProfileId(selectedProfileId);

        String PREFS_NAME = "AppPrefs";
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        record.setUserId(userId);

        int selectedWaterId = rgWaterSourceType.getCheckedRadioButtonId();
        record.setWaterLevelI(selectedWaterId == R.id.rbLevelI);
        record.setWaterLevelII(selectedWaterId == R.id.rbLevelII);
        record.setWaterLevelIII(selectedWaterId == R.id.rbLevelIII);
        record.setWaterSourceOthers(selectedWaterId == R.id.rbWaterOthers ? getTextFrom(etWaterSourceOthers) : "");

        record.setWaterLocatedInsideDwelling(rgWaterLocatedInside.getCheckedRadioButtonId() == R.id.rbWaterInsideYes);
        record.setWaterAvailable12Hours(rgWaterAvailable12Hours.getCheckedRadioButtonId() == R.id.rbWater12HoursYes);
        record.setMicrobiologicalTestDate(getTextFrom(etMicrobiologicalTestDate));
        record.setMicrobiologicalTestResult(getRadioValue(rgMicrobiologicalResult, R.id.rbMicrobiologicalPos, 1, R.id.rbMicrobiologicalNeg, 0));
        record.setWaterSafetyPlanOperational(getRadioValue(rgWaterSafetyPlan, R.id.rbWaterSafetyPlanYes, 1, R.id.rbWaterSafetyPlanNo, 0));

        int selectedSanitationId = rgSanitationStatus.getCheckedRadioButtonId();
        if (selectedSanitationId == R.id.rbSanitaryToilet) record.setSanitationStatus("Functional Sanitary");
        else if (selectedSanitationId == R.id.rbUnsanitaryToilet) record.setSanitationStatus("Unsanitary");
        else if (selectedSanitationId == R.id.rbNoToilet) record.setSanitationStatus("No Toilet");
        else record.setSanitationStatus("");

        record.setUnsanitaryToiletType(getUnsanitaryToiletValue());
        record.setToiletShared(getRadioValue(rgToiletShared, R.id.rbToiletSharedYes, 1, R.id.rbToiletSharedNo, 0));
        record.setBasicSanitationFacility(getRadioValue(rgBasicSanitationFacility, R.id.rbBasicFacilityYes, 1, R.id.rbBasicFacilityNo, 0));
        record.setDisposalDate(getTextFrom(etDisposalDate));

        record.setDisposalInSitu(cbDisposalInSitu.isChecked());
        record.setDisposalOffSiteDesludged(cbDisposalOffSiteDesludged.isChecked());
        record.setDisposalOffSiteSewer(cbDisposalOffSiteSewer.isChecked());

        record.setSafelyManagedSanitationService(getRadioValue(rgSafelyManagedSanitation, R.id.rbSafelyManagedSanitationYes, 1, R.id.rbSafelyManagedSanitationNo, 0));
        record.setSafelyManagedDrinkingWater(getRadioValue(rgSafelyManagedDrinkingWater, R.id.rbSafelyManagedDrinkingWaterYes, 1, R.id.rbSafelyManagedDrinkingWaterNo, 0));
        record.setRemarks(getTextFrom(etRemarks));

        Executors.newSingleThreadExecutor().execute(() -> {
            if (currentRecordId > 0) {
                db.environmentalHealthDao().updateRecord(record);
            } else {
                record.setNewInsert(true);
                db.environmentalHealthDao().insertRecord(record);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Environmental Record saved successfully!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
            }
        });
    }

    private void clearFormInputs() {
        etHouseholdHeadName.setText("", false);
        rgWaterSourceType.clearCheck();
        etWaterSourceOthers.setText("");
        rgWaterLocatedInside.clearCheck();
        rgWaterAvailable12Hours.clearCheck();
        etMicrobiologicalTestDate.setText("");
        rgMicrobiologicalResult.clearCheck();
        rgWaterSafetyPlan.clearCheck();
        rgSanitationStatus.clearCheck();
        rgUnsanitaryToilet.clearCheck();
        rgToiletShared.clearCheck();
        rgBasicSanitationFacility.clearCheck();
        etDisposalDate.setText("");
        cbDisposalInSitu.setChecked(false);
        cbDisposalOffSiteDesludged.setChecked(false);
        cbDisposalOffSiteSewer.setChecked(false);
        rgSafelyManagedSanitation.clearCheck();
        rgSafelyManagedDrinkingWater.clearCheck();
        etRemarks.setText("");
        currentRecordId = 0;
        selectedProfileId = -1;
    }

    private String getTextFrom(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String getTextFrom(AutoCompleteTextView et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private int getRadioValue(RadioGroup group, int positiveId, int positiveVal, int negativeId, int negativeVal) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId == positiveId) return positiveVal;
        if (checkedId == negativeId) return negativeVal;
        return -1;
    }

    private void setRadioValue(RadioGroup group, int val, int positiveId, int negativeId) {
        if (val == 1) group.check(positiveId);
        else if (val == 0) group.check(negativeId);
        else group.clearCheck();
    }

    private int getUnsanitaryToiletValue() {
        int checkedId = rgUnsanitaryToilet.getCheckedRadioButtonId();
        if (checkedId == R.id.rbUnsanitary3) return 3;
        if (checkedId == R.id.rbUnsanitary2) return 2;
        if (checkedId == R.id.rbUnsanitary1) return 1;
        if (checkedId == R.id.rbUnsanitary0) return 0;
        return -1;
    }

    private void setUnsanitaryToiletSelection(int val) {
        if (val == 3) rgUnsanitaryToilet.check(R.id.rbUnsanitary3);
        else if (val == 2) rgUnsanitaryToilet.check(R.id.rbUnsanitary2);
        else if (val == 1) rgUnsanitaryToilet.check(R.id.rbUnsanitary1);
        else if (val == 0) rgUnsanitaryToilet.check(R.id.rbUnsanitary0);
        else rgUnsanitaryToilet.clearCheck();
    }

    // Formats typed input as MM-DD-YYYY by inserting dashes after the month and day
    private static class DateFormattingWatcher implements TextWatcher {
        private final TextInputEditText editText;
        private boolean isDeleting = false;

        public DateFormattingWatcher(TextInputEditText editText) {
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
                if (s.length() == 3 || s.length() == 6) {
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
                if ((i == 1 && len > 2) || (i == 3 && len > 4)) {
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