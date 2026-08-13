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
import com.android.hfsis.model.vital_statistics.InfantDeathRecord;

public class InfantDeathViewFragment extends Fragment {

    private TextView tvFullName, tvDate, tvAddress, tvAge, tvSex, tvRemarks;
    private Button btnEdit, btnDelete;
    private DatabaseHelper db;
    private InfantDeathRecord record;
    private int recordId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_infant_death, container, false);
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
        tvSex = view.findViewById(R.id.tvSex);
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
            record = db.infantDeathDao().getRecordById(recordId);
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
            tvAge.setText("Age: " + record.age);
            tvSex.setText("Sex: " + (record.sex != null ? (record.sex.equals("M") ? "Male" : "Female") : "N/A"));
            tvRemarks.setText("Remarks: " + (record.remarks != null ? record.remarks : "None"));
        }
    }

    private void startEditForm() {
        InfantDeathFormFragment editFragment = new InfantDeathFormFragment();
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
            db.infantDeathDao().delete(record);
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