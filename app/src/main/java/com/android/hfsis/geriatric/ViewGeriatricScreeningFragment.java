package com.android.hfsis.geriatric;

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
import com.android.hfsis.model.geriatric.GeriatricScreeningRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewGeriatricScreeningFragment
 * Manages the list workspace view for Target Client List for Geriatric Screening.
 * Performs automated search filtering and handles navigation flows to edit existing records.
 */
public class ViewGeriatricScreeningFragment extends Fragment {

    private RecyclerView rvGeriatricRecords;
    private TextView tvGeriatricRecordCount, tvGeriatricEmptyState;
    private EditText etSearchGeriatricRecords;
    private FloatingActionButton fabAddGeriatricRecord;

    private GeriatricScreeningAdapter adapter;
    private final List<GeriatricScreeningRecord> masterList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_geriatric_screening, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = DatabaseHelper.getInstance(requireContext());

        // Bind Views
        rvGeriatricRecords = view.findViewById(R.id.rvGeriatricRecords);
        tvGeriatricRecordCount = view.findViewById(R.id.tvGeriatricRecordCount);
        tvGeriatricEmptyState = view.findViewById(R.id.tvGeriatricEmptyState);
        etSearchGeriatricRecords = view.findViewById(R.id.etSearchGeriatricRecords);
        fabAddGeriatricRecord = view.findViewById(R.id.fabAddGeriatricRecord);

        // Setup Recycler View
        rvGeriatricRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GeriatricScreeningAdapter(new ArrayList<>());
        rvGeriatricRecords.setAdapter(adapter);

        // Action Listeners
        fabAddGeriatricRecord.setOnClickListener(v -> navigateToFormFragment(-1));

        etSearchGeriatricRecords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearchFilter(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadScreeningRecords();
    }

    private void loadScreeningRecords() {
        new Thread(() -> {
            List<GeriatricScreeningRecord> records = database.geriatricScreeningDao().getAllRecords();
            requireActivity().runOnUiThread(() -> {
                masterList.clear();
                masterList.addAll(records);
                updateListDisplay(masterList);
            });
        }).start();
    }

    private void performSearchFilter(String query) {
        if (query.isEmpty()) {
            updateListDisplay(masterList);
            return;
        }

        new Thread(() -> {
            List<GeriatricScreeningRecord> filtered = database.geriatricScreeningDao().searchRecords("%" + query + "%");
            requireActivity().runOnUiThread(() -> updateListDisplay(filtered));
        }).start();
    }

    private void updateListDisplay(List<GeriatricScreeningRecord> list) {
        adapter.updateList(list);
        tvGeriatricRecordCount.setText(String.format(Locale.US, "Total Records: %d", list.size()));

        if (list.isEmpty()) {
            rvGeriatricRecords.setVisibility(View.GONE);
            tvGeriatricEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvGeriatricRecords.setVisibility(View.VISIBLE);
            tvGeriatricEmptyState.setVisibility(View.GONE);
        }
    }

    private void navigateToFormFragment(long recordId) {
        Fragment formFragment = new GeriatricScreeningFragment();
        if (recordId != -1) {
            Bundle args = new Bundle();
            args.putLong("record_id", recordId);
            formFragment.setArguments(args);
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, formFragment) // Ensure this fits your parent activity's container ID
                .addToBackStack(null)
                .commit();
    }

    // ── Internal Adapter Class ──────────────────────────────────────────────
    private class GeriatricScreeningAdapter extends RecyclerView.Adapter<GeriatricScreeningAdapter.RecordViewHolder> {

        private final List<GeriatricScreeningRecord> recordsList;

        public GeriatricScreeningAdapter(List<GeriatricScreeningRecord> recordsList) {
            this.recordsList = recordsList;
        }

        public void updateList(List<GeriatricScreeningRecord> newList) {
            this.recordsList.clear();
            this.recordsList.addAll(newList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Reuses R.layout.item_philpen_record structural item standard
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_philpen_record, parent, false);
            return new RecordViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            GeriatricScreeningRecord record = recordsList.get(position);

            holder.tvItemClientName.setText(record.getName() != null ? record.getName() : "Unknown Client");
            holder.tvItemSerial.setText(String.format("Serial: %s",
                    (record.getFamilySerialNumber() != null && !record.getFamilySerialNumber().isEmpty()) ? record.getFamilySerialNumber() : "--"));
            holder.tvItemAddress.setText(record.getAddress() != null ? record.getAddress() : "--");

            // Format results representation cleanly for space boundaries
            String parsedResults = (record.getResults() != null && !record.getResults().isEmpty()) ? record.getResults() : "0 (Negative)";

            holder.tvItemMetaDetails.setText(String.format(Locale.US, "Age: %d | Sex: %s | Domains: %s | Screened: %s",
                    record.getAge(),
                    (record.getSex() != null && !record.getSex().isEmpty()) ? record.getSex() : "--",
                    parsedResults,
                    (record.getDateOfScreening() != null && !record.getDateOfScreening().isEmpty()) ? record.getDateOfScreening() : "--"
            ));

            // Map programmatic route edit modification trigger mapping using the existing item button layout
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