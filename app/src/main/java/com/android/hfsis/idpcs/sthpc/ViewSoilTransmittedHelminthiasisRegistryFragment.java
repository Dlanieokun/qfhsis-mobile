package com.android.hfsis.idpcs.sthpc;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.database.idpcs.sthpc.SoilTransmittedHelminthiasisDao;
import com.android.hfsis.model.idpcs.sthpc.SoilTransmittedHelminthiasisRegistryRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ViewSoilTransmittedHelminthiasisRegistryFragment extends Fragment {

    private RecyclerView rvSTHRecords;
    private TextView tvSTHRecordCount, tvSTHEmptyState;
    private EditText etSearchSTHRecords;
    private FloatingActionButton fabAddSTHRecord;

    private STHAdapter adapter;
    private final List<SoilTransmittedHelminthiasisRegistryRecord> masterList = new ArrayList<>();
    private final List<SoilTransmittedHelminthiasisRegistryRecord> displayList = new ArrayList<>();

    public ViewSoilTransmittedHelminthiasisRegistryFragment() {}

    public static ViewSoilTransmittedHelminthiasisRegistryFragment newInstance() {
        return new ViewSoilTransmittedHelminthiasisRegistryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_soil_transmitted_helminthiasis_registry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSTHRecords = view.findViewById(R.id.rvSTHRecords);
        tvSTHRecordCount = view.findViewById(R.id.tvSTHRecordCount);
        tvSTHEmptyState = view.findViewById(R.id.tvSTHEmptyState);
        etSearchSTHRecords = view.findViewById(R.id.etSearchSTHRecords);
        fabAddSTHRecord = view.findViewById(R.id.fabAddSTHRecord);

        rvSTHRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new STHAdapter(displayList);
        rvSTHRecords.setAdapter(adapter);

        fabAddSTHRecord.setOnClickListener(v -> navigateToFormFragment(-1));

        etSearchSTHRecords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performFilterSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDatabaseRecords();
    }

    private void loadDatabaseRecords() {
        Executors.newSingleThreadExecutor().execute(() -> {
            SoilTransmittedHelminthiasisDao dao = DatabaseHelper.getInstance(requireContext()).soilTransmittedHelminthiasisDao();
            List<SoilTransmittedHelminthiasisRegistryRecord> records = dao.getAllRecords();

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    masterList.clear();
                    masterList.addAll(records);
                    performFilterSearch(etSearchSTHRecords.getText().toString().trim());
                });
            }
        });
    }

    private void performFilterSearch(String query) {
        displayList.clear();
        if (query.isEmpty()) {
            displayList.addAll(masterList);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (SoilTransmittedHelminthiasisRegistryRecord record : masterList) {
                boolean matchesName = record.getName() != null && record.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                boolean matchesSerial = record.getFamilySerialNumber() != null && record.getFamilySerialNumber().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                if (matchesName || matchesSerial) {
                    displayList.add(record);
                }
            }
        }

        adapter.notifyDataSetChanged();
        tvSTHRecordCount.setText(String.format(Locale.US, "Total Records: %d", displayList.size()));

        if (displayList.isEmpty()) {
            tvSTHEmptyState.setVisibility(View.VISIBLE);
            rvSTHRecords.setVisibility(View.GONE);
        } else {
            tvSTHEmptyState.setVisibility(View.GONE);
            rvSTHRecords.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToFormFragment(int recordId) {
        Fragment formFragment;
        if (recordId == -1) {
            formFragment = SoilTransmittedHelminthiasisRegistryFragment.newInstance();
        } else {
            formFragment = SoilTransmittedHelminthiasisRegistryFragment.newInstance(recordId);
        }

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, formFragment) // Ensure this matches your container ID inside Activity layout
                .addToBackStack(null)
                .commit();
    }

    // RecyclerView Adapter Component Blueprint
    private class STHAdapter extends RecyclerView.Adapter<STHAdapter.RecordViewHolder> {

        private final List<SoilTransmittedHelminthiasisRegistryRecord> recordsList;

        public STHAdapter(List<SoilTransmittedHelminthiasisRegistryRecord> recordsList) {
            this.recordsList = recordsList;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Reuses the card item standard matching layout components from your project structure
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sthr_record, parent, false);
            return new RecordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            SoilTransmittedHelminthiasisRegistryRecord record = recordsList.get(position);

            holder.tvItemClientName.setText(record.getName() != null ? record.getName() : "Unknown Patient");
            holder.tvItemSerial.setText(String.format("Serial No: %s", record.getFamilySerialNumber() != null ? record.getFamilySerialNumber() : "--"));
            holder.tvItemAddress.setText(record.getAddress() != null ? record.getAddress() : "No Address Listed");

            holder.tvItemMetaDetails.setText(String.format(Locale.US, "Age: %d | Sex: %s | Reg Date: %s",
                    record.getAge(),
                    (record.getSex() != null && !record.getSex().isEmpty()) ? record.getSex() : "--",
                    (record.getDateOfRegistration() != null && !record.getDateOfRegistration().isEmpty()) ? record.getDateOfRegistration() : "--"
            ));

            holder.btnItemPhilPENEdit.setOnClickListener(v -> navigateToFormFragment(record.getId()));
        }

        @Override
        public int getItemCount() {
            return recordsList.size();
        }

        class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemClientName, tvItemSerial, tvItemAddress, tvItemMetaDetails;
            Button btnItemPhilPENEdit;

            public RecordViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemClientName   = itemView.findViewById(R.id.tvItemClientName);
                tvItemSerial       = itemView.findViewById(R.id.tvItemSerial);
                tvItemAddress      = itemView.findViewById(R.id.tvItemAddress);
                tvItemMetaDetails  = itemView.findViewById(R.id.tvItemMetaDetails);
                btnItemPhilPENEdit = itemView.findViewById(R.id.btnItemPhilPENEdit);
            }
        }
    }
}