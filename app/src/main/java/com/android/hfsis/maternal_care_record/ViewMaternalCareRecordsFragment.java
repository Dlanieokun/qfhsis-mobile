package com.android.hfsis.maternal_care_record;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.android.hfsis.MaternalCareAndServicesFragment;
import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.MaternalCareRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ViewMaternalCareRecordsFragment extends Fragment {

    private RecyclerView rvMaternalRecords;
    private TextView tvRecordCount, tvEmptyState;
    private EditText etSearchMaternalRecords;
    private FloatingActionButton fabAddMaternalRecord;

    private MaternalRecordsAdapter adapter;
    private List<MaternalCareRecord> masterList = new ArrayList<>();
    private List<MaternalCareRecord> filteredList = new ArrayList<>();
    private DatabaseHelper db;

    public ViewMaternalCareRecordsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_maternal_care_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getDatabase(requireContext());

        rvMaternalRecords = view.findViewById(R.id.rvMaternalRecords);
        tvRecordCount = view.findViewById(R.id.tvRecordCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        etSearchMaternalRecords = view.findViewById(R.id.etSearchMaternalRecords);
        fabAddMaternalRecord = view.findViewById(R.id.fabAddMaternalRecord);

        rvMaternalRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MaternalRecordsAdapter(filteredList);
        rvMaternalRecords.setAdapter(adapter);

        fabAddMaternalRecord.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MaternalCareAndServicesFragment())
                    .addToBackStack(null)
                    .commit();
        });

        setupSearchFilter();
        loadMaternalRecords();
    }

    private void loadMaternalRecords() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<MaternalCareRecord> records = db.maternalCareDao().getAllMaternalRecords();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    masterList.clear();
                    if (records != null) {
                        masterList.addAll(records);
                    }
                    filterList(etSearchMaternalRecords.getText().toString());
                });
            }
        });
    }

    private void setupSearchFilter() {
        etSearchMaternalRecords.addTextChangedListener(new TextWatcher() {
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
            for (MaternalCareRecord record : masterList) {
                if ((record.patientName != null && record.patientName.toLowerCase(Locale.US).contains(cleanQuery)) ||
                        (record.familySerialNumber != null && record.familySerialNumber.toLowerCase(Locale.US).contains(cleanQuery))) {
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
            rvMaternalRecords.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvMaternalRecords.setVisibility(View.VISIBLE);
        }
    }

    // --- Inner RecyclerView Adapter Class ---
    private class MaternalRecordsAdapter extends RecyclerView.Adapter<MaternalRecordsAdapter.RecordViewHolder> {

        private final List<String> recordsList;

        @SuppressWarnings({"unchecked", "rawtypes"})
        public MaternalRecordsAdapter(List recordsList) {
            this.recordsList = recordsList;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maternal_care_record, parent, false);
            return new RecordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            MaternalCareRecord record = filteredList.get(position);

            holder.tvItemPatientName.setText(record.patientName);
            holder.tvItemAddress.setText(record.homeAddress);
            holder.tvItemSerial.setText("Serial: " + record.familySerialNumber);
            holder.tvItemRegDate.setText("Registered: " + record.registrationDate);
            holder.tvItemAge.setText("Age: " + record.age + " (" + record.ageGroup + ")");
            holder.tvItemGP.setText("G/P: " + record.gravidaPara);
            holder.tvItemLMP.setText("LMP: " + record.lmpDate);
            holder.tvItemEDD.setText("EDD: " + record.eddDate);
            holder.tvItemMetrics.setText(String.format(Locale.US, "Weight: %.1f kg | Height: %.1f cm", record.weightKg, record.heightCm));
            holder.tvItemBmiValue.setText(record.bmiValue);
            holder.tvItemBmiStatus.setText(record.bmiStatus);

            // Change BMI status color based on assessment output string values
            String status = record.bmiStatus != null ? record.bmiStatus : "";
            if (status.equalsIgnoreCase("Underweight")) {
                holder.tvItemBmiStatus.setTextColor(Color.parseColor("#EAB308")); // Yellow
            } else if (status.equalsIgnoreCase("Normal Weight")) {
                holder.tvItemBmiStatus.setTextColor(Color.parseColor("#22C55E")); // Green
            } else {
                holder.tvItemBmiStatus.setTextColor(Color.parseColor("#EF4444")); // Red
            }

            // --- CLICK LISTENER FOR EDIT BUTTON ---
            holder.btnItemEdit.setOnClickListener(v -> {
                // Initialize destination record entry fragment
                MaternalCareAndServicesFragment targetFragment = new MaternalCareAndServicesFragment();

                // Pack target record row database ID inside transmission arguments layout bundle
                Bundle args = new Bundle();
                args.putInt("edit_record_id", record.id);
                targetFragment.setArguments(args);

                // Run screen transaction action to load data into entry inputs form layouts
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, targetFragment)
                        .addToBackStack(null)
                        .commit();
            });

            // 1. Prenatal Stage Module Navigation
            holder.btnItemPrenatal.setOnClickListener(v -> {
                // Initialize target stage logic entity object
                Fragment prenatalFragment = new PrenatalFragment();
                Bundle args = new Bundle();
                args.putInt("maternal_record_id", record.id);
                args.putInt("profile_id", record.profileId);
                args.putString("patient_name", record.patientName);
                prenatalFragment.setArguments(args);

                navigateToFragment(prenatalFragment);
            });

            // 2. Intrapartum Stage Module Navigation
            holder.btnItemIntrapartum.setOnClickListener(v -> {
                // Initialize target stage logic entity object
                Fragment intrapartumFragment = new IntrapartumFragment();
                Bundle args = new Bundle();
                args.putInt("MATERNAL_RECORD_ID", record.id);
                args.putInt("profile_id", record.profileId);
                args.putString("patient_name", record.patientName);
                intrapartumFragment.setArguments(args);

                navigateToFragment(intrapartumFragment);
            });

            // 3. Postpartum Stage Module Navigation
            holder.btnItemPostpartum.setOnClickListener(v -> {
                // Initialize target stage logic entity object
                Fragment postpartumFragment = new PostpartumFragment();
                Bundle args = new Bundle();
                args.putInt("maternal_record_id", record.id);
                args.putInt("profile_id", record.profileId);
                args.putString("patient_name", record.patientName);
                postpartumFragment.setArguments(args);

                navigateToFragment(postpartumFragment);
            });

        }

        // Helper navigation method shared with the row click listeners
        private void navigateToFragment(Fragment fragment) {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemPatientName, tvItemAddress, tvItemSerial, tvItemRegDate, tvItemAge, tvItemGP, tvItemLMP, tvItemEDD, tvItemMetrics, tvItemBmiValue, tvItemBmiStatus;
            LinearLayout layoutBmiBadge;
            Button btnItemPrenatal, btnItemIntrapartum, btnItemPostpartum;
            ImageButton btnItemEdit;

            public RecordViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemPatientName = itemView.findViewById(R.id.tvItemPatientName);
                tvItemAddress = itemView.findViewById(R.id.tvItemAddress);
                tvItemSerial = itemView.findViewById(R.id.tvItemSerial);
                tvItemRegDate = itemView.findViewById(R.id.tvItemRegDate);
                tvItemAge = itemView.findViewById(R.id.tvItemAge);
                tvItemGP = itemView.findViewById(R.id.tvItemGP);
                tvItemLMP = itemView.findViewById(R.id.tvItemLMP);
                tvItemEDD = itemView.findViewById(R.id.tvItemEDD);
                tvItemMetrics = itemView.findViewById(R.id.tvItemMetrics);
                tvItemBmiValue = itemView.findViewById(R.id.tvItemBmiValue);
                tvItemBmiStatus = itemView.findViewById(R.id.tvItemBmiStatus);
                layoutBmiBadge = itemView.findViewById(R.id.layoutBmiBadge);
                btnItemEdit = itemView.findViewById(R.id.btnItemEdit);
                btnItemPrenatal = itemView.findViewById(R.id.btnItemPrenatal);
                btnItemIntrapartum = itemView.findViewById(R.id.btnItemIntrapartum);
                btnItemPostpartum = itemView.findViewById(R.id.btnItemPostpartum);
            }
        }
    }
}