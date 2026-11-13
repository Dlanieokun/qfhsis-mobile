package com.android.hfsis;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.database.HouseholdProfileDao;
import com.android.hfsis.model.HouseholdProfile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Displays all saved household profiles in a searchable, scrollable list.
 * Tapping an entry (or its arrow icon) opens ViewHouseholdProfileFragment.
 *
 * Assumptions (adjust to match your project if different):
 *  - AppDatabase.getInstance(Context) exposes householdProfileDao()
 *  - The hosting Activity has a container view with id R.id.fragment_container
 *    that fragment transactions replace() into (same pattern used by
 *    ViewHouseholdProfileFragment)
 *  - res/values/strings.xml has a "household_number_label" string with a %s placeholder,
 *    e.g. <string name="household_number_label">HH No: %s</string>
 */
public class ViewHouseholdListFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateText;
    private TextView textActiveCount;
    private SearchView searchView;
    private FloatingActionButton fabAddHousehold;

    private HouseholdListAdapter adapter;
    private HouseholdProfileDao householdProfileDao;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ViewHouseholdListFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_household_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewHouseholds);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyStateText = view.findViewById(R.id.textEmptyState);
        textActiveCount = view.findViewById(R.id.textActiveCount);
        searchView = view.findViewById(R.id.searchViewHouseholds);
        fabAddHousehold = view.findViewById(R.id.fabAddHousehold);

        householdProfileDao = DatabaseHelper.getInstance(requireContext()).householdProfileDao();

        setupRecyclerView();
        setupSearch();
        setupSwipeRefresh();
        setupFab();

        loadHouseholdProfiles();
    }

    private void setupRecyclerView() {
        adapter = new HouseholdListAdapter(this::onHouseholdClicked);
        // Cards already have their own spacing/elevation, so no DividerItemDecoration here.
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                updateActiveCount();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                updateActiveCount();
                return true;
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadHouseholdProfiles);
    }

    private void setupFab() {
        fabAddHousehold.setOnClickListener(v -> {
            if (getActivity() == null) return;

            ProfilingFragment newHouseholdFragment = new ProfilingFragment();

            // A brand-new household's first member is always its Head —
            // force that selection and lock the field.
            Bundle args = new Bundle();
            args.putBoolean(ARG_FORCE_HEAD_RELATIONSHIP, true);
            newHouseholdFragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newHouseholdFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private static final String ARG_SELECTED_PROFILE = "household_profile";
    private static final String ARG_FORCE_HEAD_RELATIONSHIP = "FORCE_HEAD_RELATIONSHIP";

    private void onHouseholdClicked(HouseholdProfile profile) {
        if (getActivity() == null) return;

        ViewHouseholdProfileFragment profileFragment = new ViewHouseholdProfileFragment();

        // Send the tapped profile's full data across, not just its id, so
        // ViewHouseholdProfileFragment can scroll to / highlight it directly
        // without having to guess which row the user came from.
        Bundle args = new Bundle();
        args.putSerializable(ARG_SELECTED_PROFILE, profile);
        profileFragment.setArguments(args);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadHouseholdProfiles() {
        swipeRefreshLayout.setRefreshing(true);
        executorService.execute(() -> {
            List<HouseholdProfile> profiles = householdProfileDao.getAllHousehold();
            mainHandler.post(() -> {
                adapter.submitList(profiles);
                updateEmptyState(profiles == null || profiles.isEmpty());
                updateActiveCount();
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updateActiveCount() {
        int count = adapter.getVisibleCount();
        textActiveCount.setText(getString(R.string.household_active_count, count));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView.setAdapter(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}