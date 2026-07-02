package com.android.hfsis.idpcs.rabies;

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
import com.android.hfsis.database.idpcs.rabies.RabiesDao;
import com.android.hfsis.model.idpcs.rabies.RabiesRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewRabiesFragment
 * Manages the list workspace view for Rabies Treatment Records.
 * Performs automated search filtering and handles navigation flows to edit existing records.
 */
public class ViewRabiesFragment extends Fragment {

    private RecyclerView rvRabiesRecords;
    private TextView tvRabiesRecordCount, tvRabiesEmptyState;
    private EditText etSearchRabiesRecords;
    private FloatingActionButton fabAddRabiesRecord;

    private RabiesAdapter adapter;
    private final List<RabiesRecord> masterList = new ArrayList<>();
    private final List<RabiesRecord> filteredList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Uses an equivalent list arrangement layout file layout structure
        return inflater.inflate(R.layout.fragment_view_rabies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind layout elements
        rvRabiesRecords       = view.findViewById(R.id.rvRabiesRecords);
        tvRabiesRecordCount   = view.findViewById(R.id.tvRabiesRecordCount);
        tvRabiesEmptyState    = view.findViewById(R.id.tvRabiesEmptyState);
        etSearchRabiesRecords = view.findViewById(R.id.etSearchRabiesRecords);
        fabAddRabiesRecord    = view.findViewById(R.id.fabAddRabiesRecord);

        database = DatabaseHelper.getInstance(requireContext());

        setupRecyclerView();
        setupSearchFilter();

        // FAB navigates to form view tracking for a new record entry
        fabAddRabiesRecord.setOnClickListener(v -> navigateToFormFragment(0));

        // Asynchronously load records out of Room persistence
        loadRabiesRecords();
    }

    private void setupRecyclerView() {
        rvRabiesRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RabiesAdapter(filteredList);
        rvRabiesRecords.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        etSearchRabiesRecords.addTextChangedListener(new TextWatcher() {
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

    private void loadRabiesRecords() {
        new Thread(() -> {
            try {
                RabiesDao dao = database.rabiesDao();
                List<RabiesRecord> records = dao.getAllRecords();

                requireActivity().runOnUiThread(() -> {
                    masterList.clear();
                    if (records != null) {
                        masterList.addAll(records);
                    }
                    // Reset text and fill the list view matching local dataset layout
                    applySearchFilter(etSearchRabiesRecords.getText().toString());
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
            for (RabiesRecord entity : masterList) {
                boolean matchesName = entity.getName() != null && entity.getName().toLowerCase(Locale.getDefault()).contains(constraint);
                boolean matchesAddress = entity.getAddress() != null && entity.getAddress().toLowerCase(Locale.getDefault()).contains(constraint);

                if (matchesName || matchesAddress) {
                    filteredList.add(entity);
                }
            }
        }

        // Synchronize view layout items count markers
        tvRabiesRecordCount.setText(String.format(Locale.US, "Total Records: %d", filteredList.size()));

        if (filteredList.isEmpty()) {
            tvRabiesEmptyState.setVisibility(View.VISIBLE);
            rvRabiesRecords.setVisibility(View.GONE);
        } else {
            tvRabiesEmptyState.setVisibility(View.GONE);
            rvRabiesRecords.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private void navigateToFormFragment(long recordId) {
        RabiesFragment formFragment = RabiesFragment.newInstance();

        // Pass targeted primary row identity down if editing an existing entry
        if (recordId > 0) {
            Bundle args = new Bundle();
            args.putLong("EDIT_RECORD_ID", recordId); // Ensure RabiesFragment checks this inside onViewCreated to populate data
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

    private class RabiesAdapter extends RecyclerView.Adapter<RabiesAdapter.RecordViewHolder> {

        private final List<RabiesRecord> recordsList;

        public RabiesAdapter(List<RabiesRecord> recordsList) {
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
            RabiesRecord record = recordsList.get(position);

            holder.tvItemClientName.setText(record.getName() != null ? record.getName() : "Unknown Patient");

            // Reusing the tvItemSerial view slot to display Contact Number safely for Rabies context
            holder.tvItemSerial.setText(String.format("Contact: %s", (record.getContactNo() != null && !record.getContactNo().isEmpty()) ? record.getContactNo() : "N/A"));
            holder.tvItemAddress.setText(record.getAddress() != null ? record.getAddress() : "No Address Listed");

            holder.tvItemMetaDetails.setText(String.format(Locale.US, "Age: %d | Sex: %s | Date of Bite: %s",
                    record.getAge(),
                    (record.getSex() != null && !record.getSex().isEmpty()) ? record.getSex() : "--",
                    (record.getDateOfBite() != null && !record.getDateOfBite().isEmpty()) ? record.getDateOfBite() : "--"
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