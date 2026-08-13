package com.android.hfsis.vital_statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.vital_statistics.MaternalDeathRecord;

import java.util.List;

public class MaternalDeathListFragment extends Fragment {

    private ListView lvMaternalDeaths;
    private TextView tvNoData;
    private DatabaseHelper db;
    private MaternalDeathAdapter adapter;

    public MaternalDeathListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maternal_death_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(getContext());
        lvMaternalDeaths = view.findViewById(R.id.lvMaternalDeaths);
        tvNoData = view.findViewById(R.id.tvNoData);

        loadRecords();

        lvMaternalDeaths.setOnItemClickListener((parent, view1, position, id) -> {
            MaternalDeathRecord record = (MaternalDeathRecord) adapter.getItem(position);
            if (record != null) {
                openViewDetailsFragment(record.id);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecords();
    }

    private void loadRecords() {
        new Thread(() -> {
            List<MaternalDeathRecord> records = db.maternalDeathDao().getAllRecords();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (records.isEmpty()) {
                        lvMaternalDeaths.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
                    } else {
                        lvMaternalDeaths.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);
                        adapter = new MaternalDeathAdapter(getContext(), records);
                        lvMaternalDeaths.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    private void openViewDetailsFragment(int recordId) {
        MaternalDeathViewFragment viewFragment = new MaternalDeathViewFragment();
        Bundle args = new Bundle();
        args.putInt("recordId", recordId);
        viewFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, viewFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}