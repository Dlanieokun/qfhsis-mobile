package com.android.hfsis.ncdpcs.phil;

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
import com.android.hfsis.database.ncdps.PhilPENDao;
import com.android.hfsis.model.ncdpcs.PhilPENAssessmentEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewPhilPENRiskAssessmentFragment
 * * Manages the list workspace view matching ViewChildImmunizationRecordsFragment.
 * Performs automated search filtering and handles navigation flows to edit existing records.
 */
public class ViewPhilPENRiskAssessmentFragment extends Fragment {

    private RecyclerView rvPhilPENRecords;
    private TextView tvPhilPENRecordCount, tvPhilPENEmptyState;
    private EditText etSearchPhilPENRecords;
    private FloatingActionButton fabAddPhilPENRecord;

    private PhilPENAssessmentAdapter adapter;
    private final List<PhilPENAssessmentEntity> masterList = new ArrayList<>();
    private final List<PhilPENAssessmentEntity> filteredList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_philpen_risk_assessment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind layouts elements
        rvPhilPENRecords       = view.findViewById(R.id.rvPhilPENRecords);
        tvPhilPENRecordCount   = view.findViewById(R.id.tvPhilPENRecordCount);
        tvPhilPENEmptyState    = view.findViewById(R.id.tvPhilPENEmptyState);
        etSearchPhilPENRecords = view.findViewById(R.id.etSearchPhilPENRecords);
        fabAddPhilPENRecord    = view.findViewById(R.id.fabAddPhilPENRecord);

        database = DatabaseHelper.getInstance(requireContext());

        setupRecyclerView();
        setupSearchFilter();

        // FAB navigates to form view tracking for a new record entry
        fabAddPhilPENRecord.setOnClickListener(v -> navigateToFormFragment(0));

        // Asynchronously load records out of Room persistence
        loadAssessmentRecords();
    }

    private void setupRecyclerView() {
        rvPhilPENRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PhilPENAssessmentAdapter(filteredList);
        rvPhilPENRecords.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        etSearchPhilPENRecords.addTextChangedListener(new TextWatcher() {
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

    private void loadAssessmentRecords() {
        new Thread(() -> {
            try {
                PhilPENDao dao = database.philPENDao();
                List<PhilPENAssessmentEntity> records = dao.getAllAssessments();

                requireActivity().runOnUiThread(() -> {
                    masterList.clear();
                    if (records != null) {
                        masterList.addAll(records);
                    }
                    // Reset text and fill the list view matching local dataset layout
                    applySearchFilter(etSearchPhilPENRecords.getText().toString());
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to read records: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void applySearchFilter(String query) {
        filteredList.clear();
        String constraint = query.trim().toLowerCase(Locale.getDefault());

        if (constraint.isEmpty()) {
            filteredList.addAll(masterList);
        } else {
            for (PhilPENAssessmentEntity entity : masterList) {
                boolean matchesName = entity.name != null && entity.name.toLowerCase(Locale.getDefault()).contains(constraint);
                boolean matchesSerial = entity.familySerial != null && entity.familySerial.toLowerCase(Locale.getDefault()).contains(constraint);

                if (matchesName || matchesSerial) {
                    filteredList.add(entity);
                }
            }
        }

        // Synchronize view layout items count markers
        tvPhilPENRecordCount.setText(String.format(Locale.US, "Total Records: %d", filteredList.size()));

        if (filteredList.isEmpty()) {
            tvPhilPENEmptyState.setVisibility(View.VISIBLE);
            rvPhilPENRecords.setVisibility(View.GONE);
        } else {
            tvPhilPENEmptyState.setVisibility(View.GONE);
            rvPhilPENRecords.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private void navigateToFormFragment(long recordId) {
        PhilPENRiskAssessmentFragment formFragment = PhilPENRiskAssessmentFragment.newInstance();

        // Pass targeted primary row identity down if editing an existing entry
        if (recordId > 0) {
            Bundle args = new Bundle();
            args.putLong("EDIT_RECORD_ID", recordId);
            formFragment.setArguments(args);
        }

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // ── Internal Recycler Adapter Implementation Module ──────────────────────

    private class PhilPENAssessmentAdapter extends RecyclerView.Adapter<PhilPENAssessmentAdapter.RecordViewHolder> {

        private final List<PhilPENAssessmentEntity> recordsList;

        public PhilPENAssessmentAdapter(List<PhilPENAssessmentEntity> recordsList) {
            this.recordsList = recordsList;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_philpen_record, parent, false);
            return new RecordViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            PhilPENAssessmentEntity record = recordsList.get(position);

            holder.tvItemClientName.setText(record.name != null ? record.name : "Unknown Client");
            holder.tvItemSerial.setText(String.format("Serial: %s", (record.familySerial != null && !record.familySerial.isEmpty()) ? record.familySerial : "N/A"));
            holder.tvItemAddress.setText(record.address != null ? record.address : "No Address Listed");

            holder.tvItemMetaDetails.setText(String.format(Locale.US, "Age: %s | Sex: %s | Date Assessed: %s",
                    (record.age != null && !record.age.isEmpty()) ? record.age : "--",
                    (record.sex != null && !record.sex.isEmpty()) ? record.sex : "--",
                    (record.dateAssessment != null && !record.dateAssessment.isEmpty()) ? record.dateAssessment : "--"
            ));

            // Setup programmatic route modification trigger mapping
            holder.btnItemPhilPENEdit.setOnClickListener(v -> navigateToFormFragment(record.id));
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
                tvItemClientName    = itemView.findViewById(R.id.tvItemClientName);
                tvItemSerial        = itemView.findViewById(R.id.tvItemSerial);
                tvItemAddress       = itemView.findViewById(R.id.tvItemAddress);
                tvItemMetaDetails   = itemView.findViewById(R.id.tvItemMetaDetails);
                btnItemPhilPENEdit = itemView.findViewById(R.id.btnItemPhilPENEdit);
            }
        }
    }
}