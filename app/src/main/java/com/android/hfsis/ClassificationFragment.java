package com.android.hfsis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.ClassificationEntity;
import com.android.hfsis.model.HouseholdProfile;

import java.util.concurrent.Executors;

public class ClassificationFragment extends Fragment {

    private TextView tvClassificationMemberName;
    private EditText etQ1Age, etQ2Age, etQ3Age, etQ4Age;
    private Spinner spinnerQ1Class, spinnerQ2Class, spinnerQ3Class, spinnerQ4Class;
    private Button btnSaveClassification;

    private long profileId = -1;
    private HouseholdProfile householdProfile;
    private ClassificationEntity existingMetrics;

    private final String[] classAbbreviationOptions = new String[]{
            "Select Class", "N", "AB", "SC", "WRA", "S", "A", "P", "AP", "PP", "I", "U", "PWD"
    };

    public ClassificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_classification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvClassificationMemberName = view.findViewById(R.id.tvClassificationMemberName);
        etQ1Age = view.findViewById(R.id.etQ1Age);
        etQ2Age = view.findViewById(R.id.etQ2Age);
        etQ3Age = view.findViewById(R.id.etQ3Age);
        etQ4Age = view.findViewById(R.id.etQ4Age);

        spinnerQ1Class = view.findViewById(R.id.spinnerQ1Class);
        spinnerQ2Class = view.findViewById(R.id.spinnerQ2Class);
        spinnerQ3Class = view.findViewById(R.id.spinnerQ3Class);
        spinnerQ4Class = view.findViewById(R.id.spinnerQ4Class);
        btnSaveClassification = view.findViewById(R.id.btnSaveClassification);

        setupQuarterDropdownAdapters();

        if (getArguments() != null && getArguments().containsKey("PROFILE_ID")) {
            profileId = getArguments().getLong("PROFILE_ID");
            loadProfileAndClassificationData();
        } else {
            Toast.makeText(getContext(), "Error: Missing data link configuration", Toast.LENGTH_SHORT).show();
        }

        btnSaveClassification.setOnClickListener(v -> saveQuarterClassificationMetrics());
    }

    private void setupQuarterDropdownAdapters() {
        if (getContext() == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, classAbbreviationOptions);
        spinnerQ1Class.setAdapter(adapter);
        spinnerQ2Class.setAdapter(adapter);
        spinnerQ3Class.setAdapter(adapter);
        spinnerQ4Class.setAdapter(adapter);
    }

    private void loadProfileAndClassificationData() {
        if (getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            householdProfile = db.householdProfileDao().getProfileById(profileId);
            existingMetrics = db.classificationDao().getClassificationByProfileId(profileId);

            if (householdProfile != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String formattedName = "Member: " + householdProfile.memberLastName + ", " + householdProfile.memberFirstName;
                    tvClassificationMemberName.setText(formattedName);

                    if (existingMetrics != null) {
                        etQ1Age.setText(existingMetrics.q1Age);
                        etQ2Age.setText(existingMetrics.q2Age);
                        etQ3Age.setText(existingMetrics.q3Age);
                        etQ4Age.setText(existingMetrics.q4Age);

                        setSpinnerValue(spinnerQ1Class, existingMetrics.q1Class);
                        setSpinnerValue(spinnerQ2Class, existingMetrics.q2Class);
                        setSpinnerValue(spinnerQ3Class, existingMetrics.q3Class);
                        setSpinnerValue(spinnerQ4Class, existingMetrics.q4Class);
                    }
                });
            }
        });
    }

    private void saveQuarterClassificationMetrics() {
        if (householdProfile == null || getContext() == null) return;

        ClassificationEntity metrics;
        if (existingMetrics != null) {
            metrics = existingMetrics;
        } else {
            metrics = new ClassificationEntity();
            metrics.profileId = profileId;
            metrics.createdAt = System.currentTimeMillis();
        }

        metrics.q1Age = etQ1Age.getText().toString().trim();
        metrics.q1Class = spinnerQ1Class.getSelectedItemPosition() > 0 ? spinnerQ1Class.getSelectedItem().toString() : "";

        metrics.q2Age = etQ2Age.getText().toString().trim();
        metrics.q2Class = spinnerQ2Class.getSelectedItemPosition() > 0 ? spinnerQ2Class.getSelectedItem().toString() : "";

        metrics.q3Age = etQ3Age.getText().toString().trim();
        metrics.q3Class = spinnerQ3Class.getSelectedItemPosition() > 0 ? spinnerQ3Class.getSelectedItem().toString() : "";

        metrics.q4Age = etQ4Age.getText().toString().trim();
        metrics.q4Class = spinnerQ4Class.getSelectedItemPosition() > 0 ? spinnerQ4Class.getSelectedItem().toString() : "";

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            long result = db.classificationDao().saveClassification(metrics);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (result >= 0) {
                        Toast.makeText(getActivity(), "Risk classifications saved successfully", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getActivity(), "Database update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null || spinner.getAdapter() == null) return;
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            if (spinner.getAdapter().getItem(i).toString().trim().equalsIgnoreCase(value.trim())) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}