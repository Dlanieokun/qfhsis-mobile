package com.android.hfsis.child;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.child.ChildImmunizationRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViewChildImmunizationRecordsFragment extends Fragment {

    private RecyclerView rvChildRecords;
    private TextView tvRecordCount, tvEmptyState;
    private EditText etSearchChildRecords;
    private FloatingActionButton fabAddChildRecord;

    private ChildImmunizationAdapter adapter;
    private final List<ChildImmunizationRecord> masterList = new ArrayList<>();
    private final List<ChildImmunizationRecord> filteredList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_child_immunization_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = DatabaseHelper.getInstance(requireContext().getApplicationContext());

        rvChildRecords = view.findViewById(R.id.rvChildRecords);
        tvRecordCount = view.findViewById(R.id.tvRecordCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        etSearchChildRecords = view.findViewById(R.id.etSearchChildRecords);
        fabAddChildRecord = view.findViewById(R.id.fabAddChildRecord);

        setupRecyclerView();
        setupSearchFilter();
        observeImmunizationRecords();

        fabAddChildRecord.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ChildImmunizationFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ChildImmunizationAdapter(filteredList);
        rvChildRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChildRecords.setAdapter(adapter);
    }

    private void observeImmunizationRecords() {
        if (database == null) return;

        database.childImmunizationDao().getAllRecords().observe(getViewLifecycleOwner(), records -> {
            if (records != null) {
                masterList.clear();
                masterList.addAll(records);
                applyFilter(etSearchChildRecords.getText().toString());
            }
        });
    }

    private void setupSearchFilter() {
        etSearchChildRecords.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                applyFilter(s.toString());
            }
        });
    }

    private void applyFilter(String query) {
        filteredList.clear();
        String cleanQuery = query.trim().toLowerCase(Locale.getDefault());

        if (cleanQuery.isEmpty()) {
            filteredList.addAll(masterList);
        } else {
            for (ChildImmunizationRecord record : masterList) {
                String name = record.getChildName() != null ? record.getChildName().toLowerCase(Locale.getDefault()) : "";
                String serial = record.getFamilySerialNumber() != null ? record.getFamilySerialNumber().toLowerCase(Locale.getDefault()) : "";
                String address = record.getAddress() != null ? record.getAddress().toLowerCase(Locale.getDefault()) : "";

                if (name.contains(cleanQuery) || serial.contains(cleanQuery) || address.contains(cleanQuery)) {
                    filteredList.add(record);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateUiState();
    }

    private void updateUiState() {
        int count = filteredList.size();
        tvRecordCount.setText(String.format(Locale.getDefault(), "Total Records Found: %d", count));

        if (count == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvChildRecords.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvChildRecords.setVisibility(View.VISIBLE);
        }
    }

    private class ChildImmunizationAdapter extends RecyclerView.Adapter<ChildImmunizationAdapter.RecordViewHolder> {

        private final List<ChildImmunizationRecord> recordsList;

        public ChildImmunizationAdapter(List<ChildImmunizationRecord> recordsList) {
            this.recordsList = recordsList;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child_immunization_record, parent, false);
            return new RecordViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            ChildImmunizationRecord record = recordsList.get(position);

            holder.tvItemChildName.setText(record.getChildName() != null && !record.getChildName().isEmpty() ? record.getChildName() : "Unnamed Child");
            holder.tvItemAddress.setText(String.format("Address: %s", record.getAddress() != null && !record.getAddress().isEmpty() ? record.getAddress() : "N/A"));
            holder.tvItemSerial.setText(String.format("Serial No: %s", record.getFamilySerialNumber() != null && !record.getFamilySerialNumber().isEmpty() ? record.getFamilySerialNumber() : "N/A"));
            holder.tvItemRegDate.setText(String.format("Registered: %s", record.getRegistrationDate() != null && !record.getRegistrationDate().isEmpty() ? record.getRegistrationDate() : "N/A"));
            holder.tvItemAgeMonths.setText(String.format("Age: %s Months", record.getAgeMonths() != null && !record.getAgeMonths().isEmpty() ? record.getAgeMonths() : "0"));
            holder.tvItemSex.setText(String.format("Sex: %s", record.getSex() != null && !record.getSex().isEmpty() ? record.getSex() : "N/A"));

            boolean isFic = record.isFicBcg() && record.isFicDpt3() && record.isFicOpv3() && record.isFicMmr2();
            holder.tvItemStatus.setText(isFic ? "Fully Immunized Child (FIC)" : "Incomplete / Under Schedule");
            holder.tvItemStatus.setTextColor(isFic ? 0xFF2E7D32 : 0xFFC62828);

            // Function triggered when EDIT is pressed
            holder.btnItemEdit.setOnClickListener(v -> {
                ChildImmunizationFragment formFragment = new ChildImmunizationFragment();
                Bundle args = new Bundle();
                args.putLong("EDIT_RECORD_ID", record.getId()); // Pass row primary key ID
                formFragment.setArguments(args);

                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, formFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return recordsList.size();
        }

        class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemChildName, tvItemAddress, tvItemSerial, tvItemRegDate, tvItemAgeMonths, tvItemSex, tvItemStatus;
            Button btnItemEdit;

            public RecordViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemChildName = itemView.findViewById(R.id.tvItemChildName);
                tvItemAddress = itemView.findViewById(R.id.tvItemAddress);
                tvItemSerial = itemView.findViewById(R.id.tvItemSerial);
                tvItemRegDate = itemView.findViewById(R.id.tvItemRegDate);
                tvItemAgeMonths = itemView.findViewById(R.id.tvItemAgeMonths);
                tvItemSex = itemView.findViewById(R.id.tvItemSex);
                tvItemStatus = itemView.findViewById(R.id.tvItemStatus);
                btnItemEdit = itemView.findViewById(R.id.btnItemEdit);
            }
        }
    }
}