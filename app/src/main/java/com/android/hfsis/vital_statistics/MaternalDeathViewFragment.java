package com.android.hfsis.vital_statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.vital_statistics.MaternalDeathRecord;

public class MaternalDeathViewFragment extends Fragment {

    private TextView tvFullName, tvDate, tvAddress, tvAge, tvAgeGroup, tvPlace, tvCause, tvRemarks;
    private Button btnEdit, btnDelete;
    private DatabaseHelper db;
    private MaternalDeathRecord record;
    private int recordId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_maternal_death, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(getContext());

        // Initialize TextViews
        tvFullName = view.findViewById(R.id.tvFullName);
        tvDate = view.findViewById(R.id.tvDate);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvAge = view.findViewById(R.id.tvAge);
        tvAgeGroup = view.findViewById(R.id.tvAgeGroup);
        tvPlace = view.findViewById(R.id.tvPlace);
        tvCause = view.findViewById(R.id.tvCause);
        tvRemarks = view.findViewById(R.id.tvRemarks);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);

        if (getArguments() != null) {
            recordId = getArguments().getInt("recordId");
            loadRecordData();
        }

        btnEdit.setOnClickListener(v -> startEditForm());
        btnDelete.setOnClickListener(v -> deleteRecord());
    }

    private void loadRecordData() {
        new Thread(() -> {
            record = db.maternalDeathDao().getRecordById(recordId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::displayRecordData);
            }
        }).start();
    }

    private void displayRecordData() {
        if (record != null) {
            tvFullName.setText("Name: " + (record.fullName != null ? record.fullName : "N/A"));
            tvDate.setText("Date of Registration: " + (record.dateOfRegistration != null ? record.dateOfRegistration : "N/A"));
            tvAddress.setText("Address: " + (record.completeAddress != null ? record.completeAddress : "N/A"));
            tvAge.setText("Age: " + record.age + " years");
            tvAgeGroup.setText("Age Group: " + (record.ageGroup != null ? getAgeGroupLabel(record.ageGroup) : "N/A"));
            tvPlace.setText("Place of Occurrence: " + (record.placeOfOccurrence != null ? getPlaceLabel(record.placeOfOccurrence) : "N/A"));
            tvCause.setText("Cause of Death: " + (record.causeOfDeath != null ? getCauseLabel(record.causeOfDeath) : "N/A"));
            tvRemarks.setText("Remarks: " + (record.remarks != null ? record.remarks : "None"));
        }
    }

    private String getAgeGroupLabel(String code) {
        switch (code) {
            case "A": return "A - 10-14 years old";
            case "B": return "B - 15-19 years old";
            case "C": return "C - 20-49 years old";
            default: return code;
        }
    }

    private String getPlaceLabel(String code) {
        switch (code) {
            case "A": return "A - Resident";
            case "B": return "B - Non-Resident";
            default: return code;
        }
    }

    private String getCauseLabel(String code) {
        switch (code) {
            case "A": return "A - Direct";
            case "B": return "B - Indirect";
            default: return code;
        }
    }

    private void startEditForm() {
        MaternalDeathFormFragment editFragment = new MaternalDeathFormFragment();
        Bundle args = new Bundle();
        args.putInt("recordId", recordId);
        editFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, editFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void deleteRecord() {
        new Thread(() -> {
            db.maternalDeathDao().delete(record);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Record deleted successfully", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                });
            }
        }).start();
    }
}