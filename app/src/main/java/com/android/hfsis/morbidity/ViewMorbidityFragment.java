package com.android.hfsis.morbidity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.morbidity.MorbidityRecord;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ViewMorbidityFragment extends Fragment {

    private RecyclerView recyclerView;
    private MorbidityAdapter adapter;
    private List<MorbidityRecord> allRecords = new ArrayList<>();
    private EditText etSearch;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_morbidity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_morbidity);
        etSearch = view.findViewById(R.id.et_search);
        tvEmpty = view.findViewById(R.id.tv_empty);
        fabAdd = view.findViewById(R.id.fab_add);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MorbidityAdapter(new ArrayList<>(), this::onRecordClick, this::onEditClick, this::onDeleteClick);
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecords(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> navigateToForm(null));
        loadRecords();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecords();
    }

    private void loadRecords() {
        new Thread(() -> {
            allRecords = DatabaseHelper.getInstance(requireContext())
                    .morbidityDao().getAllRecords();
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    adapter.updateData(allRecords);
                    tvEmpty.setVisibility(allRecords.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(allRecords.isEmpty() ? View.GONE : View.VISIBLE);
                });
            }
        }).start();
    }

    private void filterRecords(String query) {
        List<MorbidityRecord> filtered = new ArrayList<>();
        for (MorbidityRecord record : allRecords) {
            String disease = record.getDiseaseName() != null ? record.getDiseaseName().toLowerCase() : "";
            String icd = record.getIcdCode() != null ? record.getIcdCode().toLowerCase() : "";
            String period = (record.getReportMonth() + " " + record.getReportYear()).toLowerCase();
            if (query.isEmpty() || disease.contains(query.toLowerCase())
                    || icd.contains(query.toLowerCase())
                    || period.contains(query.toLowerCase())) {
                filtered.add(record);
            }
        }
        adapter.updateData(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void onRecordClick(MorbidityRecord record) {
        navigateToDetailView(record);
    }

    private void onEditClick(MorbidityRecord record) {
        navigateToForm(record);
    }

    private void onDeleteClick(MorbidityRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Record")
                .setMessage("Are you sure you want to delete this morbidity record?\n\n" +
                        record.getDiseaseName() + " (" + record.getReportMonth() + " " + record.getReportYear() + ")")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        DatabaseHelper.getInstance(requireContext()).morbidityDao().delete(record);
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Record deleted", Toast.LENGTH_SHORT).show();
                                loadRecords();
                            });
                        }
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToForm(@Nullable MorbidityRecord record) {
        Fragment fragment = record == null
                ? MorbidityFragment.newInstance()
                : MorbidityFragment.newInstanceForEdit(record.getId());

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToDetailView(MorbidityRecord record) {
        MorbidityDetailFragment detailFragment = MorbidityDetailFragment.newInstance(record.getId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}