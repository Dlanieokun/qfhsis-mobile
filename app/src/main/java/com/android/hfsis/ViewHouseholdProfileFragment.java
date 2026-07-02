package com.android.hfsis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    private RecyclerView rvProfiles;
    private ProgressBar progressBar;
    private TextView tvEmptyMessage;
    private FloatingActionButton fabAddProfile;
    private SearchView searchViewProfiles;
    private HouseholdProfileAdapter adapter;

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
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfilingFragment())
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
            List<HouseholdProfile> profileList = db.householdProfileDao().getAllProfiles();

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
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHouseholdProfiles();
    }
}