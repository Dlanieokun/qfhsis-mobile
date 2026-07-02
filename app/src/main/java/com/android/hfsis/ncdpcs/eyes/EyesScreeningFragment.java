package com.android.hfsis.ncdpcs.eyes;

import android.app.DatePickerDialog;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.android.hfsis.model.ncdpcs.EyesScreeningsData;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class EyesScreeningFragment extends Fragment {

    // ── Client Information ──────────────────────────────────────────────────
    private EditText etDateScreening, etFamilySerial, etAddress, etDateOfBirth, etAge;
    private TextInputLayout tilName, tilDateScreening, tilDateOfBirth;
    private AutoCompleteTextView etName;
    private AutoCompleteTextView spinnerAgeGroup;
    private RadioGroup rgSex;
    private RadioButton rbMale, rbFemale;

    // ── Screening Results ────────────────────────────────────────────────────
    private AutoCompleteTextView spinnerScreened;
    private AutoCompleteTextView spinnerEyeDisease;
    private TextView tvDiseaseDetail;
    private EditText etDateReferred;
    private TextInputLayout tilDateReferred;

    // ── Remarks ──────────────────────────────────────────────────────────────
    private EditText etRemarks;
    private Button btnSubmit;

    // State management tracking for record modifications
    private long existingRecordId = 0;
    private int selectedProfileId = 0;

    private static final String[] AGE_ITEMS = {
            "Select Age Group", "A – 0 to 9 years old", "B – 10 to 19 years old", "C – 20 to 59 years old", "D – 60 years old and above"
    };
    private static final String[] SCREENED_ITEMS = {"Select", "1 – Yes", "0 – No"};
    private static final String[] DISEASE_ITEMS = {
            "Select category", "0 – No eye disease identified", "A – Changes in Vision", "B – Changes in Appearance", "C – Eye and Orbital Injury", "D – Routine Eye Exam"
    };
    private static final String[] DISEASE_DETAILS = {
            "", "No eye disease was identified during screening.",
            "Conditions: Error of Refraction (EOR), Cataract, Glaucoma, Age-related Macular Degeneration (AMD).",
            "Conditions: Strabismus, Pterygium, Eye Mass/Tumor, Conjunctivitis, Blepharitis, Subconjunctival Hemorrhage, Hordeolum/Stye, Retinoblastoma.",
            "Conditions: Trauma, Chemical Burns, Foreign Body, Retinal Detachment.",
            "Conditions: Routine Eye Exam, Retinopathy of Prematurity (ROP) Screening, Diabetic Retinopathy Screening."
    };

    public static EyesScreeningFragment newInstance() {
        return new EyesScreeningFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_eyes_screening, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupNameAutocomplete();
        setupDropdowns();
        setupDatePickersAndFormatters();
        setupSubmitButton();

        // Check if an ID was passed down for editing
        if (getArguments() != null && getArguments().containsKey("EDIT_RECORD_ID")) {
            existingRecordId = getArguments().getLong("EDIT_RECORD_ID");
            loadExistingRecordData(existingRecordId);
        }else {
            // Automatically set the current date for a new maternal record
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            etDateScreening.setText(sdf.format(new Date()));
        }
    }

    private void bindViews(View v) {
        tilDateScreening = v.findViewById(R.id.tilDateScreening);
        etDateScreening  = v.findViewById(R.id.etDateScreening);
        etFamilySerial   = v.findViewById(R.id.etFamilySerial);
        tilName          = v.findViewById(R.id.tilName);
        etName           = v.findViewById(R.id.etName);
        etAddress        = v.findViewById(R.id.etAddress);
        tilDateOfBirth   = v.findViewById(R.id.tilDateOfBirth);
        etDateOfBirth    = v.findViewById(R.id.etDateOfBirth);
        etAge            = v.findViewById(R.id.etAge);
        spinnerAgeGroup  = v.findViewById(R.id.spinnerAgeGroup);
        rgSex            = v.findViewById(R.id.rgSex);
        rbMale           = v.findViewById(R.id.rbMale);
        rbFemale         = v.findViewById(R.id.rbFemale);

        spinnerScreened   = v.findViewById(R.id.spinnerScreened);
        spinnerEyeDisease = v.findViewById(R.id.spinnerEyeDisease);
        tvDiseaseDetail   = v.findViewById(R.id.tvDiseaseDetail);
        tilDateReferred   = v.findViewById(R.id.tilDateReferred);
        etDateReferred    = v.findViewById(R.id.etDateReferred);

        etRemarks = v.findViewById(R.id.etRemarks);
        btnSubmit = v.findViewById(R.id.btnSubmit);
    }

    private void setupNameAutocomplete() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> namesList = dbHelper.householdProfileDao().getAllHouseholdNames();
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

        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    selectedProfileId = 0;
                }
            }
        });
    }

    private void autoPopulateFromProfile(String fullCalculatedName) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = dbHelper.householdProfileDao().getProfileByCalculatedName(fullCalculatedName);
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
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

                    if (!TextUtils.isEmpty(profile.hhNumber)) {
                        etFamilySerial.setText(profile.hhNumber);
                    }

                    if ("M".equalsIgnoreCase(profile.sex) || "Male".equalsIgnoreCase(profile.sex)) {
                        rbMale.setChecked(true);
                    } else if ("F".equalsIgnoreCase(profile.sex) || "Female".equalsIgnoreCase(profile.sex)) {
                        rbFemale.setChecked(true);
                    }

                    if (!TextUtils.isEmpty(profile.dob)) {
                        etDateOfBirth.setText(profile.dob);
                        try {
                            // Updated profile parsing split configuration to accommodate YYYY-MM-DD layout indices
                            String[] dobParts = profile.dob.split("-");
                            if (dobParts.length == 3) {
                                int year = Integer.parseInt(dobParts[0]);
                                int month = Integer.parseInt(dobParts[1]) - 1;
                                int day = Integer.parseInt(dobParts[2]);
                                calculateAgeAndGroup(year, month, day);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void calculateAgeAndGroup(int birthYear, int birthMonth, int birthDay) {
        Calendar dob = Calendar.getInstance();
        dob.set(birthYear, birthMonth, birthDay);
        Calendar today = Calendar.getInstance();

        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
        if (months < 0 || (months == 0 && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))) {
            years--;
        }

        int totalYears = Math.max(0, years);
        etAge.setText(String.valueOf(totalYears));

        if (totalYears >= 60) {
            spinnerAgeGroup.setText(AGE_ITEMS[4], false);
        } else if (totalYears >= 20) {
            spinnerAgeGroup.setText(AGE_ITEMS[3], false);
        } else if (totalYears >= 10) {
            spinnerAgeGroup.setText(AGE_ITEMS[2], false);
        } else {
            spinnerAgeGroup.setText(AGE_ITEMS[1], false);
        }
    }

    private void setupDropdowns() {
        setDropdownAdapter(spinnerAgeGroup, AGE_ITEMS);
        setDropdownAdapter(spinnerScreened, SCREENED_ITEMS);
        setDropdownAdapter(spinnerEyeDisease, DISEASE_ITEMS);

        spinnerEyeDisease.setOnItemClickListener((parent, view, position, id) -> {
            updateDiseaseDetailMessage(position);
        });
    }

    private void updateDiseaseDetailMessage(int position) {
        if (position >= 0 && position < DISEASE_DETAILS.length) {
            String detail = DISEASE_DETAILS[position];
            tvDiseaseDetail.setText(detail.isEmpty() ? "Select a category above to see included conditions." : detail);
        }
    }

    private void setDropdownAdapter(AutoCompleteTextView menu, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, items);
        menu.setAdapter(adapter);
        if (items.length > 0) {
            menu.setText(items[0], false);
        }
    }

    private void setupDatePickersAndFormatters() {
        tilDateScreening.setStartIconOnClickListener(v -> showDatePickerDialog(etDateScreening));
        tilDateOfBirth.setStartIconOnClickListener(v -> showDatePickerDialog(etDateOfBirth));
        tilDateReferred.setStartIconOnClickListener(v -> showDatePickerDialog(etDateReferred));

        etDateScreening.addTextChangedListener(new DateFormattingWatcher(etDateScreening));
        etDateOfBirth.addTextChangedListener(new DateFormattingWatcher(etDateOfBirth));
        etDateReferred.addTextChangedListener(new DateFormattingWatcher(etDateReferred));
    }

    private void showDatePickerDialog(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, selYear, selMonth, selDay) -> {
            Calendar selCalendar = Calendar.getInstance();
            selCalendar.set(Calendar.YEAR, selYear);
            selCalendar.set(Calendar.MONTH, selMonth);
            selCalendar.set(Calendar.DAY_OF_MONTH, selDay);

            // Modified default UI formatter pattern sequence
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            targetEditText.setText(sdf.format(selCalendar.getTime()));
            targetEditText.setError(null);
        }, year, month, day);

        dialog.show();
    }

    private void loadExistingRecordData(long id) {
        new Thread(() -> {
            try {
                DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
                EyesScreeningsData record = db.eyesScreeningDao().getScreeningById(id);

                if (record != null && isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> populateFormFields(record));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populateFormFields(EyesScreeningsData data) {
        selectedProfileId = data.getProfileId();
        etDateScreening.setText(data.getDateScreening());
        etFamilySerial.setText(data.getFamilySerial());
        etName.setText(data.getName(), false);
        etAddress.setText(data.getAddress());
        etDateOfBirth.setText(data.getDateOfBirth());
        etAge.setText(data.getAge());

        String ageGroupCode = data.getAgeGroup();
        for (String item : AGE_ITEMS) {
            if (item.startsWith(ageGroupCode + " –") || item.startsWith(ageGroupCode + " —")) {
                spinnerAgeGroup.setText(item, false);
                break;
            }
        }

        if ("M".equals(data.getSex())) {
            rbMale.setChecked(true);
        } else if ("F".equals(data.getSex())) {
            rbFemale.setChecked(true);
        }

        int screenedVal = data.getScreened();
        spinnerScreened.setText(screenedVal == 1 ? SCREENED_ITEMS[1] : SCREENED_ITEMS[2], false);

        String code = data.getEyeDiseaseCode();
        int diseaseIndex = 0;
        if ("0".equals(code)) diseaseIndex = 1;
        else if ("A".equals(code)) diseaseIndex = 2;
        else if ("B".equals(code)) diseaseIndex = 3;
        else if ("C".equals(code)) diseaseIndex = 4;
        else if ("D".equals(code)) diseaseIndex = 5;

        if (diseaseIndex > 0) {
            spinnerEyeDisease.setText(DISEASE_ITEMS[diseaseIndex], false);
            updateDiseaseDetailMessage(diseaseIndex);
        }

        etDateReferred.setText(data.getDateReferred());
        etRemarks.setText(data.getRemarks());

        btnSubmit.setText("UPDATE SCREENING");
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                EyesScreeningsData data = collectFormData();
                onScreeningSubmitted(data);
            }
        });
    }

    private boolean validateForm() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Client name is required");
            etName.requestFocus();
            return false;
        }

        String screeningDate = etDateScreening.getText().toString().trim();
        if (!isValidDateFormat(screeningDate)) {
            etDateScreening.setError("Required format is YYYY-MM-DD");
            etDateScreening.requestFocus();
            return false;
        }

        String dobDate = etDateOfBirth.getText().toString().trim();
        if (!dobDate.isEmpty() && !isValidDateFormat(dobDate)) {
            etDateOfBirth.setError("Invalid date format (YYYY-MM-DD)");
            etDateOfBirth.requestFocus();
            return false;
        }

        String refDate = etDateReferred.getText().toString().trim();
        if (!refDate.isEmpty() && !isValidDateFormat(refDate)) {
            etDateReferred.setError("Invalid date format (YYYY-MM-DD)");
            etDateReferred.requestFocus();
            return false;
        }

        if (Arrays.asList(AGE_ITEMS).indexOf(spinnerAgeGroup.getText().toString()) <= 0) {
            Toast.makeText(requireContext(), "Please select an Age Group", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (rgSex.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Please select Sex", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Arrays.asList(SCREENED_ITEMS).indexOf(spinnerScreened.getText().toString()) <= 0) {
            Toast.makeText(requireContext(), "Please indicate whether the client was screened", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidDateFormat(String date) {
        // Adjusted regex expression filter pattern validation constraint structure matches matching YYYY-MM-DD ISO structures
        return date.matches("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])");
    }

    private EyesScreeningsData collectFormData() {
        EyesScreeningsData d = new EyesScreeningsData();

        d.setId(existingRecordId);
        d.setProfileId(selectedProfileId);

        d.setDateScreening(etDateScreening.getText().toString().trim());
        d.setFamilySerial(etFamilySerial.getText().toString().trim());
        d.setName(etName.getText().toString().trim());
        d.setAddress(etAddress.getText().toString().trim());
        d.setDateOfBirth(etDateOfBirth.getText().toString().trim());
        d.setAge(etAge.getText().toString().trim());

        String rawAgeGroup = spinnerAgeGroup.getText().toString();
        d.setAgeGroup(rawAgeGroup.startsWith("Select") ? "" : rawAgeGroup.substring(0, 1));

        d.setSex(rbMale.isChecked() ? "M" : (rbFemale.isChecked() ? "F" : ""));

        int screenedPos = Arrays.asList(SCREENED_ITEMS).indexOf(spinnerScreened.getText().toString());
        d.setScreened(screenedPos == 1 ? 1 : 0);

        int diseasePos = Arrays.asList(DISEASE_ITEMS).indexOf(spinnerEyeDisease.getText().toString());
        d.setEyeDiseaseCode(resolveDiseaseCode(diseasePos));
        d.setDateReferred(etDateReferred.getText().toString().trim());
        d.setRemarks(etRemarks.getText().toString().trim());

        return d;
    }

    private String resolveDiseaseCode(int position) {
        switch (position) {
            case 1:  return "0";
            case 2:  return "A";
            case 3:  return "B";
            case 4:  return "C";
            case 5:  return "D";
            default: return "";
        }
    }

    private void onScreeningSubmitted(EyesScreeningsData data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatabaseHelper db = DatabaseHelper.getInstance(requireContext());

                    if (data.getId() == 0) {
                        long newId = db.eyesScreeningDao().insert(data);
                        data.setId(newId);
                    } else {
                        db.eyesScreeningDao().update(data);
                    }

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(requireContext(),
                                        "Screening saved successfully for: " + data.getName(),
                                        Toast.LENGTH_LONG).show();

                                getParentFragmentManager().popBackStack();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(requireContext(),
                                        "Error saving record to database",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        }).start();
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
            // Modified length positions to correctly erase hyphens for YYYY-MM-DD strings on backspace
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
                // Automatically injects hyphens dynamically after index 3 (YYYY-) and index 5 (YYYY-MM-)
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