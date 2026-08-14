package com.android.hfsis.morbidity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.morbidity.MorbidityRecord;

public class MorbidityDetailFragment extends Fragment {

    private static final String ARG_RECORD_ID = "record_id";
    private int recordId;
    private MorbidityRecord record;

    // Header TextViews
    private TextView tvDiseaseName, tvIcdCode, tvPeriod, tvBarangay, tvMunicipality, tvProvince;

    // Age group TextViews
    private TextView tv0to6dM, tv0to6dF, tv0to6dT;
    private TextView tv7to28dM, tv7to28dF, tv7to28dT;
    private TextView tv29dto11moM, tv29dto11moF, tv29dto11moT;
    private TextView tv1to4yrsM, tv1to4yrsF, tv1to4yrsT;
    private TextView tv5to9yrsM, tv5to9yrsF, tv5to9yrsT;
    private TextView tv10to14yrsM, tv10to14yrsF, tv10to14yrsT;
    private TextView tv15to19yrsM, tv15to19yrsF, tv15to19yrsT;
    private TextView tv20to24yrsM, tv20to24yrsF, tv20to24yrsT;
    private TextView tv25to29yrsM, tv25to29yrsF, tv25to29yrsT;
    private TextView tv30to34yrsM, tv30to34yrsF, tv30to34yrsT;
    private TextView tv35to39yrsM, tv35to39yrsF, tv35to39yrsT;
    private TextView tv40to44yrsM, tv40to44yrsF, tv40to44yrsT;
    private TextView tv45to49yrsM, tv45to49yrsF, tv45to49yrsT;
    private TextView tv50to54yrsM, tv50to54yrsF, tv50to54yrsT;
    private TextView tv55to59yrsM, tv55to59yrsF, tv55to59yrsT;
    private TextView tv60plusM, tv60plusF, tv60plusT;
    private TextView tvGrandTotalM, tvGrandTotalF, tvGrandTotalBoth;

    private Button btnEdit, btnBack;

