package com.android.hfsis.idpcs.leprosy;

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

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.idpcs.leprosy.LeprosyRegistryRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViewLeprosyRegistryFragment extends Fragment {

    private RecyclerView rvLeprosyRecords;
    private TextView tvLeprosyRecordCount, tvLeprosyEmptyState;
    private EditText etSearchLeprosyRecords;
    private FloatingActionButton fabAddLeprosyRecord;

    private LeprosyAdapter adapter;
    private final List<LeprosyRegistryRecord> masterList = new ArrayList<>();
    private final List<LeprosyRegistryRecord> filteredList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_leprosy_registry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLeprosyRecords       = view.findViewById(R.id.rvLeprosyRecords);
        tvLeprosyRecordCount   = view.findViewById(R.id.tvLeprosyRecordCount);
        tvLeprosyEmptyState    = view.findViewById(R.id.tvLeprosyEmptyState);
        etSearchLeprosyRecords = view.findViewById(R.id.etSearchLeprosyRecords);
        fabAddLeprosyRecord    = view.findViewById(R.id.fabAddLeprosyRecord);

        database = DatabaseHelper.getInstance(requireContext());

        setupRecyclerView();
        setupSearchFilter();

        fabAddLeprosyRecord.setOnClickListener(v -> navigateToFormFragment(0));

        loadLeprosyRecords();
    }

    private void setupRecyclerView() {
        rvLeprosyRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new LeprosyAdapter(filteredList);
        rvLeprosyRecords.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        etSearchLeprosyRecords.addTextChangedListener(new TextWatcher() {
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

    private void loadLeprosyRecords() {
        new Thread(() -> {
            List<LeprosyRegistryRecord> records = database.leprosyRegistryDao().getAllRecords();
            requireActivity().runOnUiThread(() -> {
                masterList.clear();
                if (records != null) {
                    masterList.addAll(records);
                }
                filterRecords(etSearchLeprosyRecords.getText().toString());
            });
        }).start();
    }

    private void filterRecords(String query) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase(Locale.getDefault()).trim();

        if (lowerQuery.isEmpty()) {
            filteredList.addAll(masterList);
        } else {
            for (LeprosyRegistryRecord r : masterList) {
                if ((r.getName() != null && r.getName().toLowerCase(Locale.getDefault()).contains(lowerQuery)) ||
                        (r.getAddress() != null && r.getAddress().toLowerCase(Locale.getDefault()).contains(lowerQuery))) {
                    filteredList.add(r);
                }
            }
        }

        tvLeprosyRecordCount.setText(String.format(Locale.US, "Total Records: %d", filteredList.size()));
        if (filteredList.isEmpty()) {
            tvLeprosyEmptyState.setVisibility(View.VISIBLE);
            rvLeprosyRecords.setVisibility(View.GONE);
        } else {
            tvLeprosyEmptyState.setVisibility(View.GONE);
            rvLeprosyRecords.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    private void navigateToFormFragment(long id) {
        LeprosyRegistryFragment formFragment = LeprosyRegistryFragment.newInstance(id);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, formFragment)
                .addToBackStack(null)
                .commit();
    }

    private class LeprosyAdapter extends RecyclerView.Adapter<LeprosyAdapter.RecordViewHolder> {

        private final List<LeprosyRegistryRecord> recordsList;

        public LeprosyAdapter(List<LeprosyRegistryRecord> recordsList) {
            this.recordsList = recordsList;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leprosy_record, parent, false);
            return new RecordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            LeprosyRegistryRecord record = recordsList.get(position);
            holder.tvItemClientName.setText(record.getName() != null ? record.getName() : "Unknown");
            holder.tvItemSerial.setText(String.format(Locale.US, "Reg Date: %s", record.getDateOfRegistration()));
            holder.tvItemAddress.setText(record.getAddress() != null ? record.getAddress() : "No Address Listed");

            String classification = "1".equals(record.getClinicalClassification()) ? "PB" : "MB";
            holder.tvItemMetaDetails.setText(String.format(Locale.US, "Age: %d | Sex: %s | Classification: %s",
                    record.getAge(),
                    (record.getSex() != null && !record.getSex().isEmpty()) ? record.getSex() : "--",
                    classification
            ));

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