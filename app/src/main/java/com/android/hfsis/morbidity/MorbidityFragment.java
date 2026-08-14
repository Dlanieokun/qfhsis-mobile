package com.android.hfsis.morbidity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.morbidity.MorbidityRecord;

import java.util.Calendar;

public class MorbidityFragment extends Fragment {

    private static final String ARG_RECORD_ID = "record_id";
    private static final int MODE_ADD = 0;
    private static final int MODE_EDIT = 1;

    private int mode = MODE_ADD;
    private int recordId = -1;
    private MorbidityRecord existingRecord;

    // Header fields
    private AutoCompleteTextView spinnerDisease;
    private EditText etIcdCode;
    private AutoCompleteTextView spinnerMonth;
    private AutoCompleteTextView spinnerYear;
    private EditText etBarangay;
    private EditText etMunicipality;
    private EditText etProvince;

    // Age group fields - Male / Female
    private EditText et0to6dMale, et0to6dFemale;
    private EditText et7to28dMale, et7to28dFemale;
    private EditText et29dto11moMale, et29dto11moFemale;
    private EditText et1to4yrsMale, et1to4yrsFemale;
    private EditText et5to9yrsMale, et5to9yrsFemale;
    private EditText et10to14yrsMale, et10to14yrsFemale;
    private EditText et15to19yrsMale, et15to19yrsFemale;
    private EditText et20to24yrsMale, et20to24yrsFemale;
    private EditText et25to29yrsMale, et25to29yrsFemale;
    private EditText et30to34yrsMale, et30to34yrsFemale;
    private EditText et35to39yrsMale, et35to39yrsFemale;
    private EditText et40to44yrsMale, et40to44yrsFemale;
    private EditText et45to49yrsMale, et45to49yrsFemale;
    private EditText et50to54yrsMale, et50to54yrsFemale;
    private EditText et55to59yrsMale, et55to59yrsFemale;
    private EditText et60plusMale, et60plusFemale;

    // Total display fields
    private TextView tvTotalMale, tvTotalFemale, tvGrandTotal;

    private Button btnSave, btnCancel;

    private static final String[] DISEASES = {
            "Cholera", "Typhoid and paratyphoid fevers", "Shigellosis", "Amoebiasis",
            "Diarrhea and gastroenteritis of presumed infectious origin",
            "Other intestinal infectious diseases", "Respiratory tuberculosis",
            "Other tuberculosis", "Plague", "Brucellosis", "Leptospirosis",
            "Leprosy", "Tetanus neonatorum", "Other tetanus", "Diphtheria",
            "Whooping cough", "Meningococcal infection", "Sepsis",
            "Congenital syphilis", "Early syphilis", "Gonococcal infection",
            "Dengue", "Herpesviral infections", "Varicella and zoster",
            "Measles", "Rubella", "Acute hepatitis A", "Acute hepatitis B",
            "Human immunodeficiency virus [HIV] disease", "Mumps",
            "Malaria", "Schistosomiasis", "Filariasis",
            "Iron deficiency anemia", "Diabetes mellitus", "Malnutrition",
            "Obesity", "Essential (primary) hypertension",
            "Acute myocardial infarction", "Heart failure", "Stroke",
            "Pneumonia", "Influenza", "Asthma",
            "Other chronic obstructive pulmonary diseases",
            "Dental caries", "Gastric and duodenal ulcer", "Diseases of appendix",
            "Urinary tract infection, site not specified",
            "Spontaneous abortion", "COVID-19, virus identified",
            "COVID-19, virus not identified",
            "Multisystem inflammatory syndrome associated with COVID-19",
            "SARS", "Vaping Disorder",
            "Fever of unknown origin", "Rabies", "Other"
    };

