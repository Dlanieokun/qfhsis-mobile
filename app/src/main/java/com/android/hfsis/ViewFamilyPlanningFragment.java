package com.android.hfsis;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.FamilyPlanningRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.Executors;

public class ViewFamilyPlanningFragment extends Fragment {

    private RecyclerView rvFamilyPlanning;
    private LinearLayout layoutEmptyState;
    private TextView tvTotalRecordsCount;
    private EditText etSearchFP;
    private FloatingActionButton fabAddFP;
    private FamilyPlanningAdapter adapter;

    public ViewFamilyPlanningFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_family_planning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFamilyPlanning = view.findViewById(R.id.rvFamilyPlanning);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        tvTotalRecordsCount = view.findViewById(R.id.tvTotalRecordsCount);
        etSearchFP = view.findViewById(R.id.etSearchFP);
        fabAddFP = view.findViewById(R.id.fabAddFP);

        rvFamilyPlanning.setLayoutManager(new LinearLayoutManager(getContext()));
        // Pass the context and the interface click handler
        // Inside your ViewFamilyPlanningFragment class onViewCreated method:

        adapter = new FamilyPlanningAdapter(requireContext(), new FamilyPlanningAdapter.OnRecordActionListener() {
            @Override
            public void onEditClick(FamilyPlanningRecord record) {
                FamilyPlanningFragment editFormFragment = new FamilyPlanningFragment();
                Bundle args = new Bundle();
                args.putInt("RECORD_ID", record.id);
                editFormFragment.setArguments(args);

                navigateToFragment(editFormFragment);
            }

            @Override
            public void onFollowUpClick(FamilyPlanningRecord record) {
                FollowUpFragment followUpFragment = new FollowUpFragment();
                Bundle args = new Bundle();
                args.putInt("RECORD_ID", record.id);
                args.putLong("PROFILE_ID", record.profileId);
                followUpFragment.setArguments(args);

                navigateToFragment(followUpFragment);
            }

            @Override
            public void onDropOutClick(FamilyPlanningRecord record) {
                // Open the Drop-out Tracking interface details screen
                DropOutFragment dropOutFragment = new DropOutFragment();
                Bundle args = new Bundle();

                // Pass essential identifier keys so the DropOut layout loads matching records data context
                args.putInt("RECORD_ID", record.id);
                args.putLong("PROFILE_ID", record.profileId);
                dropOutFragment.setArguments(args);

                navigateToFragment(dropOutFragment);
            }
        });

        rvFamilyPlanning.setAdapter(adapter);

        // Attach dynamic room filter database event handlers when typing
        etSearchFP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performLiveSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add Record Navigation Command Pattern Route
        fabAddFP.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FamilyPlanningFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        loadSavedRecords();
    }

    private void loadSavedRecords() {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            List<FamilyPlanningRecord> records = db.familyPlanningDao().getAllRecords();

            updateUIWithRecords(records);
        });
    }

    private void navigateToFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void performLiveSearch(String query) {
        if (getContext() == null) return;

        if (query.isEmpty()) {
            loadSavedRecords();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            List<FamilyPlanningRecord> filtered = db.familyPlanningDao().searchRecordsByClientName("%" + query + "%");

            updateUIWithRecords(filtered);
        });
    }

    private void updateUIWithRecords(List<FamilyPlanningRecord> records) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (records == null || records.isEmpty()) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    rvFamilyPlanning.setVisibility(View.GONE);
                    tvTotalRecordsCount.setText("Total Records: 0");
                } else {
                    layoutEmptyState.setVisibility(View.GONE);
                    rvFamilyPlanning.setVisibility(View.VISIBLE);
                    tvTotalRecordsCount.setText("Total Saved Records: " + records.size());
                    adapter.setRecords(records);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedRecords();
    }
}