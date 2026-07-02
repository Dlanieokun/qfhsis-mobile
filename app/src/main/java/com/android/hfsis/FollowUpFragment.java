package com.android.hfsis;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.FollowUpEntity;
import com.android.hfsis.model.HouseholdProfile;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class FollowUpFragment extends Fragment {

    private int recordId = -1;
    private long profileId = -1;

    private TextView tvFollowUpClientName;
    private LinearLayout monthsContainer;
    private Button btnSaveFollowUp;

    private final String[] monthsArray = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    // In-memory view holder maps to cleanly retrieve tracking entries later
    private final Map<String, EditText> scheduledInputsMap = new HashMap<>();
    private final Map<String, EditText> actualInputsMap = new HashMap<>();

    public FollowUpFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_follow_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFollowUpClientName = view.findViewById(R.id.tvFollowUpClientName);
        monthsContainer = view.findViewById(R.id.monthsContainer);
        btnSaveFollowUp = view.findViewById(R.id.btnSaveFollowUp);

        // Parse arguments received from transaction bundle mapping safely
        if (getArguments() != null) {
            recordId = getArguments().getInt("RECORD_ID", -1);
            profileId = getArguments().getLong("PROFILE_ID", -1);
        }

        buildMonthlyFormRows();
        loadClientProfileSummary();
        loadExistingFollowUpRecords();

        btnSaveFollowUp.setOnClickListener(v -> saveAllMonthlyFollowUpRows());
    }

    private void buildMonthlyFormRows() {
        if (getContext() == null) return;
        monthsContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (String month : monthsArray) {
            View monthRow = inflater.inflate(R.layout.item_month_row, monthsContainer, false);

            TextView tvLabel = monthRow.findViewById(R.id.tvMonthLabel);
            EditText etScheduled = monthRow.findViewById(R.id.etScheduledDate);
            EditText etActual = monthRow.findViewById(R.id.etActualDate);

            tvLabel.setText(month);

            // Attach interactive date picker dialog operations
            etScheduled.setOnClickListener(v -> showDatePicker(etScheduled));
            etActual.setOnClickListener(v -> showDatePicker(etActual));

            // Store entry layout references mapped under month keys
            scheduledInputsMap.put(month, etScheduled);
            actualInputsMap.put(month, etActual);

            monthsContainer.addView(monthRow);
        }
    }

    private void showDatePicker(EditText targetField) {
        if (getContext() == null) return;
        final Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, (month + 1), dayOfMonth);
            targetField.setText(formattedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void loadClientProfileSummary() {
        if (profileId == -1 || getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            HouseholdProfile profile = db.householdProfileDao().getProfileById(profileId);

            if (profile != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String fullDisplayName = profile.memberLastName + ", " + profile.memberFirstName;
                    if (profile.memberMiddleName != null && !profile.memberMiddleName.isEmpty()) {
                        fullDisplayName += " " + profile.memberMiddleName;
                    }
                    tvFollowUpClientName.setText(fullDisplayName);
                });
            }
        });
    }

    private void loadExistingFollowUpRecords() {
        if (recordId == -1 || getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            // Make sure your database implementation includes a valid follow-up DAO retrieval path
            List<FollowUpEntity> savedList = db.followUpDao().getFollowUpsForRecord(recordId);

            if (savedList != null && !savedList.isEmpty() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    for (FollowUpEntity entity : savedList) {
                        EditText etSched = scheduledInputsMap.get(entity.monthName);
                        EditText etAct = actualInputsMap.get(entity.monthName);

                        if (etSched != null) etSched.setText(entity.scheduledDate);
                        if (etAct != null) etAct.setText(entity.actualDate);
                    }
                });
            }
        });
    }

    private void saveAllMonthlyFollowUpRows() {
        if (recordId == -1 || getContext() == null) {
            Toast.makeText(getContext(), "Error: Invalid Record Context Association Match", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());

            for (String month : monthsArray) {
                EditText etSched = scheduledInputsMap.get(month);
                EditText etAct = actualInputsMap.get(month);

                String sDate = (etSched != null) ? etSched.getText().toString().trim() : "";
                String aDate = (etAct != null) ? etAct.getText().toString().trim() : "";

                // Only persist tracking rows that have at least one valid date entry
                if (!sDate.isEmpty() || !aDate.isEmpty()) {
                    FollowUpEntity existingRow = db.followUpDao().getFollowUpByMonth(recordId, month);

                    if (existingRow == null) {
                        FollowUpEntity newRow = new FollowUpEntity();
                        newRow.recordId = recordId;
                        newRow.profileId = profileId;
                        newRow.monthName = month;
                        newRow.scheduledDate = sDate;
                        newRow.actualDate = aDate;
                        db.followUpDao().insertFollowUp(newRow);
                    } else {
                        existingRow.scheduledDate = sDate;
                        existingRow.actualDate = aDate;
                        db.followUpDao().updateFollowUp(existingRow);
                    }
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Follow-up schedule metrics updated", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Smooth return navigation back to primary list array view
                });
            }
        });
    }
}