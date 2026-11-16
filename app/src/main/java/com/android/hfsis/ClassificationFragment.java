package com.android.hfsis;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

    private static final String NONE_LABEL = "None";

    // Class values that also drive HouseholdProfile.status
    private static final String[] STATUS_OPTIONS = {"Trans-In", "Trans-Out", "Resident", "Dead"};

    private TextView tvClassificationMemberName;
    private TextView tvLegendToggle;
    private View layoutLegendHeader;
    private View gridLegend;

    private EditText etQ1Age, etQ2Age, etQ3Age, etQ4Age;
    private AutoCompleteTextView actQ1Class, actQ2Class, actQ3Class, actQ4Class;
    private Button btnSaveClassification;

    private long profileId = -1;
    private HouseholdProfile householdProfile;
    private ClassificationEntity existingMetrics;

    private final String[] classAbbreviationOptions = new String[]{
            NONE_LABEL, "N", "AB", "SC", "WRA", "S", "A", "P", "AP", "PP", "I", "U", "PWD",
            "Trans-In", "Trans-Out", "Resident", "Dead"
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
        tvLegendToggle = view.findViewById(R.id.tvLegendToggle);
        layoutLegendHeader = view.findViewById(R.id.layoutLegendHeader);
        gridLegend = view.findViewById(R.id.gridLegend);

        etQ1Age = view.findViewById(R.id.etQ1Age);
        etQ2Age = view.findViewById(R.id.etQ2Age);
        etQ3Age = view.findViewById(R.id.etQ3Age);
        etQ4Age = view.findViewById(R.id.etQ4Age);

        actQ1Class = view.findViewById(R.id.actQ1Class);
        actQ2Class = view.findViewById(R.id.actQ2Class);
        actQ3Class = view.findViewById(R.id.actQ3Class);
        actQ4Class = view.findViewById(R.id.actQ4Class);
        btnSaveClassification = view.findViewById(R.id.btnSaveClassification);

        setupQuarterDropdownAdapters();

        layoutLegendHeader.setOnClickListener(v -> {
            boolean show = gridLegend.getVisibility() != View.VISIBLE;
            gridLegend.setVisibility(show ? View.VISIBLE : View.GONE);
            tvLegendToggle.setText(show ? "Hide" : "Show");
        });

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
        // Separate adapter per field so each dropdown always lists every option
        actQ1Class.setAdapter(newClassAdapter());
        actQ2Class.setAdapter(newClassAdapter());
        actQ3Class.setAdapter(newClassAdapter());
        actQ4Class.setAdapter(newClassAdapter());
    }

    private ArrayAdapter<String> newClassAdapter() {
        return new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, classAbbreviationOptions);
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

                        setDropdownValue(actQ1Class, existingMetrics.q1Class);
                        setDropdownValue(actQ2Class, existingMetrics.q2Class);
                        setDropdownValue(actQ3Class, existingMetrics.q3Class);
                        setDropdownValue(actQ4Class, existingMetrics.q4Class);
                    }
                });
            }
        });
    }

    private void saveQuarterClassificationMetrics() {
        if (householdProfile == null || getContext() == null) return;

        // Read every field on the main thread, before the background task starts
        final String q1Age = etQ1Age.getText().toString().trim();
        final String q2Age = etQ2Age.getText().toString().trim();
        final String q3Age = etQ3Age.getText().toString().trim();
        final String q4Age = etQ4Age.getText().toString().trim();
        final String q1Class = getDropdownValue(actQ1Class);
        final String q2Class = getDropdownValue(actQ2Class);
        final String q3Class = getDropdownValue(actQ3Class);
        final String q4Class = getDropdownValue(actQ4Class);

        final Context appContext = requireContext().getApplicationContext();

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(appContext);

            // Always work on the latest row in the DB, not a possibly stale/null copy
            ClassificationEntity metrics = db.classificationDao().getClassificationByProfileId(profileId);
            if (metrics == null) {
                metrics = new ClassificationEntity();
                metrics.profileId = profileId;
                metrics.createdAt = System.currentTimeMillis();
            }

            metrics.q1Age = q1Age;
            metrics.q1Class = q1Class;
            metrics.q2Age = q2Age;
            metrics.q2Class = q2Class;
            metrics.q3Age = q3Age;
            metrics.q3Class = q3Class;
            metrics.q4Age = q4Age;
            metrics.q4Class = q4Class;

            metrics.newInsert = true;
            metrics.isSynced = false;                       // make sure edits get re-synced
            metrics.updatedAt = System.currentTimeMillis();

            long result = db.classificationDao().saveClassification(metrics);

            // Read it back and confirm Q4 really is in the database
            ClassificationEntity saved = db.classificationDao().getClassificationByProfileId(profileId);
            boolean q4Saved = saved != null
                    && sameText(saved.q4Age, q4Age)
                    && sameText(saved.q4Class, q4Class);

            // Auto-update the member's profile status when a status class was selected
            final String newStatus = resolveStatusFromClasses(metrics);
            if (result >= 0 && newStatus != null && householdProfile != null
                    && !newStatus.equals(householdProfile.status)) {
                householdProfile.status = newStatus;
                householdProfile.isSynced = false;          // so the profile gets re-synced
                householdProfile.updatedAt = System.currentTimeMillis();
                db.householdProfileDao().updateProfile(householdProfile);
            }

            final boolean ok = result >= 0 && q4Saved;
            final boolean saveOk = result >= 0;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (ok) {
                        Toast.makeText(getActivity(), "Risk classifications saved successfully", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else if (saveOk) {
                        Toast.makeText(getActivity(), "Saved, but 4th Quarter could not be verified", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Database update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private static boolean sameText(String a, String b) {
        String x = a == null ? "" : a.trim();
        String y = b == null ? "" : b.trim();
        return x.equals(y);
    }

    /**
     * Returns the member status implied by the quarter classes, or null if none is a status value.
     * Checks Q4 -> Q1 so the most recent quarter wins when more than one is set.
     */
    private String resolveStatusFromClasses(ClassificationEntity m) {
        String[] classesLatestFirst = {m.q4Class, m.q3Class, m.q2Class, m.q1Class};
        for (String c : classesLatestFirst) {
            if (c == null || c.trim().isEmpty()) continue;
            for (String status : STATUS_OPTIONS) {
                if (status.equalsIgnoreCase(c.trim())) return status;
            }
        }
        return null;
    }

    /** Returns the selected class abbreviation, or "" when nothing / "None" is selected. */
    private String getDropdownValue(AutoCompleteTextView field) {
        String value = field.getText().toString().trim();
        return NONE_LABEL.equalsIgnoreCase(value) ? "" : value;
    }

    /** Sets the dropdown text without triggering the adapter filter (so all options stay listed). */
    private void setDropdownValue(AutoCompleteTextView field, String value) {
        if (value == null || value.trim().isEmpty()) {
            field.setText("", false);
            return;
        }
        for (String option : classAbbreviationOptions) {
            if (option.equalsIgnoreCase(value.trim())) {
                field.setText(option, false);
                return;
            }
        }
        field.setText("", false);
    }
}