package com.android.hfsis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.HouseholdProfile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.Executors;

public class ViewHouseholdProfileFragment extends Fragment {

    private static final String ARG_SELECTED_PROFILE = "household_profile";

    private RecyclerView rvProfiles;
    private ProgressBar progressBar;
    private TextView tvEmptyMessage;
    private FloatingActionButton fabAddProfile;
    private SearchView searchViewProfiles;
    private HouseholdProfileAdapter adapter;

    /** The profile that was tapped on the previous screen, if any. Used to
     *  scroll to / highlight the matching row once the list has loaded. */
    private HouseholdProfile selectedProfile;

    public ViewHouseholdProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_household_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            selectedProfile = (HouseholdProfile) getArguments().getSerializable(ARG_SELECTED_PROFILE);
        }

        rvProfiles = view.findViewById(R.id.rvProfiles);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        fabAddProfile = view.findViewById(R.id.fabAddProfile);
        searchViewProfiles = view.findViewById(R.id.searchViewProfiles);

        rvProfiles.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HouseholdProfileAdapter();
        rvProfiles.setAdapter(adapter);

        // IMPLEMENTING THE FUNCTION FOR THE VIEW CLASSIFICATION BUTTON
        adapter.setOnClassificationClickListener(profile -> {
            if (getActivity() != null) {
                // Instantiate the new Classification view target screen
                ClassificationFragment classificationFragment = new ClassificationFragment();

                // Bind the active profile ID context parameter to the bundle payload
                Bundle args = new Bundle();
                args.putLong("PROFILE_ID", profile.id);
                classificationFragment.setArguments(args);

                // Perform standard back-stack transaction navigation routing cleanly
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, classificationFragment)
                        .addToBackStack(null) // Allows users to return to the list when pressing back
                        .commit();
            }
        });

        // IMPLEMENTING THE FUNCTION FOR THE EDIT BUTTON
        adapter.setOnEditProfileClickListener(profile -> {
            if (getActivity() != null) {
                ProfilingFragment editFragment = new ProfilingFragment();

                Bundle dataBundle = new Bundle();
                dataBundle.putLong("PROFILE_ID", profile.id);
                editFragment.setArguments(dataBundle);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        searchViewProfiles.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        fabAddProfile.setOnClickListener(v -> {
            if (getActivity() != null) {
                ProfilingFragment addFragment = new ProfilingFragment();

                if (selectedProfile != null) {
                    // Adding a member to an already-known household: carry its
                    // Location & Demographics details over so ProfilingFragment
                    // can pre-fill and lock those fields instead of asking the
                    // user to re-enter them for every member.
                    Bundle prefillArgs = new Bundle();
                    prefillArgs.putSerializable("PREFILL_HOUSEHOLD", selectedProfile);
                    addFragment.setArguments(prefillArgs);
                }

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        loadHouseholdProfiles();
    }

    private void loadHouseholdProfiles() {
        if (getContext() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyMessage.setVisibility(View.GONE);

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());

            List<HouseholdProfile> profileList;
            if (selectedProfile != null && selectedProfile.hhNumber != null) {
                // Show only the members of the household that was tapped,
                // sorted by family number.
                profileList = db.householdProfileDao()
                        .getHouseholdMemberSortedByFamily(selectedProfile.hhNumber);
            } else {
                // No specific household was passed in — show everyone,
                // grouped by household and sorted by family number.
                profileList = db.householdProfileDao().getAllProfilesSorted();
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (searchViewProfiles != null) {
                        searchViewProfiles.setQuery("", false);
                    }

                    if (profileList == null || profileList.isEmpty()) {
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        adapter.setProfiles(profileList);
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                        adapter.setProfiles(profileList);
                        scrollToSelectedProfile();
                    }
                });
            }
        });
    }

    /**
     * If the user arrived here by tapping a specific household (i.e. a
     * profile was passed in via arguments), scroll that row into view and
     * give it a brief highlight pulse so it's easy to spot in the full list.
     */
    private void scrollToSelectedProfile() {
        if (selectedProfile == null || rvProfiles == null) return;

        int position = adapter.getPositionForId(selectedProfile.id);
        if (position < 0) return; // not found (e.g. hidden by an active search filter)

        rvProfiles.post(() -> {
            rvProfiles.smoothScrollToPosition(position);
            rvProfiles.postDelayed(() -> {
                RecyclerView.ViewHolder holder = rvProfiles.findViewHolderForAdapterPosition(position);
                if (holder != null) {
                    pulseHighlight(holder.itemView);
                }
            }, 300);
        });
    }

    private void pulseHighlight(View itemView) {
        itemView.animate()
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setDuration(180)
                .withEndAction(() -> itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(180)
                        .start())
                .start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHouseholdProfiles();
    }
}