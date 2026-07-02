package com.android.hfsis.vital_satatistics.environmental;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.environmental.EnvironmentalHealthModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * ViewMasterlistEnvironmentalHealthFragment
 * Displays a searchable dashboard list of all Environmental Health and Sanitation entries.
 */
public class ViewMasterlistEnvironmentalHealthFragment extends Fragment {

    private RecyclerView rvEnvironmentalRecords;
    private TextView tvRecordCount, tvEmptyState;
    private EditText etSearchEnvironmentalRecords;
    private FloatingActionButton fabAddEnvironmentalRecord;

    private EnvironmentalRecordsAdapter adapter;
    private List<EnvironmentalHealthModel> masterList = new ArrayList<>();
    private List<EnvironmentalHealthModel> filteredList = new ArrayList<>();
    private DatabaseHelper db;

    public ViewMasterlistEnvironmentalHealthFragment() {
        // Required empty public constructor
    }

    public static ViewMasterlistEnvironmentalHealthFragment newInstance() {
        return new ViewMasterlistEnvironmentalHealthFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // FIXED: Pointing to the correct resource layout filename matching your codebase
        return inflater.inflate(R.layout.fragment_view_masterlist_environmental_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());

        // Initialize UI Elements
        rvEnvironmentalRecords = view.findViewById(R.id.rvOralRecords);
        tvRecordCount = view.findViewById(R.id.tvRecordCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        etSearchEnvironmentalRecords = view.findViewById(R.id.etSearchOralRecords);
        fabAddEnvironmentalRecord = view.findViewById(R.id.fabAddOralRecord);

        // Safely alter hints on the bound search item
        if (etSearchEnvironmentalRecords != null) {
            etSearchEnvironmentalRecords.setHint("Search by Household Head Name...");
            etSearchEnvironmentalRecords.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterRecords(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Setup RecyclerView
        rvEnvironmentalRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EnvironmentalRecordsAdapter(filteredList);
        rvEnvironmentalRecords.setAdapter(adapter);

        // Add Record Click Target Router
        if (fabAddEnvironmentalRecord != null) {
            fabAddEnvironmentalRecord.setOnClickListener(v -> {
                MasterlistEnvironmentalHealthFragment targetFragment = MasterlistEnvironmentalHealthFragment.newInstance();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, targetFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        loadDatabaseRecords();
    }

    private void loadDatabaseRecords() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<EnvironmentalHealthModel> records = db.environmentalHealthDao().getAllRecords();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    masterList.clear();
                    if (records != null) {
                        masterList.addAll(records);
                    }
                    if (etSearchEnvironmentalRecords != null) {
                        filterRecords(etSearchEnvironmentalRecords.getText().toString());
                    } else {
                        filterRecords("");
                    }
                });
            }
        });
    }

    private void filterRecords(String query) {
        filteredList.clear();
        String cleanQuery = query.toLowerCase(Locale.getDefault()).trim();

        if (cleanQuery.isEmpty()) {
            filteredList.addAll(masterList);
        } else {
            for (EnvironmentalHealthModel item : masterList) {
                if (item.getHouseholdHeadName() != null &&
                        item.getHouseholdHeadName().toLowerCase(Locale.getDefault()).contains(cleanQuery)) {
                    filteredList.add(item);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateUiState();
    }

    private void updateUiState() {
        int count = filteredList.size();
        if (tvRecordCount != null) {
            tvRecordCount.setText(String.format(Locale.getDefault(), "Total Records: %d", count));
        }

        if (count == 0) {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
            if (rvEnvironmentalRecords != null) rvEnvironmentalRecords.setVisibility(View.GONE);
        } else {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
            if (rvEnvironmentalRecords != null) rvEnvironmentalRecords.setVisibility(View.VISIBLE);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RecyclerView Adapter Subclass Implementation
    // ─────────────────────────────────────────────────────────────
    private class EnvironmentalRecordsAdapter extends RecyclerView.Adapter<EnvironmentalRecordsAdapter.RecordViewHolder> {

        private final List<EnvironmentalHealthModel> recordsList;

        public EnvironmentalRecordsAdapter(List<EnvironmentalHealthModel> recordsList) {
            this.recordsList = recordsList;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_environmental_health_record, parent, false);
            return new RecordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            EnvironmentalHealthModel record = recordsList.get(position);

            // Bind values text parameters safely
            holder.tvItemHeadName.setText(record.getHouseholdHeadName() != null ? record.getHouseholdHeadName() : "N/A");
            holder.tvItemRemarks.setText(record.getRemarks() != null && !record.getRemarks().isEmpty() ? "Remarks: " + record.getRemarks() : "Remarks: None");

            // Evaluate Safe Water Supply configuration strings
            StringBuilder waterType = new StringBuilder("Water: ");
            if (record.isWaterLevelI()) waterType.append("Level I");
            else if (record.isWaterLevelII()) waterType.append("Level II");
            else if (record.isWaterLevelIII()) waterType.append("Level III");
            else if (record.getWaterSourceOthers() != null && !record.getWaterSourceOthers().isEmpty()) waterType.append(record.getWaterSourceOthers());
            else waterType.append("Not Specified");
            holder.tvItemWaterType.setText(waterType.toString());

            // Evaluate Sanitation status configuration strings
            String statusLabel = record.getSanitationStatus() != null && !record.getSanitationStatus().isEmpty()
                    ? record.getSanitationStatus() : "Unassigned";
            holder.tvItemSanitationStatus.setText("Sanitation: " + statusLabel);

            // Compute compliance flags to render high visibility badge metrics
            if (record.getSafelyManagedDrinkingWater() == 1 && record.getSafelyManagedSanitationService() == 1) {
                holder.tvItemStatusBadge.setText("FULLY COMPLIANT");
                holder.tvItemStatusBadge.setTextColor(Color.parseColor("#2E7D32")); // Green
            } else {
                holder.tvItemStatusBadge.setText("PENDING / INCOMPLETE");
                holder.tvItemStatusBadge.setTextColor(Color.parseColor("#C62828")); // Red
            }

            // Edit Action Navigation payload mapping
            holder.btnItemEdit.setOnClickListener(v -> {
                MasterlistEnvironmentalHealthFragment targetFragment = MasterlistEnvironmentalHealthFragment.newInstance();

                // Set the existing ID record so MasterlistEnvironmentalHealthFragment triggers an UPDATE cycle instead of an INSERT cycle.
                targetFragment.setExistingRecordId(record.getId());

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, targetFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        @Override
        public int getItemCount() {
            return recordsList.size();
        }

        class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemHeadName, tvItemWaterType, tvItemSanitationStatus, tvItemRemarks, tvItemStatusBadge;
            ImageButton btnItemEdit;

            public RecordViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemHeadName         = itemView.findViewById(R.id.tvItemHeadName);
                tvItemWaterType        = itemView.findViewById(R.id.tvItemWaterType);
                tvItemSanitationStatus = itemView.findViewById(R.id.tvItemSanitationStatus);
                tvItemRemarks          = itemView.findViewById(R.id.tvItemRemarks);
                tvItemStatusBadge      = itemView.findViewById(R.id.tvItemStatusBadge);
                btnItemEdit            = itemView.findViewById(R.id.btnItemEdit);
            }
        }
    }
}