    private static final String[] ICD_CODES = {
            "A00", "A01", "A03", "A06", "A09", "A02,A04-A05,A07-A08",
            "A15-A16", "A17-A19", "A20", "A23", "A27", "A30", "A33",
            "A34-A35", "A36", "A37", "A39", "A40-A41", "A50", "A51",
            "A54", "A97", "B00", "B01-B02", "B05", "B06", "B15", "B16",
            "B20-B24", "B26", "B50-B54", "B65", "B74",
            "D50", "E10-E14", "E40-E46", "E66", "I10", "I21-I22",
            "I50", "I64", "J12-J18", "J09-J11", "J45-J46", "J44",
            "K02", "K25-K27", "K35-K38", "N39.0",
            "O03", "U07.1", "U07.2", "U10.9", "U04", "U07.0",
            "R50", "A82", ""
    };

    private static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    public static MorbidityFragment newInstance() {
        return new MorbidityFragment();
    }

    public static MorbidityFragment newInstanceForEdit(int recordId) {
        MorbidityFragment fragment = new MorbidityFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RECORD_ID, recordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_RECORD_ID)) {
            recordId = getArguments().getInt(ARG_RECORD_ID, -1);
            if (recordId != -1) {
                mode = MODE_EDIT;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_morbidity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupDropdowns();
        setupTotalCalculations();
        setupButtons();

        if (mode == MODE_EDIT) {
            loadExistingRecord();
        } else {
            setDefaultPeriod();
        }
    }

    private void initViews(View view) {
        spinnerDisease = view.findViewById(R.id.spinner_disease);
        etIcdCode = view.findViewById(R.id.et_icd_code);
        spinnerMonth = view.findViewById(R.id.spinner_month);
        spinnerYear = view.findViewById(R.id.spinner_year);
        etBarangay = view.findViewById(R.id.et_barangay);
        etMunicipality = view.findViewById(R.id.et_municipality);
        etProvince = view.findViewById(R.id.et_province);

        et0to6dMale = view.findViewById(R.id.et_0to6d_male);
        et0to6dFemale = view.findViewById(R.id.et_0to6d_female);
        et7to28dMale = view.findViewById(R.id.et_7to28d_male);
        et7to28dFemale = view.findViewById(R.id.et_7to28d_female);
        et29dto11moMale = view.findViewById(R.id.et_29dto11mo_male);
        et29dto11moFemale = view.findViewById(R.id.et_29dto11mo_female);
        et1to4yrsMale = view.findViewById(R.id.et_1to4yrs_male);
        et1to4yrsFemale = view.findViewById(R.id.et_1to4yrs_female);
        et5to9yrsMale = view.findViewById(R.id.et_5to9yrs_male);
        et5to9yrsFemale = view.findViewById(R.id.et_5to9yrs_female);
        et10to14yrsMale = view.findViewById(R.id.et_10to14yrs_male);
        et10to14yrsFemale = view.findViewById(R.id.et_10to14yrs_female);
        et15to19yrsMale = view.findViewById(R.id.et_15to19yrs_male);
        et15to19yrsFemale = view.findViewById(R.id.et_15to19yrs_female);
        et20to24yrsMale = view.findViewById(R.id.et_20to24yrs_male);
        et20to24yrsFemale = view.findViewById(R.id.et_20to24yrs_female);
        et25to29yrsMale = view.findViewById(R.id.et_25to29yrs_male);
        et25to29yrsFemale = view.findViewById(R.id.et_25to29yrs_female);
        et30to34yrsMale = view.findViewById(R.id.et_30to34yrs_male);
        et30to34yrsFemale = view.findViewById(R.id.et_30to34yrs_female);
        et35to39yrsMale = view.findViewById(R.id.et_35to39yrs_male);
        et35to39yrsFemale = view.findViewById(R.id.et_35to39yrs_female);
        et40to44yrsMale = view.findViewById(R.id.et_40to44yrs_male);
        et40to44yrsFemale = view.findViewById(R.id.et_40to44yrs_female);
        et45to49yrsMale = view.findViewById(R.id.et_45to49yrs_male);
        et45to49yrsFemale = view.findViewById(R.id.et_45to49yrs_female);
        et50to54yrsMale = view.findViewById(R.id.et_50to54yrs_male);
        et50to54yrsFemale = view.findViewById(R.id.et_50to54yrs_female);
        et55to59yrsMale = view.findViewById(R.id.et_55to59yrs_male);
        et55to59yrsFemale = view.findViewById(R.id.et_55to59yrs_female);
        et60plusMale = view.findViewById(R.id.et_60plus_male);
        et60plusFemale = view.findViewById(R.id.et_60plus_female);

        tvTotalMale = view.findViewById(R.id.tv_total_male);
        tvTotalFemale = view.findViewById(R.id.tv_total_female);
        tvGrandTotal = view.findViewById(R.id.tv_grand_total);

        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }

    private void setupDropdowns() {
        ArrayAdapter<String> diseaseAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, DISEASES);
        spinnerDisease.setAdapter(diseaseAdapter);
        spinnerDisease.setOnItemClickListener((parent, v, position, id) -> {
            if (position < ICD_CODES.length) {
                etIcdCode.setText(ICD_CODES[position]);
            }
        });

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, MONTHS);
        spinnerMonth.setAdapter(monthAdapter);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, years);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void setupTotalCalculations() {
        View.OnFocusChangeListener listener = (v, hasFocus) -> {
            if (!hasFocus) recalculateTotals();
        };

        EditText[] allFields = {
                et0to6dMale, et0to6dFemale, et7to28dMale, et7to28dFemale,
                et29dto11moMale, et29dto11moFemale, et1to4yrsMale, et1to4yrsFemale,
                et5to9yrsMale, et5to9yrsFemale, et10to14yrsMale, et10to14yrsFemale,
                et15to19yrsMale, et15to19yrsFemale, et20to24yrsMale, et20to24yrsFemale,
                et25to29yrsMale, et25to29yrsFemale, et30to34yrsMale, et30to34yrsFemale,
                et35to39yrsMale, et35to39yrsFemale, et40to44yrsMale, et40to44yrsFemale,
                et45to49yrsMale, et45to49yrsFemale, et50to54yrsMale, et50to54yrsFemale,
                et55to59yrsMale, et55to59yrsFemale, et60plusMale, et60plusFemale
        };
        for (EditText et : allFields) {
            et.setOnFocusChangeListener(listener);
        }
    }

    private void recalculateTotals() {
        int totalMale = safeInt(et0to6dMale) + safeInt(et7to28dMale) +
                safeInt(et29dto11moMale) + safeInt(et1to4yrsMale) +
                safeInt(et5to9yrsMale) + safeInt(et10to14yrsMale) +
                safeInt(et15to19yrsMale) + safeInt(et20to24yrsMale) +
                safeInt(et25to29yrsMale) + safeInt(et30to34yrsMale) +
                safeInt(et35to39yrsMale) + safeInt(et40to44yrsMale) +
                safeInt(et45to49yrsMale) + safeInt(et50to54yrsMale) +
                safeInt(et55to59yrsMale) + safeInt(et60plusMale);

        int totalFemale = safeInt(et0to6dFemale) + safeInt(et7to28dFemale) +
                safeInt(et29dto11moFemale) + safeInt(et1to4yrsFemale) +
                safeInt(et5to9yrsFemale) + safeInt(et10to14yrsFemale) +
                safeInt(et15to19yrsFemale) + safeInt(et20to24yrsFemale) +
                safeInt(et25to29yrsFemale) + safeInt(et30to34yrsFemale) +
                safeInt(et35to39yrsFemale) + safeInt(et40to44yrsFemale) +
                safeInt(et45to49yrsFemale) + safeInt(et50to54yrsFemale) +
                safeInt(et55to59yrsFemale) + safeInt(et60plusFemale);

        tvTotalMale.setText(String.valueOf(totalMale));
        tvTotalFemale.setText(String.valueOf(totalFemale));
        tvGrandTotal.setText(String.valueOf(totalMale + totalFemale));
    }

    private int safeInt(EditText et) {
        String s = et.getText().toString().trim();
        if (TextUtils.isEmpty(s)) return 0;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    private void setupButtons() {
        String label = mode == MODE_EDIT ? "Update Record" : "Save Record";
        btnSave.setText(label);

        btnSave.setOnClickListener(v -> saveRecord());
        btnCancel.setOnClickListener(v -> {
            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setDefaultPeriod() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        spinnerMonth.setText(MONTHS[month], false);
        spinnerYear.setText(String.valueOf(year), false);
    }

    private void loadExistingRecord() {
        new Thread(() -> {
            existingRecord = DatabaseHelper.getInstance(requireContext())
                    .morbidityDao().getRecordById(recordId);
            if (existingRecord != null && isAdded()) {
                requireActivity().runOnUiThread(this::populateFields);
            }
        }).start();
    }

    private void populateFields() {
        if (existingRecord == null) return;
        spinnerDisease.setText(existingRecord.getDiseaseName(), false);
        etIcdCode.setText(existingRecord.getIcdCode());
        spinnerMonth.setText(existingRecord.getReportMonth(), false);
        spinnerYear.setText(existingRecord.getReportYear(), false);
        etBarangay.setText(existingRecord.getBarangay());
        etMunicipality.setText(existingRecord.getMunicipality());
        etProvince.setText(existingRecord.getProvince());

        setField(et0to6dMale, existingRecord.getAge0to6daysMale());
        setField(et0to6dFemale, existingRecord.getAge0to6daysFemale());
        setField(et7to28dMale, existingRecord.getAge7to28daysMale());
        setField(et7to28dFemale, existingRecord.getAge7to28daysFemale());
        setField(et29dto11moMale, existingRecord.getAge29daysto11moMale());
        setField(et29dto11moFemale, existingRecord.getAge29daysto11moFemale());
        setField(et1to4yrsMale, existingRecord.getAge1to4yrsMale());
        setField(et1to4yrsFemale, existingRecord.getAge1to4yrsFemale());
        setField(et5to9yrsMale, existingRecord.getAge5to9yrsMale());
        setField(et5to9yrsFemale, existingRecord.getAge5to9yrsFemale());
        setField(et10to14yrsMale, existingRecord.getAge10to14yrsMale());
        setField(et10to14yrsFemale, existingRecord.getAge10to14yrsFemale());
        setField(et15to19yrsMale, existingRecord.getAge15to19yrsMale());
        setField(et15to19yrsFemale, existingRecord.getAge15to19yrsFemale());
        setField(et20to24yrsMale, existingRecord.getAge20to24yrsMale());
        setField(et20to24yrsFemale, existingRecord.getAge20to24yrsFemale());
        setField(et25to29yrsMale, existingRecord.getAge25to29yrsMale());
        setField(et25to29yrsFemale, existingRecord.getAge25to29yrsFemale());
        setField(et30to34yrsMale, existingRecord.getAge30to34yrsMale());
        setField(et30to34yrsFemale, existingRecord.getAge30to34yrsFemale());
        setField(et35to39yrsMale, existingRecord.getAge35to39yrsMale());
        setField(et35to39yrsFemale, existingRecord.getAge35to39yrsFemale());
        setField(et40to44yrsMale, existingRecord.getAge40to44yrsMale());
        setField(et40to44yrsFemale, existingRecord.getAge40to44yrsFemale());
        setField(et45to49yrsMale, existingRecord.getAge45to49yrsMale());
        setField(et45to49yrsFemale, existingRecord.getAge45to49yrsFemale());
        setField(et50to54yrsMale, existingRecord.getAge50to54yrsMale());
        setField(et50to54yrsFemale, existingRecord.getAge50to54yrsFemale());
        setField(et55to59yrsMale, existingRecord.getAge55to59yrsMale());
        setField(et55to59yrsFemale, existingRecord.getAge55to59yrsFemale());
        setField(et60plusMale, existingRecord.getAge60plusMale());
        setField(et60plusFemale, existingRecord.getAge60plusFemale());

        recalculateTotals();
    }

    private void setField(EditText et, int value) {
        et.setText(value > 0 ? String.valueOf(value) : "");
    }

    private void saveRecord() {
        if (!validateForm()) return;

        recalculateTotals();

        MorbidityRecord record = (mode == MODE_EDIT && existingRecord != null)
                ? existingRecord : new MorbidityRecord();

        record.setDiseaseName(spinnerDisease.getText().toString().trim());
        record.setIcdCode(etIcdCode.getText().toString().trim());
        record.setReportMonth(spinnerMonth.getText().toString().trim());
        record.setReportYear(spinnerYear.getText().toString().trim());
        record.setBarangay(etBarangay.getText().toString().trim());
        record.setMunicipality(etMunicipality.getText().toString().trim());
        record.setProvince(etProvince.getText().toString().trim());

        record.setAge0to6daysMale(safeInt(et0to6dMale));
        record.setAge0to6daysFemale(safeInt(et0to6dFemale));
        record.setAge7to28daysMale(safeInt(et7to28dMale));
        record.setAge7to28daysFemale(safeInt(et7to28dFemale));
        record.setAge29daysto11moMale(safeInt(et29dto11moMale));
        record.setAge29daysto11moFemale(safeInt(et29dto11moFemale));
        record.setAge1to4yrsMale(safeInt(et1to4yrsMale));
        record.setAge1to4yrsFemale(safeInt(et1to4yrsFemale));
        record.setAge5to9yrsMale(safeInt(et5to9yrsMale));
        record.setAge5to9yrsFemale(safeInt(et5to9yrsFemale));
        record.setAge10to14yrsMale(safeInt(et10to14yrsMale));
        record.setAge10to14yrsFemale(safeInt(et10to14yrsFemale));
        record.setAge15to19yrsMale(safeInt(et15to19yrsMale));
        record.setAge15to19yrsFemale(safeInt(et15to19yrsFemale));
        record.setAge20to24yrsMale(safeInt(et20to24yrsMale));
        record.setAge20to24yrsFemale(safeInt(et20to24yrsFemale));
        record.setAge25to29yrsMale(safeInt(et25to29yrsMale));
        record.setAge25to29yrsFemale(safeInt(et25to29yrsFemale));
        record.setAge30to34yrsMale(safeInt(et30to34yrsMale));
        record.setAge30to34yrsFemale(safeInt(et30to34yrsFemale));
        record.setAge35to39yrsMale(safeInt(et35to39yrsMale));
        record.setAge35to39yrsFemale(safeInt(et35to39yrsFemale));
        record.setAge40to44yrsMale(safeInt(et40to44yrsMale));
        record.setAge40to44yrsFemale(safeInt(et40to44yrsFemale));
        record.setAge45to49yrsMale(safeInt(et45to49yrsMale));
        record.setAge45to49yrsFemale(safeInt(et45to49yrsFemale));
        record.setAge50to54yrsMale(safeInt(et50to54yrsMale));
        record.setAge50to54yrsFemale(safeInt(et50to54yrsFemale));
        record.setAge55to59yrsMale(safeInt(et55to59yrsMale));
        record.setAge55to59yrsFemale(safeInt(et55to59yrsFemale));
        record.setAge60plusMale(safeInt(et60plusMale));
        record.setAge60plusFemale(safeInt(et60plusFemale));
        record.setUpdatedAt(System.currentTimeMillis());
        record.setSynced(false);

        new Thread(() -> {
            if (mode == MODE_EDIT) {
                DatabaseHelper.getInstance(requireContext()).morbidityDao().update(record);
            } else {
                DatabaseHelper.getInstance(requireContext()).morbidityDao().insert(record);
            }
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    String msg = mode == MODE_EDIT ? "Record updated successfully" : "Record saved successfully";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        }).start();
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(spinnerDisease.getText())) {
            spinnerDisease.setError("Please select a disease");
            spinnerDisease.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(spinnerMonth.getText())) {
            spinnerMonth.setError("Please select a month");
            spinnerMonth.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(spinnerYear.getText())) {
            spinnerYear.setError("Please select a year");
            spinnerYear.requestFocus();
            return false;
        }
        return true;
    }
}