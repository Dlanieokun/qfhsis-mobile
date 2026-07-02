package com.android.hfsis;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private Button btnEReferral, btnOtherServices, btnLogout;
    private static final String PREFS_NAME = "AppPrefs";

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI Elements
        btnEReferral = view.findViewById(R.id.btnEReferral);
        btnOtherServices = view.findViewById(R.id.btnOtherServices);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Click listener for E-Referral
        btnEReferral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Opening E-Referral...", Toast.LENGTH_SHORT).show();
            }
        });

        // Click listener for Other Services
        btnOtherServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new OtherServicesFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        // Click listener for Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogout();
            }
        });
    }

    /**
     * Clears user session metrics out of SharedPreferences and routes back to LoginFragment
     */
    private void handleLogout() {
        if (getActivity() == null) return;

        // Open SharedPreferences and completely clear authentication properties
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Remove individual session identity keys
        editor.remove("auth_bearer_token");
        editor.remove("user_id");
        editor.remove("user_name");
        editor.remove("user_email");
        editor.remove("user_role");
        editor.remove("user_assigned_facility");
        editor.remove("user_barangay");
        editor.remove("user_municipality");
        editor.remove("user_province");

        // Apply changes
        editor.apply();

        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect back to LoginFragment and clear out the view container layout cleanly
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }
}