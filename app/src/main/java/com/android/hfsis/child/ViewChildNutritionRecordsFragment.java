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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.child.ChildNutritionRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ViewChildNutritionRecordsFragment extends Fragment {

    private RecyclerView rvChildRecords;
    private TextView tvRecordCount, tvEmptyState;
    private EditText etSearchChildRecords;
    private FloatingActionButton fabAddChildRecord;

    private ChildNutritionAdapter adapter;
    private final List<ChildNutritionRecord> masterList = new ArrayList<>();
    private final List<ChildNutritionRecord> filteredList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_child_immunization_records, container, false);

        database = DatabaseHelper.getInstance(requireContext().getApplicationContext());

        rvChildRecords = view.findViewById(R.id.rvChildRecords);
        tvRecordCount = view.findViewById(R.id.tvRecordCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        etSearchChildRecords = view.findViewById(R.id.etSearchChildRecords);
        fabAddChildRecord = view.findViewById(R.id.fabAddChildRecord);

        rvChildRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChildNutritionAdapter(filteredList);
        rvChildRecords.setAdapter(adapter);

        fabAddChildRecord.setOnClickListener(v -> {
            ChildNutritionFragment formFragment = new ChildNutritionFragment();
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, formFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        setupSearchWatcher();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNutritionRecords();
    }

    private void loadNutritionRecords() {
        if (database == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            List<ChildNutritionRecord> records = database.childNutritionDao().getAllRecords();

            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    masterList.clear();
                    if (records != null) {
                        masterList.addAll(records);
                    }
                    applySearchFilter(etSearchChildRecords.getText().toString());
                });
            }
        });
    }

    private void setupSearchWatcher() {
        etSearchChildRecords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchFilter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applySearchFilter(String query) {
        filteredList.clear();
        String cleanQuery = query.toLowerCase(Locale.getDefault()).trim();

        if (cleanQuery.isEmpty()) {
            filteredList.addAll(masterList);
        } else {
            for (ChildNutritionRecord record : masterList) {
                String name = record.getChildName() != null ? record.getChildName().toLowerCase(Locale.getDefault()) : "";
                String serial = record.getFamilySerialNumber() != null ? record.getFamilySerialNumber().toLowerCase(Locale.getDefault()) : "";
                String address = record.getAddress() != null ? record.getAddress().toLowerCase(Locale.getDefault()) : "";

                if (name.contains(cleanQuery) || serial.contains(cleanQuery) || address.contains(cleanQuery)) {
                    filteredList.add(record);
                }
            }
        }

        tvRecordCount.setText(String.format(Locale.getDefault(), "Total Records: %d", filteredList.size()));
        if (filteredList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvChildRecords.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvChildRecords.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    private class ChildNutritionAdapter extends RecyclerView.Adapter<ChildNutritionAdapter.RecordViewHolder> {

        private final List<ChildNutritionRecord> recordsList;

        public ChildNutritionAdapter(List<ChildNutritionRecord> recordsList) {
            this.recordsList = recordsList;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child_nutrition_record, parent, false);
            return new RecordViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            ChildNutritionRecord record = recordsList.get(position);

            holder.tvItemChildName.setText(record.getChildName() != null ? record.getChildName().toUpperCase(Locale.getDefault()) : "N/A");
            holder.tvItemAddress.setText(String.format("Address: %s", record.getAddress() != null ? record.getAddress() : "N/A"));
            holder.tvItemSerial.setText(String.format("Serial No: %s", record.getFamilySerialNumber() != null ? record.getFamilySerialNumber() : "N/A"));
            holder.tvItemRegDate.setText(String.format("Registered: %s", record.getDateRegistration() != null ? record.getDateRegistration() : "N/A"));
            holder.tvItemAgeMonths.setText(String.format("Age: %s mos.", record.getAgeMonths() != null ? record.getAgeMonths() : "0"));
            holder.tvItemSex.setText(String.format("Sex: %s", record.getSex() != null ? record.getSex() : "-"));

            String bwStatus = record.getBirthWeightStatus();
            if ("L".equalsIgnoreCase(bwStatus)) {
                holder.tvItemStatus.setText("Low Birth Wt.");
            } else if ("N".equalsIgnoreCase(bwStatus)) {
                holder.tvItemStatus.setText("Normal Wt.");
            } else {
                holder.tvItemStatus.setText("Unknown Status");
            }

            holder.btnItemEdit.setOnClickListener(v -> {
                ChildNutritionFragment formFragment = new ChildNutritionFragment();
                Bundle args = new Bundle();
                args.putLong("EDIT_RECORD_ID", record.getId());
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