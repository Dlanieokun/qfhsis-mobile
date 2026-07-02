package com.android.hfsis.child;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.hfsis.R;

public class ChildCareServicesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ChildCareServicesFragment() {
        // Required empty public constructor
    }

    public static ChildCareServicesFragment newInstance(String param1, String param2) {
        ChildCareServicesFragment fragment = new ChildCareServicesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_child_care_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        Button btnChildImmunization = view.findViewById(R.id.btnChildImmunization);
        Button btnChildImmunizationSchool = view.findViewById(R.id.btnChildImmunizationSchool);
        Button btnChildNutrition = view.findViewById(R.id.btnChildNutrition);
        Button btnManagementOfSick = view.findViewById(R.id.btnManagementOfSick);

        // Set Click Listeners
        btnChildImmunization.setOnClickListener(v -> {

            Fragment targetFragment = new ViewChildImmunizationRecordsFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();


            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnChildImmunizationSchool.setOnClickListener(v -> {

            Fragment targetFragment = new ViewChildImmunizationSchoolFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();


            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        btnChildNutrition.setOnClickListener(v -> {

            Fragment targetFragment = new ViewChildNutritionRecordsFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();


            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        btnManagementOfSick.setOnClickListener(v -> {

            Fragment targetFragment = new ViewChildManagementOfSickFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();


            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    private void handleButtonClick(String serviceName) {
        Toast.makeText(getContext(), "Selected: " + serviceName, Toast.LENGTH_SHORT).show();
    }
}