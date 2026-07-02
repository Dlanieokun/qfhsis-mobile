package com.android.hfsis.child;

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
import com.android.hfsis.model.child.ChildImmunizationSchoolRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ViewChildImmunizationSchoolFragment extends Fragment {

    private RecyclerView rvSchoolRecords;
    private TextView tvSchoolRecordCount, tvSchoolEmptyState;
    private EditText etSearchSchoolRecords;
    private FloatingActionButton fabAddSchoolRecord;

    private SchoolRecordAdapter adapter;
    private final List<ChildImmunizationSchoolRecord> masterList = new ArrayList<>();
    private DatabaseHelper database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_child_immunization_school, container, false);

        database = DatabaseHelper.getInstance(getContext());
        initViews(view);

        // Bind LiveData tracking stream listener
        database.childImmunizationSchoolDao().getAllSchoolRecords().observe(getViewLifecycleOwner(), records -> {
            if (records != null) {
                masterList.clear();
                masterList.addAll(records);
                filterRecords(etSearchSchoolRecords.getText().toString());
            }
        });

        return view;
    }

    private void initViews(View view) {
        rvSchoolRecords = view.findViewById(R.id.rvSchoolRecords);
        tvSchoolRecordCount = view.findViewById(R.id.tvSchoolRecordCount);
        tvSchoolEmptyState = view.findViewById(R.id.tvSchoolEmptyState);
        etSearchSchoolRecords = view.findViewById(R.id.etSearchSchoolRecords);
        fabAddSchoolRecord = view.findViewById(R.id.fabAddSchoolRecord);

        rvSchoolRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SchoolRecordAdapter(new ArrayList<>());
        rvSchoolRecords.setAdapter(adapter);

        // FAB navigates to the add form
        fabAddSchoolRecord.setOnClickListener(v -> navigateToForm(null));

        // Setup real-time filtering as the user types
        etSearchSchoolRecords.addTextChangedListener(new TextWatcher() {
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

    private void filterRecords(String query) {
        if (query.trim().isEmpty()) {
            updateUiList(masterList);
        } else {
            // Offload database search queries to a background thread to prevent UI freezing
            Executors.newSingleThreadExecutor().execute(() -> {
                String dynamicQuery = "%" + query.trim() + "%";
                List<ChildImmunizationSchoolRecord> filterResults = database.childImmunizationSchoolDao().searchSchoolRecords(dynamicQuery);

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> updateUiList(filterResults));
                }
            });
        }
    }

    private void updateUiList(List<ChildImmunizationSchoolRecord> displayList) {
        adapter.updateList(displayList);
        tvSchoolRecordCount.setText("Total Records: " + displayList.size());

        if (displayList.isEmpty()) {
            tvSchoolEmptyState.setVisibility(View.VISIBLE);
            rvSchoolRecords.setVisibility(View.GONE);
        } else {
            tvSchoolEmptyState.setVisibility(View.GONE);
            rvSchoolRecords.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToForm(@Nullable ChildImmunizationSchoolRecord record) {
        ChildImmunizationSchoolFragment formFragment = new ChildImmunizationSchoolFragment();
        if (record != null) {
            Bundle args = new Bundle();
            args.putLong("EDIT_RECORD_ID", record.getId());
            formFragment.setArguments(args);
        }

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // ── RECYCLERVIEW ADAPTER IMPLEMENTATION ─────────────────
    private class SchoolRecordAdapter extends RecyclerView.Adapter<SchoolRecordAdapter.RecordViewHolder> {

        private final List<ChildImmunizationSchoolRecord> recordsList;

        public SchoolRecordAdapter(List<ChildImmunizationSchoolRecord> recordsList) {
            this.recordsList = recordsList;
        }

        public void updateList(List<ChildImmunizationSchoolRecord> newRecords) {
            this.recordsList.clear();
            this.recordsList.addAll(newRecords);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflates your requested custom row item view layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child_immunization_school, parent, false);
            return new RecordViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            ChildImmunizationSchoolRecord record = recordsList.get(position);

            holder.tvItemChildName.setText(record.getChildName());
            holder.tvItemSerial.setText("Serial: " + (record.getFamilySerialNumber().isEmpty() ? "N/A" : record.getFamilySerialNumber()));
            holder.tvItemRegDate.setText("Registered: " + record.getRegistrationDate());
            holder.tvItemAgeMonths.setText("Age: " + record.getAgeYears() + " yrs");
            holder.tvItemSex.setText("Sex: " + record.getSex());
            holder.tvItemAddress.setText("Address: " + (record.getAddress().isEmpty() ? "N/A" : record.getAddress()));

            holder.tvItemStatus.setText("Grade Status: Grade " + record.getGradeLevel());

            holder.btnItemEdit.setOnClickListener(v -> navigateToForm(record));
        }

        @Override
        public int getItemCount() {
            return recordsList.size();
        }

        class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemChildName, tvItemAddress, tvItemSerial, tvItemRegDate, tvItemAgeMonths, tvItemSex, tvItemStatus;
            Button btnItemEdit;

            public RecordViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemChildName = itemView.findViewById(R.id.tvItemChildName);
                tvItemAddress = itemView.findViewById(R.id.tvItemAddress);
                tvItemSerial = itemView.findViewById(R.id.tvItemSerial);
                tvItemRegDate = itemView.findViewById(R.id.tvItemRegDate);
                tvItemAgeMonths = itemView.findViewById(R.id.tvItemAgeMonths);
                tvItemSex = itemView.findViewById(R.id.tvItemSex);
                tvItemStatus = itemView.findViewById(R.id.tvItemStatus);
                btnItemEdit = itemView.findViewById(R.id.btnItemEdit);
            }
        }
    }
}