    public static MorbidityDetailFragment newInstance(int recordId) {
        MorbidityDetailFragment fragment = new MorbidityDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RECORD_ID, recordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recordId = getArguments().getInt(ARG_RECORD_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_morbidity_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadRecord();
    }

    private void initViews(View view) {
        tvDiseaseName = view.findViewById(R.id.tv_disease_name);
        tvIcdCode = view.findViewById(R.id.tv_icd_code);
        tvPeriod = view.findViewById(R.id.tv_period);
        tvBarangay = view.findViewById(R.id.tv_barangay);
        tvMunicipality = view.findViewById(R.id.tv_municipality);
        tvProvince = view.findViewById(R.id.tv_province);

        tv0to6dM = view.findViewById(R.id.tv_0to6d_m); tv0to6dF = view.findViewById(R.id.tv_0to6d_f); tv0to6dT = view.findViewById(R.id.tv_0to6d_t);
        tv7to28dM = view.findViewById(R.id.tv_7to28d_m); tv7to28dF = view.findViewById(R.id.tv_7to28d_f); tv7to28dT = view.findViewById(R.id.tv_7to28d_t);
        tv29dto11moM = view.findViewById(R.id.tv_29dto11mo_m); tv29dto11moF = view.findViewById(R.id.tv_29dto11mo_f); tv29dto11moT = view.findViewById(R.id.tv_29dto11mo_t);
        tv1to4yrsM = view.findViewById(R.id.tv_1to4yrs_m); tv1to4yrsF = view.findViewById(R.id.tv_1to4yrs_f); tv1to4yrsT = view.findViewById(R.id.tv_1to4yrs_t);
        tv5to9yrsM = view.findViewById(R.id.tv_5to9yrs_m); tv5to9yrsF = view.findViewById(R.id.tv_5to9yrs_f); tv5to9yrsT = view.findViewById(R.id.tv_5to9yrs_t);
        tv10to14yrsM = view.findViewById(R.id.tv_10to14yrs_m); tv10to14yrsF = view.findViewById(R.id.tv_10to14yrs_f); tv10to14yrsT = view.findViewById(R.id.tv_10to14yrs_t);
        tv15to19yrsM = view.findViewById(R.id.tv_15to19yrs_m); tv15to19yrsF = view.findViewById(R.id.tv_15to19yrs_f); tv15to19yrsT = view.findViewById(R.id.tv_15to19yrs_t);
        tv20to24yrsM = view.findViewById(R.id.tv_20to24yrs_m); tv20to24yrsF = view.findViewById(R.id.tv_20to24yrs_f); tv20to24yrsT = view.findViewById(R.id.tv_20to24yrs_t);
        tv25to29yrsM = view.findViewById(R.id.tv_25to29yrs_m); tv25to29yrsF = view.findViewById(R.id.tv_25to29yrs_f); tv25to29yrsT = view.findViewById(R.id.tv_25to29yrs_t);
        tv30to34yrsM = view.findViewById(R.id.tv_30to34yrs_m); tv30to34yrsF = view.findViewById(R.id.tv_30to34yrs_f); tv30to34yrsT = view.findViewById(R.id.tv_30to34yrs_t);
        tv35to39yrsM = view.findViewById(R.id.tv_35to39yrs_m); tv35to39yrsF = view.findViewById(R.id.tv_35to39yrs_f); tv35to39yrsT = view.findViewById(R.id.tv_35to39yrs_t);
        tv40to44yrsM = view.findViewById(R.id.tv_40to44yrs_m); tv40to44yrsF = view.findViewById(R.id.tv_40to44yrs_f); tv40to44yrsT = view.findViewById(R.id.tv_40to44yrs_t);
        tv45to49yrsM = view.findViewById(R.id.tv_45to49yrs_m); tv45to49yrsF = view.findViewById(R.id.tv_45to49yrs_f); tv45to49yrsT = view.findViewById(R.id.tv_45to49yrs_t);
        tv50to54yrsM = view.findViewById(R.id.tv_50to54yrs_m); tv50to54yrsF = view.findViewById(R.id.tv_50to54yrs_f); tv50to54yrsT = view.findViewById(R.id.tv_50to54yrs_t);
        tv55to59yrsM = view.findViewById(R.id.tv_55to59yrs_m); tv55to59yrsF = view.findViewById(R.id.tv_55to59yrs_f); tv55to59yrsT = view.findViewById(R.id.tv_55to59yrs_t);
        tv60plusM = view.findViewById(R.id.tv_60plus_m); tv60plusF = view.findViewById(R.id.tv_60plus_f); tv60plusT = view.findViewById(R.id.tv_60plus_t);
        tvGrandTotalM = view.findViewById(R.id.tv_grand_total_m);
        tvGrandTotalF = view.findViewById(R.id.tv_grand_total_f);
        tvGrandTotalBoth = view.findViewById(R.id.tv_grand_total_both);

        btnEdit = view.findViewById(R.id.btn_edit);
        btnBack = view.findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> {
            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void loadRecord() {
        new Thread(() -> {
            record = DatabaseHelper.getInstance(requireContext())
                    .morbidityDao().getRecordById(recordId);
            if (record != null && isAdded()) {
                requireActivity().runOnUiThread(this::populateViews);
            }
        }).start();
    }

    private void populateViews() {
        if (record == null) return;

        tvDiseaseName.setText(record.getDiseaseName());
        tvIcdCode.setText("ICD Code: " + record.getIcdCode());
        tvPeriod.setText(record.getReportMonth() + " " + record.getReportYear());
        tvBarangay.setText(record.getBarangay() != null ? record.getBarangay() : "—");
        tvMunicipality.setText(record.getMunicipality() != null ? record.getMunicipality() : "—");
        tvProvince.setText(record.getProvince() != null ? record.getProvince() : "—");

        setRow(tv0to6dM, tv0to6dF, tv0to6dT, record.getAge0to6daysMale(), record.getAge0to6daysFemale());
        setRow(tv7to28dM, tv7to28dF, tv7to28dT, record.getAge7to28daysMale(), record.getAge7to28daysFemale());
        setRow(tv29dto11moM, tv29dto11moF, tv29dto11moT, record.getAge29daysto11moMale(), record.getAge29daysto11moFemale());
        setRow(tv1to4yrsM, tv1to4yrsF, tv1to4yrsT, record.getAge1to4yrsMale(), record.getAge1to4yrsFemale());
        setRow(tv5to9yrsM, tv5to9yrsF, tv5to9yrsT, record.getAge5to9yrsMale(), record.getAge5to9yrsFemale());
        setRow(tv10to14yrsM, tv10to14yrsF, tv10to14yrsT, record.getAge10to14yrsMale(), record.getAge10to14yrsFemale());
        setRow(tv15to19yrsM, tv15to19yrsF, tv15to19yrsT, record.getAge15to19yrsMale(), record.getAge15to19yrsFemale());
        setRow(tv20to24yrsM, tv20to24yrsF, tv20to24yrsT, record.getAge20to24yrsMale(), record.getAge20to24yrsFemale());
        setRow(tv25to29yrsM, tv25to29yrsF, tv25to29yrsT, record.getAge25to29yrsMale(), record.getAge25to29yrsFemale());
        setRow(tv30to34yrsM, tv30to34yrsF, tv30to34yrsT, record.getAge30to34yrsMale(), record.getAge30to34yrsFemale());
        setRow(tv35to39yrsM, tv35to39yrsF, tv35to39yrsT, record.getAge35to39yrsMale(), record.getAge35to39yrsFemale());
        setRow(tv40to44yrsM, tv40to44yrsF, tv40to44yrsT, record.getAge40to44yrsMale(), record.getAge40to44yrsFemale());
        setRow(tv45to49yrsM, tv45to49yrsF, tv45to49yrsT, record.getAge45to49yrsMale(), record.getAge45to49yrsFemale());
        setRow(tv50to54yrsM, tv50to54yrsF, tv50to54yrsT, record.getAge50to54yrsMale(), record.getAge50to54yrsFemale());
        setRow(tv55to59yrsM, tv55to59yrsF, tv55to59yrsT, record.getAge55to59yrsMale(), record.getAge55to59yrsFemale());
        setRow(tv60plusM, tv60plusF, tv60plusT, record.getAge60plusMale(), record.getAge60plusFemale());

        tvGrandTotalM.setText(String.valueOf(record.getTotalMale()));
        tvGrandTotalF.setText(String.valueOf(record.getTotalFemale()));
        tvGrandTotalBoth.setText(String.valueOf(record.getGrandTotal()));

        btnEdit.setOnClickListener(v -> {
            Fragment editFragment = MorbidityFragment.newInstanceForEdit(record.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, editFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setRow(TextView tvM, TextView tvF, TextView tvT, int male, int female) {
        tvM.setText(String.valueOf(male));
        tvF.setText(String.valueOf(female));
        tvT.setText(String.valueOf(male + female));
    }
}