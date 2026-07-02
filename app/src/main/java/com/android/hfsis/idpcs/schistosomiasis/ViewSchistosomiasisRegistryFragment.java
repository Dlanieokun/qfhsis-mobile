package com.android.hfsis.idpcs.schistosomiasis;

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
import com.android.hfsis.database.idpcs.schistosomiasis.SchistosomiasisDao;
import com.android.hfsis.model.idpcs.schistosomiasis.SchistosomiasisRegistryRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewSchistosomiasisRegistryFragment
 * Manages the list workspace view for Schistosomiasis Registry Records.
 * Performs automated search filtering and handles navigation flows to edit existing records.
 */
public class ViewSchistosomiasisRegistryFragment extends Fragment {

    private RecyclerView rvSchistosomiasisRecords;
    private TextView tvSchistosomiasisRecordCount, tvSchistosomiasisEmptyState;
    private EditText etSearchSchistosomiasisRecords;
    private FloatingActionButton fabAddSchistosomiasisRecord;

    private SchistosomiasisAdapter adapter;
    private final List<SchistosomiasisRegistryRecord> masterList = new ArrayList<>();
    private final List<SchistosomiasisRegistryRecord> filteredList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_schistosomiasis_registry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind layout elements
        rvSchistosomiasisRecords       = view.findViewById(R.id.rvSchistosomiasisRecords);
        tvSchistosomiasisRecordCount   = view.findViewById(R.id.tvSchistosomiasisRecordCount);
        tvSchistosomiasisEmptyState    = view.findViewById(R.id.tvSchistosomiasisEmptyState);
        etSearchSchistosomiasisRecords = view.findViewById(R.id.etSearchSchistosomiasisRecords);
        fabAddSchistosomiasisRecord    = view.findViewById(R.id.fabAddSchistosomiasisRecord);

        database = DatabaseHelper.getInstance(requireContext());

        setupRecyclerView();
        setupSearchFilter();

        // FAB navigates to form view tracking for a new record entry
        fabAddSchistosomiasisRecord.setOnClickListener(v -> navigateToFormFragment(0));

        // Asynchronously load records out of Room persistence
        loadSchistosomiasisRecords();
    }

    private void setupRecyclerView() {
        rvSchistosomiasisRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SchistosomiasisAdapter(filteredList);
        rvSchistosomiasisRecords.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        etSearchSchistosomiasisRecords.addTextChangedListener(new TextWatcher() {
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

    private void loadSchistosomiasisRecords() {
        new Thread(() -> {
            try {
                // Ensure abstract method schistosomiasisDao() is declared in your DatabaseHelper
                SchistosomiasisDao dao = database.schistosomiasisDao();
                List<SchistosomiasisRegistryRecord> records = dao.getAllRecords();

                requireActivity().runOnUiThread(() -> {
                    masterList.clear();
                    if (records != null) {
                        masterList.addAll(records);
                    }
                    applySearchFilter(etSearchSchistosomiasisRecords.getText().toString());
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
            for (SchistosomiasisRegistryRecord entity : masterList) {
                boolean matchesName = entity.getName() != null && entity.getName().toLowerCase(Locale.getDefault()).contains(constraint);
                boolean matchesAddress = entity.getAddress() != null && entity.getAddress().toLowerCase(Locale.getDefault()).contains(constraint);

                if (matchesName || matchesAddress) {
                    filteredList.add(entity);
                }
            }
        }

        // Synchronize view layout items count markers
        tvSchistosomiasisRecordCount.setText(String.format(Locale.US, "Total Records: %d", filteredList.size()));

        if (filteredList.isEmpty()) {
            tvSchistosomiasisEmptyState.setVisibility(View.VISIBLE);
            rvSchistosomiasisRecords.setVisibility(View.GONE);
        } else {
            tvSchistosomiasisEmptyState.setVisibility(View.GONE);
            rvSchistosomiasisRecords.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private void navigateToFormFragment(long recordId) {
        SchistosomiasisRegistryFragment formFragment = SchistosomiasisRegistryFragment.newInstance();

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

    private class SchistosomiasisAdapter extends RecyclerView.Adapter<SchistosomiasisAdapter.RecordViewHolder> {

        private final List<SchistosomiasisRegistryRecord> recordsList;

        public SchistosomiasisAdapter(List<SchistosomiasisRegistryRecord> recordsList) {
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
            SchistosomiasisRegistryRecord record = recordsList.get(position);

            holder.tvItemClientName.setText(record.getName() != null ? record.getName() : "Unknown Patient");

            // Reusing the tvItemSerial view slot to display Family Serial Number safely for Schistosomiasis context
            holder.tvItemSerial.setText(String.format("Serial No: %s", (record.getFamilySerialNumber() != null && !record.getFamilySerialNumber().isEmpty()) ? record.getFamilySerialNumber() : "N/A"));
            holder.tvItemAddress.setText(record.getAddress() != null ? record.getAddress() : "No Address Listed");

            holder.tvItemMetaDetails.setText(String.format(Locale.US, "Age: %d | Sex: %s | Reg Date: %s",
                    record.getAge(),
                    (record.getSex() != null && !record.getSex().isEmpty()) ? record.getSex() : "--",
                    (record.getDateOfRegistration() != null && !record.getDateOfRegistration().isEmpty()) ? record.getDateOfRegistration() : "--"
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