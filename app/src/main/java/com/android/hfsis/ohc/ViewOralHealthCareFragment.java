package com.android.hfsis.ohc;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.ohc.OralHealthCareEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * ViewOralHealthCareFragment
 * Displays a searchable dashboard list of all Oral Health Care entries.
 */
public class ViewOralHealthCareFragment extends Fragment {

    private RecyclerView rvOralRecords;
    private TextView tvRecordCount, tvEmptyState;
    private EditText etSearchOralRecords;
    private FloatingActionButton fabAddOralRecord;

    private OralRecordsAdapter adapter;
    private List<OralHealthCareEntity> masterList = new ArrayList<>();
    private List<OralHealthCareEntity> filteredList = new ArrayList<>();
    private DatabaseHelper db;

    public ViewOralHealthCareFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_oral_health_care, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());

        rvOralRecords = view.findViewById(R.id.rvOralRecords);
        tvRecordCount = view.findViewById(R.id.tvRecordCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        etSearchOralRecords = view.findViewById(R.id.etSearchOralRecords);
        fabAddOralRecord = view.findViewById(R.id.fabAddOralRecord);

        rvOralRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OralRecordsAdapter(filteredList);
        rvOralRecords.setAdapter(adapter);

        fabAddOralRecord.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, OralHealthCareFragment.newInstance(-1))
                    .addToBackStack(null)
                    .commit();
        });

        setupSearchFilter();
        loadOralRecords();
    }

    private void loadOralRecords() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Note: Update this method call if your DAO uses a different name like getAll()
            List<OralHealthCareEntity> records = db.oralHealthCareDao().getAll();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    masterList.clear();
                    if (records != null) {
                        masterList.addAll(records);
                    }
                    filterList(etSearchOralRecords.getText().toString());
                });
            }
        });
    }

    private void setupSearchFilter() {
        etSearchOralRecords.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterList(String query) {
        filteredList.clear();
        String cleanQuery = query.toLowerCase(Locale.US).trim();

        if (cleanQuery.isEmpty()) {
            filteredList.addAll(masterList);
        } else {
            for (OralHealthCareEntity record : masterList) {
                if ((record.name != null && record.name.toLowerCase(Locale.US).contains(cleanQuery)) ||
                        (record.familySerial != null && record.familySerial.toLowerCase(Locale.US).contains(cleanQuery))) {
                    filteredList.add(record);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateUiStates();
    }

    private void updateUiStates() {
        tvRecordCount.setText(String.format(Locale.US, "Total Logged Records: %d", filteredList.size()));
        if (filteredList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvOralRecords.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvOralRecords.setVisibility(View.VISIBLE);
        }
    }

    // --- Inner RecyclerView Adapter Class ---
    private class OralRecordsAdapter extends RecyclerView.Adapter<OralRecordsAdapter.RecordViewHolder> {

        private final List<OralHealthCareEntity> recordsList;

        public OralRecordsAdapter(List<OralHealthCareEntity> recordsList) {
            this.recordsList = recordsList;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_oral_health_care_record, parent, false);
            return new RecordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            OralHealthCareEntity record = filteredList.get(position);

            holder.tvItemPatientName.setText(record.name);
            holder.tvItemAddress.setText(record.address);
            holder.tvItemSerial.setText("Serial: " + (record.familySerial != null ? record.familySerial : "—"));
            holder.tvItemVisitDate.setText("Visited: " + record.dateOfVisit);

            // Format age display dynamically using months/years depending on target demographic
            if (record.ageYears != null && !record.ageYears.isEmpty() && !record.ageYears.equals("0")) {
                holder.tvItemAge.setText("Age: " + record.ageYears + " yrs");
            } else {
                holder.tvItemAge.setText("Age: " + (record.ageMonths != null ? record.ageMonths : "0") + " mos");
            }

            holder.tvItemSex.setText("Sex: " + ("M".equalsIgnoreCase(record.sex) ? "Male" : ("F".equalsIgnoreCase(record.sex) ? "Female" : "—")));
            holder.tvItemRemarks.setText("Remarks: " + (record.remarks != null && !record.remarks.isEmpty() ? record.remarks : "None"));

            // Check completion status flags to set context color badges
            boolean isCompleted = (record.completeRpoc0 == 1 || record.completeRpoc1st == 1 || record.completeRpoc2nd == 1);
            if (isCompleted) {
                holder.tvItemStatus.setText("Status: Completed Care");
                holder.tvItemStatus.setTextColor(Color.parseColor("#22C55E")); // Green
            } else {
                holder.tvItemStatus.setText("Status: Active / Incomplete");
                holder.tvItemStatus.setTextColor(Color.parseColor("#EAB308")); // Yellow
            }

            // --- CLICK LISTENER FOR EDIT BUTTON ---
            holder.btnItemEdit.setOnClickListener(v -> {
                OralHealthCareFragment targetFragment = OralHealthCareFragment.newInstance(record.id);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, targetFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemPatientName, tvItemAddress, tvItemSerial, tvItemVisitDate, tvItemAge, tvItemSex, tvItemRemarks, tvItemStatus;
            ImageButton btnItemEdit;

            public RecordViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemPatientName = itemView.findViewById(R.id.tvItemPatientName);
                tvItemAddress     = itemView.findViewById(R.id.tvItemAddress);
                tvItemSerial      = itemView.findViewById(R.id.tvItemSerial);
                tvItemVisitDate   = itemView.findViewById(R.id.tvItemVisitDate);
                tvItemAge         = itemView.findViewById(R.id.tvItemAge);
                tvItemSex         = itemView.findViewById(R.id.tvItemSex);
                tvItemRemarks     = itemView.findViewById(R.id.tvItemRemarks);
                tvItemStatus      = itemView.findViewById(R.id.tvItemStatus);
                btnItemEdit       = itemView.findViewById(R.id.btnItemEdit);
            }
        }
    }
}