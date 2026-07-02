package com.android.hfsis.ncdpcs.cancer;

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
import com.android.hfsis.database.ncdps.CervicalCancerScreeningDao;
import com.android.hfsis.model.ncdpcs.CervicalCancerScreeningEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewCervicalCancerScreeningFragment
 * Manages the list workspace view for Cervical Cancer records.
 * Performs automated search filtering and handles navigation flows to edit existing records.
 */
public class ViewCervicalCancerScreeningFragment extends Fragment {

    private RecyclerView rvCervicalRecords;
    private TextView tvCervicalRecordCount, tvCervicalEmptyState;
    private EditText etSearchCervicalRecords;
    private FloatingActionButton fabAddCervicalRecord;

    private CervicalCancerScreeningAdapter adapter;
    private final List<CervicalCancerScreeningEntity> masterList = new ArrayList<>();
    private final List<CervicalCancerScreeningEntity> filteredList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_cervical_cancer_screening, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind layout elements
        rvCervicalRecords       = view.findViewById(R.id.rvCervicalRecords);
        tvCervicalRecordCount   = view.findViewById(R.id.tvCervicalRecordCount);
        tvCervicalEmptyState    = view.findViewById(R.id.tvCervicalEmptyState);
        etSearchCervicalRecords = view.findViewById(R.id.etSearchCervicalRecords);
        fabAddCervicalRecord    = view.findViewById(R.id.fabAddCervicalRecord);

        database = DatabaseHelper.getInstance(requireContext());

        setupRecyclerView();
        setupSearchFilter();

        // FAB navigates to form view tracking for a new record entry
        fabAddCervicalRecord.setOnClickListener(v -> navigateToFormFragment(0));

        // Asynchronously load records out of Room persistence
        loadScreeningRecords();
    }

    private void setupRecyclerView() {
        rvCervicalRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CervicalCancerScreeningAdapter(filteredList);
        rvCervicalRecords.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        etSearchCervicalRecords.addTextChangedListener(new TextWatcher() {
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

    private void loadScreeningRecords() {
        new Thread(() -> {
            try {
                CervicalCancerScreeningDao dao = database.cervicalCancerScreeningDao();
                List<CervicalCancerScreeningEntity> records = dao.getAllScreenings();

                requireActivity().runOnUiThread(() -> {
                    masterList.clear();
                    if (records != null) {
                        masterList.addAll(records);
                    }
                    // Reset text and fill the list view matching local dataset layout
                    applySearchFilter(etSearchCervicalRecords.getText().toString());
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
            for (CervicalCancerScreeningEntity entity : masterList) {
                boolean matchesName = entity.getName() != null && entity.getName().toLowerCase(Locale.getDefault()).contains(constraint);
                boolean matchesSerial = entity.getFamilySerial() != null && entity.getFamilySerial().toLowerCase(Locale.getDefault()).contains(constraint);

                if (matchesName || matchesSerial) {
                    filteredList.add(entity);
                }
            }
        }

        // Synchronize view layout items count markers
        tvCervicalRecordCount.setText(String.format(Locale.US, "Total Records: %d", filteredList.size()));

        if (filteredList.isEmpty()) {
            tvCervicalEmptyState.setVisibility(View.VISIBLE);
            rvCervicalRecords.setVisibility(View.GONE);
        } else {
            tvCervicalEmptyState.setVisibility(View.GONE);
            rvCervicalRecords.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private void navigateToFormFragment(long recordId) {
        CervicalCancerScreeningFragment formFragment = CervicalCancerScreeningFragment.newInstance();

        // Pass targeted primary row identity down if editing an existing entry
        if (recordId > 0) {
            formFragment.setCurrentScreeningId(recordId);
        }

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // ── Internal Recycler Adapter Implementation Module ──────────────────────

    private class CervicalCancerScreeningAdapter extends RecyclerView.Adapter<CervicalCancerScreeningAdapter.RecordViewHolder> {

        private final List<CervicalCancerScreeningEntity> recordsList;

        public CervicalCancerScreeningAdapter(List<CervicalCancerScreeningEntity> recordsList) {
            this.recordsList = recordsList;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Reuses the identical standard list item format from your module architecture
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_philpen_record, parent, false);
            return new RecordViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            CervicalCancerScreeningEntity record = recordsList.get(position);

            holder.tvItemClientName.setText(record.getName() != null ? record.getName() : "Unknown Client");
            holder.tvItemSerial.setText(String.format("Serial: %s", (record.getFamilySerial() != null && !record.getFamilySerial().isEmpty()) ? record.getFamilySerial() : "N/A"));
            holder.tvItemAddress.setText(record.getAddress() != null ? record.getAddress() : "No Address Listed");

            holder.tvItemMetaDetails.setText(String.format(Locale.US, "Age: %s | Date Screened: %s",
                    (record.getAge() != null && !record.getAge().isEmpty()) ? record.getAge() : "--",
                    (record.getDateAssessment() != null && !record.getDateAssessment().isEmpty()) ? record.getDateAssessment() : "--"
            ));

            // Setup programmatic route modification trigger mapping
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