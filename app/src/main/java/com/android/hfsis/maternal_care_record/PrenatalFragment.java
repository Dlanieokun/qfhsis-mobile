package com.android.hfsis.maternal_care_record;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;

public class PrenatalFragment extends Fragment {

    private int maternalRecordId = -1;
    private int profileId = -1;
    private String patientName = "";

    private TextView tvPrenatalPatientHeader;
    private Button btnPrenatal8AncBp, btnPrenatalImmunization, btnPrenatalSupplementation, btnPrenatalLabScreening;

    public PrenatalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            maternalRecordId = getArguments().getInt("maternal_record_id", -1);
            profileId = getArguments().getInt("profile_id", -1);
            patientName = getArguments().getString("patient_name", "Unknown Patient");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prenatal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind layout views
        tvPrenatalPatientHeader = view.findViewById(R.id.tvPrenatalPatientHeader);
        if (patientName != null && !patientName.isEmpty()) {
            tvPrenatalPatientHeader.setText("Patient: " + patientName);
        }

        btnPrenatal8AncBp = view.findViewById(R.id.btnPrenatal8AncBp);
        btnPrenatalImmunization = view.findViewById(R.id.btnPrenatalImmunization);
        btnPrenatalSupplementation = view.findViewById(R.id.btnPrenatalSupplementation);
        btnPrenatalLabScreening = view.findViewById(R.id.btnPrenatalLabScreening);

        // Setup Part 1 Listeners - Navigation Routing
        btnPrenatal8AncBp.setOnClickListener(v -> {
            navigateToSubModule(new Prenatal8AncFormFragment());
        });

        btnPrenatalImmunization.setOnClickListener(v -> {
            navigateToSubModule(new PrenatalImmunizationFragment());
        });

        // Setup Part 2 Listeners - Navigation Routing
        btnPrenatalSupplementation.setOnClickListener(v -> {
            navigateToSubModule(new PrenatalSupplementationFragment());
        });

        btnPrenatalLabScreening.setOnClickListener(v -> {
            navigateToSubModule(new PrenatalLabScreeningFragment());
        });
    }

    /**
     * Helper method to seamlessly pass maternal tracking references into subsequent sub-form submodules
     */
    private void navigateToSubModule(Fragment targetFragment) {
        Bundle args = new Bundle();
        args.putInt("maternal_record_id", maternalRecordId);
        args.putInt("profile_id", profileId);
        args.putString("patient_name", patientName);
        targetFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, targetFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}