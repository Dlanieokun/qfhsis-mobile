package com.android.hfsis.ncdpcs.mental;

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
import com.android.hfsis.database.ncdps.MentalHealthDao;
import com.android.hfsis.model.ncdpcs.mental.MentalHealthRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewMentalHealthFragment
 * Manages the list workspace view for Target Client List for Mental Health.
 * Performs automated search filtering and handles navigation flows to edit existing records.
 */
public class ViewMentalHealthFragment extends Fragment {

    private RecyclerView rvMentalRecords;
    private TextView tvMentalRecordCount, tvMentalEmptyState;
    private EditText etSearchMentalRecords;
    private FloatingActionButton fabAddMentalRecord;

    private MentalHealthAdapter adapter;
    private final List<MentalHealthRecord> masterList = new ArrayList<>();
    private final List<MentalHealthRecord> filteredList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_mental_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind layout elements
        rvMentalRecords       = view.findViewById(R.id.rvMentalRecords);
        tvMentalRecordCount   = view.findViewById(R.id.tvMentalRecordCount);
        tvMentalEmptyState    = view.findViewById(R.id.tvMentalEmptyState);
        etSearchMentalRecords = view.findViewById(R.id.etSearchMentalRecords);
        fabAddMentalRecord    = view.findViewById(R.id.fabAddMentalRecord);

        database = DatabaseHelper.getInstance(requireContext());

        setupRecyclerView();
        setupSearchFilter();

        // FAB navigates to form view tracking for a new record entry
        fabAddMentalRecord.setOnClickListener(v -> navigateToFormFragment(-1));

        // Asynchronously load records out of Room persistence
        loadMentalHealthRecords();
    }

    private void setupRecyclerView() {
        rvMentalRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MentalHealthAdapter(filteredList);
        rvMentalRecords.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        etSearchMentalRecords.addTextChangedListener(new TextWatcher() {
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

    private void loadMentalHealthRecords() {
        new Thread(() -> {
            try {
                MentalHealthDao dao = database.mentalHealthDao();
                List<MentalHealthRecord> records = dao.getAllRecords();

                requireActivity().runOnUiThread(() -> {
                    masterList.clear();
                    if (records != null) {
                        masterList.addAll(records);
                    }
                    applySearchFilter(etSearchMentalRecords.getText().toString());
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
            for (MentalHealthRecord entity : masterList) {
                boolean matchesName = entity.getName() != null && entity.getName().toLowerCase(Locale.getDefault()).contains(constraint);
                boolean matchesSerial = entity.getFamilySerialNumber() != null && entity.getFamilySerialNumber().toLowerCase(Locale.getDefault()).contains(constraint);

                if (matchesName || matchesSerial) {
                    filteredList.add(entity);
                }
            }
        }

        tvMentalRecordCount.setText(String.format(Locale.US, "Total Records: %d", filteredList.size()));

        if (filteredList.isEmpty()) {
            tvMentalEmptyState.setVisibility(View.VISIBLE);
            rvMentalRecords.setVisibility(View.GONE);
        } else {
            tvMentalEmptyState.setVisibility(View.GONE);
            rvMentalRecords.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private void navigateToFormFragment(long recordId) {
        MentalHealthFragment formFragment;

        // FIXED: Use the correct factory method and sync argument key identifiers
        if (recordId != -1) {
            formFragment = MentalHealthFragment.newInstance(recordId);
        } else {
            formFragment = MentalHealthFragment.newInstance();
        }

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // ── Internal Recycler Adapter Implementation Module ──────────────────────

    private class MentalHealthAdapter extends RecyclerView.Adapter<MentalHealthAdapter.RecordViewHolder> {

        private final List<MentalHealthRecord> recordsList;

        public MentalHealthAdapter(List<MentalHealthRecord> recordsList) {
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
            MentalHealthRecord record = recordsList.get(position);

            holder.tvItemClientName.setText(record.getName() != null ? record.getName() : "Unknown Client");
            holder.tvItemSerial.setText(String.format("Serial: %s", (record.getFamilySerialNumber() != null && !record.getFamilySerialNumber().isEmpty()) ? record.getFamilySerialNumber() : "N/A"));
            holder.tvItemAddress.setText(record.getAddress() != null ? record.getAddress() : "No Address Listed");

            holder.tvItemMetaDetails.setText(String.format(Locale.US, "Age: %d (Group %s) | Sex: %s | Assessed: %s",
                    record.getAge(),
                    (record.getAgeGroup() != null ? record.getAgeGroup() : "--"),
                    (record.getSex() != null && !record.getSex().isEmpty()) ? record.getSex() : "--",
                    (record.getDateOfAssessment() != null && !record.getDateOfAssessment().isEmpty()) ? record.getDateOfAssessment() : "--"
            ));

            holder.btnItemPhilPENEdit.setOnClickListener(v -> navigateToFormFragment(record.getRecordNo()));
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