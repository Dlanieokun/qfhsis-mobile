package com.android.hfsis.idpcs.filariasis;

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
import com.android.hfsis.database.idpcs.filariasis.FilariasisDao;
import com.android.hfsis.model.idpcs.filariasis.FilariasisRegistryRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewFilariasisRegistryFragment extends Fragment {

    private RecyclerView rvFilariasisRecords;
    private TextView tvFilariasisRecordCount, tvFilariasisEmptyState;
    private EditText etSearchFilariasisRecords;
    private FloatingActionButton fabAddFilariasisRecord;

    private FilariasisRegistryAdapter adapter;
    private final List<FilariasisRegistryRecord> masterList = new ArrayList<>();
    private final List<FilariasisRegistryRecord> filteredList = new ArrayList<>();
    private DatabaseHelper database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_filariasis_registry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFilariasisRecords       = view.findViewById(R.id.rvEyesRecords);
        tvFilariasisRecordCount   = view.findViewById(R.id.tvEyesRecordCount);
        tvFilariasisEmptyState    = view.findViewById(R.id.tvEyesEmptyState);
        etSearchFilariasisRecords = view.findViewById(R.id.etSearchEyesRecords);
        fabAddFilariasisRecord    = view.findViewById(R.id.fabAddEyesRecord);

        database = DatabaseHelper.getInstance(requireContext());

        setupRecyclerView();
        setupSearchFilter();

        fabAddFilariasisRecord.setOnClickListener(v -> navigateToFormFragment(0));

        loadRegistryRecords();
    }

    private void setupRecyclerView() {
        rvFilariasisRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FilariasisRegistryAdapter(filteredList);
        rvFilariasisRecords.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        etSearchFilariasisRecords.addTextChangedListener(new TextWatcher() {
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

    private void loadRegistryRecords() {
        executorService.execute(() -> {
            try {
                FilariasisDao dao = database.filariasisDao();
                List<FilariasisRegistryRecord> records = dao.getAllRecords();

                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        masterList.clear();
                        if (records != null) {
                            masterList.addAll(records);
                        }
                        applySearchFilter(etSearchFilariasisRecords.getText().toString());
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to read records: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void applySearchFilter(String query) {
        filteredList.clear();
        String constraint = query.trim().toLowerCase(Locale.getDefault());

        if (constraint.isEmpty()) {
            filteredList.addAll(masterList);
        } else {
            for (FilariasisRegistryRecord entity : masterList) {
                boolean matchesName = entity.getName() != null && entity.getName().toLowerCase(Locale.getDefault()).contains(constraint);
                boolean matchesSerial = entity.getFamilySerialNumber() != null && entity.getFamilySerialNumber().toLowerCase(Locale.getDefault()).contains(constraint);

                if (matchesName || matchesSerial) {
                    filteredList.add(entity);
                }
            }
        }

        tvFilariasisRecordCount.setText(String.format(Locale.US, "Total Records: %d", filteredList.size()));

        if (filteredList.isEmpty()) {
            tvFilariasisEmptyState.setVisibility(View.VISIBLE);
            rvFilariasisRecords.setVisibility(View.GONE);
        } else {
            tvFilariasisEmptyState.setVisibility(View.GONE);
            rvFilariasisRecords.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private void navigateToFormFragment(long recordId) {
        Fragment formFragment;
        if (recordId > 0) {
            formFragment = FilariasisRegistryFragment.newInstanceForEdit(recordId);
        } else {
            formFragment = FilariasisRegistryFragment.newInstance();
        }

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private class FilariasisRegistryAdapter extends RecyclerView.Adapter<FilariasisRegistryAdapter.RecordViewHolder> {

        private final List<FilariasisRegistryRecord> recordsList;

        public FilariasisRegistryAdapter(List<FilariasisRegistryRecord> recordsList) {
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
            FilariasisRegistryRecord record = recordsList.get(position);

            holder.tvItemClientName.setText(record.getName() != null ? record.getName() : "Unknown Client");
            holder.tvItemSerial.setText(String.format("Serial: %s", (record.getFamilySerialNumber() != null && !record.getFamilySerialNumber().isEmpty()) ? record.getFamilySerialNumber() : "N/A"));
            holder.tvItemAddress.setText(record.getAddress() != null ? record.getAddress() : "No Address Listed");

            holder.tvItemMetaDetails.setText(String.format(Locale.US, "Age: %d | Sex: %s | Date Registered: %s",
                    record.getAge(),
                    (record.getSex() != null && !record.getSex().isEmpty()) ? record.getSex() : "--",
                    (record.getDateOfRegistration() != null && !record.getDateOfRegistration().isEmpty()) ? record.getDateOfRegistration() : "--"
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