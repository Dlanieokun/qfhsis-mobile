package com.android.hfsis.ncdpcs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.hfsis.R;
import com.android.hfsis.child.ViewChildImmunizationRecordsFragment;
import com.android.hfsis.ncdpcs.cancer.CervicalCancerScreeningFragment;
import com.android.hfsis.ncdpcs.cancer.ViewCervicalCancerScreeningFragment;
import com.android.hfsis.ncdpcs.eyes.EyesScreeningFragment;
import com.android.hfsis.ncdpcs.eyes.ViewEyesScreeningFragment;
import com.android.hfsis.ncdpcs.mental.MentalHealthFragment;
import com.android.hfsis.ncdpcs.mental.ViewMentalHealthFragment;
import com.android.hfsis.ncdpcs.phil.PhilPENRiskAssessmentFragment;
import com.android.hfsis.ncdpcs.phil.ViewPhilPENRiskAssessmentFragment;
import com.google.android.material.card.MaterialCardView;

public class NCDPCSFragment extends Fragment {

    public NCDPCSFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ncdpcs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Binding the Row Cards
        MaterialCardView cardPhilpen = view.findViewById(R.id.cardPhilpen);
        MaterialCardView cardEyes = view.findViewById(R.id.cardEyes);
        MaterialCardView cardCancer = view.findViewById(R.id.cardCancer);
        MaterialCardView cardMentalHealth = view.findViewById(R.id.cardMentalHealth);

        // Click Event Bindings
        cardPhilpen.setOnClickListener(v -> {
            Fragment targetFragment = new ViewPhilPENRiskAssessmentFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        cardEyes.setOnClickListener(v -> {
            Fragment targetFragment = new ViewEyesScreeningFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        cardCancer.setOnClickListener(v -> {
            Fragment targetFragment = new ViewCervicalCancerScreeningFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        cardMentalHealth.setOnClickListener(v -> {
            Fragment targetFragment = new ViewMentalHealthFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}