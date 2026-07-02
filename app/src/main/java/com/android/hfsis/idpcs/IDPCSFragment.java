package com.android.hfsis.idpcs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.hfsis.R;
import com.android.hfsis.idpcs.filariasis.FilariasisRegistryFragment;
import com.android.hfsis.idpcs.filariasis.ViewFilariasisRegistryFragment;
import com.android.hfsis.idpcs.leprosy.LeprosyRegistryFragment;
import com.android.hfsis.idpcs.leprosy.ViewLeprosyRegistryFragment;
import com.android.hfsis.idpcs.rabies.RabiesFragment;
import com.android.hfsis.idpcs.rabies.ViewRabiesFragment;
import com.android.hfsis.idpcs.schistosomiasis.SchistosomiasisRegistryFragment;
import com.android.hfsis.idpcs.schistosomiasis.ViewSchistosomiasisRegistryFragment;
import com.android.hfsis.idpcs.sthpc.SoilTransmittedHelminthiasisRegistryFragment;
import com.android.hfsis.idpcs.sthpc.ViewSoilTransmittedHelminthiasisRegistryFragment;
import com.android.hfsis.ncdpcs.mental.MentalHealthFragment;

public class IDPCSFragment extends Fragment implements View.OnClickListener {

    private Button btnFilariasis, btnLeprosy, btnRabies, btnSchistosomiasis, btnSthpc;

    public IDPCSFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_idpcs, container, false);

        // Initialize UI Elements
        btnFilariasis = view.findViewById(R.id.btn_filariasis);
        btnLeprosy = view.findViewById(R.id.btn_leprosy);
        btnRabies = view.findViewById(R.id.btn_rabies);
        btnSchistosomiasis = view.findViewById(R.id.btn_schistosomiasis);
        btnSthpc = view.findViewById(R.id.btn_sthpc);

        // Set Click Listeners
        btnFilariasis.setOnClickListener(v -> {
            Fragment targetFragment = new ViewFilariasisRegistryFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        btnLeprosy.setOnClickListener(v -> {
            Fragment targetFragment = new ViewLeprosyRegistryFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        btnRabies.setOnClickListener(v -> {
            Fragment targetFragment = new ViewRabiesFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        btnSchistosomiasis.setOnClickListener(v -> {
            Fragment targetFragment = new ViewSchistosomiasisRegistryFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        btnSthpc.setOnClickListener(v -> {
            Fragment targetFragment = new ViewSoilTransmittedHelminthiasisRegistryFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        // Handle clicks based on view ID
        int id = v.getId();

        if (id == R.id.btn_filariasis) {
            showToast("Filariasis selected");
            // Add your navigation or logic here
        } else if (id == R.id.btn_leprosy) {
            showToast("Leprosy selected");
            // Add your navigation or logic here
        } else if (id == R.id.btn_rabies) {
            showToast("Rabies selected");
            // Add your navigation or logic here
        } else if (id == R.id.btn_schistosomiasis) {
            showToast("Schistosomiasis selected");
            // Add your navigation or logic here
        } else if (id == R.id.btn_sthpc) {
            showToast("STHPC selected");
            // Add your navigation or logic here
        }
    }

    // Helper method to keep code clean
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}