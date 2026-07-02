package com.android.hfsis.child;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.child.ChildSickRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ViewChildManagementOfSickFragment extends Fragment {

    private TextInputEditText etSearch;
    private RecyclerView rvSickRecords;
    private LinearLayout layoutEmptyState;
    private FloatingActionButton fabAddRecord;

    private RecordsAdapter adapter;
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_child_management_of_sick, container, false);

        etSearch = view.findViewById(R.id.etSearch);
        rvSickRecords = view.findViewById(R.id.rvSickRecords);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        fabAddRecord = view.findViewById(R.id.fabAddRecord);

        database = DatabaseHelper.getInstance(getContext());

        // Initialize RecyclerView Architecture
        rvSickRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecordsAdapter(new ArrayList<>(), record -> {
            // Open target record in edit/update context
            Bundle args = new Bundle();
            args.putLong("RECORD_ID", record.getId());
            ChildManagementOfSickFragment editFragment = new ChildManagementOfSickFragment();
            editFragment.setArguments(args);

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        rvSickRecords.setAdapter(adapter);

        // FAB navigates to clear form template
        fabAddRecord.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ChildManagementOfSickFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Track live user input loops for automatic updates
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchRecordsFromDb(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Automatically sync content layout state variations upon view returns
        String currentQuery = etSearch.getText() != null ? etSearch.getText().toString() : "";
        fetchRecordsFromDb(currentQuery);
    }

    private void fetchRecordsFromDb(String query) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ChildSickRecord> records;
            String cleanQuery = query.trim();

            if (cleanQuery.isEmpty()) {
                records = database.childSickDao().getAllRecords();
            } else {
                // Pass structured syntax matching standard pattern tokens to SQLite
                records = database.childSickDao().searchRecords("%" + cleanQuery + "%");
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> updateUiState(records));
            }
        });
    }

    private void updateUiState(List<ChildSickRecord> records) {
        if (records == null || records.isEmpty()) {
            rvSickRecords.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvSickRecords.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            adapter.updateDataset(records);
        }
    }

    // ── Adapter Component Architecture ───────────────────────────────────────────
    private static class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordViewHolder> {

        private final List<ChildSickRecord> dataset;
        private final OnRecordClickListener clickListener;

        public interface OnRecordClickListener {
            void onRecordClick(ChildSickRecord record);
        }

        public RecordsAdapter(List<ChildSickRecord> dataset, OnRecordClickListener clickListener) {
            this.dataset = dataset;
            this.clickListener = clickListener;
        }

        public void updateDataset(List<ChildSickRecord> newList) {
            this.dataset.clear();
            this.dataset.addAll(newList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child_sick_record, parent, false);
            return new RecordViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            ChildSickRecord item = dataset.get(position);
            holder.tvChildName.setText(item.getChildName());
            holder.tvSerial.setText("Serial No: " + item.getFamilySerialNumber());
            holder.tvSex.setText(item.getSex());

            // Compile descriptive tags dynamically across modern or older SDK targets
            List<String> illnesses = new ArrayList<>();
            if (item.isDiagnosisMeasles()) illnesses.add("Measles");
            if (item.isDiagnosisPersistentDiarrhea()) illnesses.add("Diarrhea");

            if (illnesses.isEmpty()) {
                holder.tvDiagnosis.setText("Diagnosis: None Tracked");
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    holder.tvDiagnosis.setText("Diagnosis: " + String.join(", ", illnesses));
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < illnesses.size(); i++) {
                        sb.append(illnesses.get(i));
                        if (i < illnesses.size() - 1) sb.append(", ");
                    }
                    holder.tvDiagnosis.setText("Diagnosis: " + sb.toString());
                }
            }

            // Map listeners evenly across both full item bodies or the edit sub-icon explicitly
            View.OnClickListener handleEditAction = v -> clickListener.onRecordClick(item);
            holder.itemView.setOnClickListener(handleEditAction);
            holder.btnEditRecord.setOnClickListener(handleEditAction);
        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }

        static class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView tvChildName, tvSerial, tvSex, tvDiagnosis;
            ImageButton btnEditRecord;

            public RecordViewHolder(@NonNull View itemView) {
                super(itemView);
                tvChildName = itemView.findViewById(R.id.tvItemChildName);
                tvSerial = itemView.findViewById(R.id.tvItemSerial);
                tvSex = itemView.findViewById(R.id.tvItemSex);
                tvDiagnosis = itemView.findViewById(R.id.tvItemDiagnosis);
                btnEditRecord = itemView.findViewById(R.id.btnEditRecord);
            }
        }
    }
}