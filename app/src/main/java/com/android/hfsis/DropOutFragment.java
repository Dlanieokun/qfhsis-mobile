package com.android.hfsis;

import android.app.DatePickerDialog;
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
import com.android.hfsis.model.DropOutEntity;
import com.android.hfsis.model.HouseholdProfile;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DropOutFragment extends Fragment {

    private int recordId = -1;
    private long profileId = -1;

    private TextView tvDropOutClientName;
    private EditText etDropOutDate;
    private Spinner spinnerDropOutReason;
    private EditText etDropOutRemarks;
    private Button btnSaveDropOut;

    // Reason code definitions translated directly from tracking sheet specifications matrix in image_4a36ec.png
    private final String[] dropOutReasonsArray = {
            "A = Pregnant",
            "B = Desire to become pregnant",
            "C = Medical complications",
            "D = Fear of side effects",
            "E = Changed Clinic",
            "F = Husband disapproves",
            "G = Menopause",
            "H = Lost or moved out of the area or residence",
            "I = Failed to get supply",
            "J = Change Method",
            "K = Underwent hysterectomy",
            "L = Underwent Bilateral Salpingo-oophorectomy",
            "M = No FP commodity",
            "N = Unknown",
            "O = Age out for BTL",
            "P = Change of Age"
    };

    public DropOutFragment() {
        // Required empty structural public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drop_out, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDropOutClientName = view.findViewById(R.id.tvDropOutClientName);
        etDropOutDate = view.findViewById(R.id.etDropOutDate);
        spinnerDropOutReason = view.findViewById(R.id.spinnerDropOutReason);
        etDropOutRemarks = view.findViewById(R.id.etDropOutRemarks);
        btnSaveDropOut = view.findViewById(R.id.btnSaveDropOut);

        // Extract passed intent initialization keys
        if (getArguments() != null) {
            recordId = getArguments().getInt("RECORD_ID", -1);
            profileId = getArguments().getLong("PROFILE_ID", -1);
        }

        setupReasonSpinnerOptions();
        loadClientDemographicsSummary();
        loadExistingDropOutRecordData();

        etDropOutDate.setOnClickListener(v -> showDatePickerDialog());
        btnSaveDropOut.setOnClickListener(v -> saveDropOutFormDetailsToDatabase());
    }

    private void setupReasonSpinnerOptions() {
        if (getContext() == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, dropOutReasonsArray);
        spinnerDropOutReason.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        if (getContext() == null) return;
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, (month + 1), dayOfMonth);
            etDropOutDate.setText(formattedDate);
            etDropOutDate.setError(null);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void loadClientDemographicsSummary() {
        if (profileId == -1 || getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            HouseholdProfile profile = db.householdProfileDao().getProfileById(profileId);

            if (profile != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String nameString = profile.memberLastName + ", " + profile.memberFirstName;
                    if (profile.memberMiddleName != null && !profile.memberMiddleName.isEmpty()) {
                        nameString += " " + profile.memberMiddleName;
                    }
                    tvDropOutClientName.setText(nameString);
                });
            }
        });
    }

    private void loadExistingDropOutRecordData() {
        if (recordId == -1 || getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            DropOutEntity savedEntity = db.dropOutDao().getDropOutByRecordId(recordId);

            if (savedEntity != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    etDropOutDate.setText(savedEntity.dropOutDate);
                    etDropOutRemarks.setText(savedEntity.remarks);

                    // Re-select spinner positional values mapping back matching prefix strings configuration
                    for (int i = 0; i < dropOutReasonsArray.length; i++) {
                        if (dropOutReasonsArray[i].startsWith(savedEntity.reasonCode)) {
                            spinnerDropOutReason.setSelection(i);
                            break;
                        }
                    }
                });
            }
        });
    }

    private void saveDropOutFormDetailsToDatabase() {
        String dropDate = etDropOutDate.getText().toString().trim();
        String remarksText = etDropOutRemarks.getText().toString().trim();

        if (dropDate.isEmpty()) {
            etDropOutDate.setError("Please select the drop-out occurrence date");
            return;
        }

        if (recordId == -1 || getContext() == null) {
            Toast.makeText(getContext(), "Error: Invalid record key reference mapping context", Toast.LENGTH_SHORT).show();
            return;
        }

        // Isolate single character index token prefix (e.g., "A", "B", etc.)
        String selectedReasonItem = spinnerDropOutReason.getSelectedItem().toString();
        String selectedReasonCodeToken = selectedReasonItem.split("=")[0].trim();

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());

            // Check if existing record mapping row requires simple update parameters or clean assignment insert
            DropOutEntity entryModel = db.dropOutDao().getDropOutByRecordId(recordId);
            if (entryModel == null) {
                entryModel = new DropOutEntity();
            }

            entryModel.recordId = recordId;
            entryModel.profileId = profileId;
            entryModel.dropOutDate = dropDate;
            entryModel.reasonCode = selectedReasonCodeToken;
            entryModel.remarks = remarksText;

            // FIX: Use the correct method named in your DropOutDao interface
            db.dropOutDao().insertDropOut(entryModel);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Client drop-out documentation logging success tracking complete", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Smooth transaction transition back to directory grid listing index row panel
                });
            }
        });
    }
}