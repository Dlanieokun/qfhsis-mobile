package com.android.hfsis.vital_statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.vital_statistics.InfantDeathRecord;

import java.util.List;

public class InfantDeathListFragment extends Fragment {

    private ListView lvInfantDeaths;
    private TextView tvNoData;
    private DatabaseHelper db;
    private InfantDeathAdapter adapter;

    public InfantDeathListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_infant_death_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(getContext());
        lvInfantDeaths = view.findViewById(R.id.lvInfantDeaths);
        tvNoData = view.findViewById(R.id.tvNoData);

        loadRecords();

        lvInfantDeaths.setOnItemClickListener((parent, view1, position, id) -> {
            InfantDeathRecord record = (InfantDeathRecord) adapter.getItem(position);
            if (record != null) {
                openViewDetailsFragment(record.getId());
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
            List<InfantDeathRecord> records = db.infantDeathDao().getAllRecords();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (records.isEmpty()) {
                        lvInfantDeaths.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
                    } else {
                        lvInfantDeaths.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);
                        adapter = new InfantDeathAdapter(getContext(), records);
                        lvInfantDeaths.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    private void openViewDetailsFragment(int recordId) {
        InfantDeathViewFragment viewFragment = new InfantDeathViewFragment();
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