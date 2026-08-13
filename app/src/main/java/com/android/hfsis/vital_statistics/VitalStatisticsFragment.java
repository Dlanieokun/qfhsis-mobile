package com.android.hfsis.vital_statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.google.android.material.tabs.TabLayout;

public class VitalStatisticsFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Button btnAddNew;
    private DatabaseHelper db;

    public VitalStatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vital_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(getContext());

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        btnAddNew = view.findViewById(R.id.btnAddNew);

        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);

        btnAddNew.setOnClickListener(v -> {
            int currentTab = tabLayout.getSelectedTabPosition();
            if (currentTab == 0) {
                // Maternal Deaths Tab
                startAddMaternalDeathForm();
            } else {
                // Infant Deaths Tab
                startAddInfantDeathForm();
            }
        });
    }

    private void setupViewPager() {
        VitalStatisticsPagerAdapter adapter = new VitalStatisticsPagerAdapter(
                getChildFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        );
        adapter.addFragment(new MaternalDeathListFragment(), "Maternal Deaths");
        adapter.addFragment(new InfantDeathListFragment(), "Infant Deaths");
        viewPager.setAdapter(adapter);
    }

    private void startAddMaternalDeathForm() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MaternalDeathFormFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void startAddInfantDeathForm() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new InfantDeathFormFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }
}