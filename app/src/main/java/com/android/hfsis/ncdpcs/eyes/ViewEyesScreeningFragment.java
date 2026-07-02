package com.android.hfsis.ncdpcs.eyes;

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

import com.android.hfsis.database.ncdps.EyesScreeningDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.ncdpcs.EyesScreeningsData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewEyesScreeningFragment
 * Manages the list workspace view matching ViewPhilPENRiskAssessmentFragment.
 * Performs automated search filtering and handles navigation flows to edit existing records.
 */
public class ViewEyesScreeningFragment extends Fragment {

    private RecyclerView rvEyesRecords;
    private TextView tvEyesRecordCount, tvEyesEmptyState;
    private EditText etSearchEyesRecords;
    private FloatingActionButton fabAddEyesRecord;

    private EyesScreeningAdapter adapter;
    private final List<EyesScreeningsData> masterList = new ArrayList<>();
    private final List<EyesScreeningsData> filteredList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_eyes_screening, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind layout elements
        rvEyesRecords       = view.findViewById(R.id.rvEyesRecords);
        tvEyesRecordCount   = view.findViewById(R.id.tvEyesRecordCount);
        tvEyesEmptyState    = view.findViewById(R.id.tvEyesEmptyState);
        etSearchEyesRecords = view.findViewById(R.id.etSearchEyesRecords);
        fabAddEyesRecord    = view.findViewById(R.id.fabAddEyesRecord);

        database = DatabaseHelper.getInstance(requireContext());

        setupRecyclerView();
        setupSearchFilter();

        // FAB navigates to form view tracking for a new record entry
        fabAddEyesRecord.setOnClickListener(v -> navigateToFormFragment(0));

        // Asynchronously load records out of Room persistence
        loadScreeningRecords();
    }

    private void setupRecyclerView() {
        rvEyesRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EyesScreeningAdapter(filteredList);
        rvEyesRecords.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        etSearchEyesRecords.addTextChangedListener(new TextWatcher() {
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
                EyesScreeningDao dao = database.eyesScreeningDao();
                List<EyesScreeningsData> records = dao.getAllScreenings();

                requireActivity().runOnUiThread(() -> {
                    masterList.clear();
                    if (records != null) {
                        masterList.addAll(records);
                    }
                    // Reset text and fill the list view matching local dataset layout
                    applySearchFilter(etSearchEyesRecords.getText().toString());
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
            for (EyesScreeningsData entity : masterList) {
                boolean matchesName = entity.getName() != null && entity.getName().toLowerCase(Locale.getDefault()).contains(constraint);
                boolean matchesSerial = entity.getFamilySerial() != null && entity.getFamilySerial().toLowerCase(Locale.getDefault()).contains(constraint);

                if (matchesName || matchesSerial) {
                    filteredList.add(entity);
                }
            }
        }

        // Synchronize view layout items count markers
        tvEyesRecordCount.setText(String.format(Locale.US, "Total Records: %d", filteredList.size()));

        if (filteredList.isEmpty()) {
            tvEyesEmptyState.setVisibility(View.VISIBLE);
            rvEyesRecords.setVisibility(View.GONE);
        } else {
            tvEyesEmptyState.setVisibility(View.GONE);
            rvEyesRecords.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private void navigateToFormFragment(long recordId) {
        EyesScreeningFragment formFragment = EyesScreeningFragment.newInstance();

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

    private class EyesScreeningAdapter extends RecyclerView.Adapter<EyesScreeningAdapter.RecordViewHolder> {

        private final List<EyesScreeningsData> recordsList;

        public EyesScreeningAdapter(List<EyesScreeningsData> recordsList) {
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
            EyesScreeningsData record = recordsList.get(position);

            holder.tvItemClientName.setText(record.getName() != null ? record.getName() : "Unknown Client");
            holder.tvItemSerial.setText(String.format("Serial: %s", (record.getFamilySerial() != null && !record.getFamilySerial().isEmpty()) ? record.getFamilySerial() : "N/A"));
            holder.tvItemAddress.setText(record.getAddress() != null ? record.getAddress() : "No Address Listed");

            holder.tvItemMetaDetails.setText(String.format(Locale.US, "Age: %s | Sex: %s | Date Screened: %s",
                    (record.getAge() != null && !record.getAge().isEmpty()) ? record.getAge() : "--",
                    (record.getSex() != null && !record.getSex().isEmpty()) ? record.getSex() : "--",
                    (record.getDateScreening() != null && !record.getDateScreening().isEmpty()) ? record.getDateScreening() : "--"
            ));

            // Setup programmatic route modification trigger mapping (reusing the btnItemPhilPENEdit view element safely)
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