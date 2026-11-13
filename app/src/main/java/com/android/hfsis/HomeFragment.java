package com.android.hfsis;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private Button btnEReferral, btnOtherServices, btnLogout;
    private AppUpdater appUpdater;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_API_URL = "api_base_url";
    private static final String DEFAULT_API_URL = "https://services.leyteprovince.gov.ph:8282/qfhsis/public/";

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

        // Check for a newer app version. If one exists, a dialog asks the user to update
        // (No = close the app, Yes = download + install). Otherwise nothing happens.
        appUpdater = new AppUpdater(this);
        appUpdater.checkThrottled(() -> { /* up to date or offline: nothing to do */ });

        // Initialize UI Elements
        btnEReferral = view.findViewById(R.id.btnEReferral);
        btnOtherServices = view.findViewById(R.id.btnOtherServices);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Show app version from build.gradle (versionName)
        TextView tvVersion = view.findViewById(R.id.tvVersion);
        tvVersion.setText("Version " + BuildConfig.VERSION_NAME);

        // Click listener for E-Referral
        btnEReferral.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Opening E-Referral...", Toast.LENGTH_SHORT).show());

        // Click listener for Other Services: verify account status first
        btnOtherServices.setOnClickListener(v -> checkAccountStatusThenOpenOtherServices());

        // Click listener for Logout
        btnLogout.setOnClickListener(v -> handleLogout("Logged out successfully"));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (appUpdater != null) appUpdater.onResume();
    }

    @Override
    public void onDestroyView() {
        if (appUpdater != null) {
            appUpdater.destroy();
            appUpdater = null;
        }
        super.onDestroyView();
    }

    /**
     * Checks the user's account status on the server before opening Other Services.
     * - Inactive (or user not found) -> log out and redirect to LoginFragment
     * - Active                       -> open OtherServicesFragment
     * - Network failure              -> let the user through (change below if you prefer to block)
     */
    private void checkAccountStatusThenOpenOtherServices() {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String baseUrl = prefs.getString(KEY_API_URL, DEFAULT_API_URL);

        if (userId == -1) {
            // No valid session stored
            handleLogout("Session expired. Please log in again.");
            return;
        }

        btnOtherServices.setEnabled(false);

        ApiService apiService = RetrofitClient.getClient(baseUrl).create(ApiService.class);
        apiService.checkUserStatus(userId).enqueue(new Callback<UserStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserStatusResponse> call,
                                   @NonNull Response<UserStatusResponse> response) {
                if (!isAdded()) return;
                btnOtherServices.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isInactive()) {
                        handleLogout("Your account is currently suspended or inactive.");
                    } else {
                        openOtherServices();
                    }
                } else if (response.code() == 404) {
                    // User no longer exists on the server
                    handleLogout("Your account could not be found. Please contact your administrator.");
                } else {
                    Toast.makeText(getActivity(),
                            "Unable to verify account status. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserStatusResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnOtherServices.setEnabled(true);

                // Offline: allow access. To block instead, replace the line below with:
                // Toast.makeText(getActivity(), "No connection. Unable to verify account.", Toast.LENGTH_SHORT).show();
                openOtherServices();
            }
        });
    }

    private void openOtherServices() {
        if (getActivity() == null) return;

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OtherServicesFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Clears user session data out of SharedPreferences and routes back to LoginFragment.
     * Does not use clear(), so the saved api_base_url is preserved.
     */
    private void handleLogout(String message) {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Remove every session key saved by LoginFragment
        editor.remove("auth_bearer_token");
        editor.remove("user_id");
        editor.remove("user_name");
        editor.remove("user_username");
        editor.remove("user_email");
        editor.remove("user_role");
        editor.remove("user_status");
        editor.remove("user_assigned_facility");
        editor.remove("user_barangay");
        editor.remove("user_barangay_codes");
        editor.remove("user_municipality");
        editor.remove("user_province");
        editor.remove("user_region");
        editor.remove("user_municipality_code");
        editor.remove("user_province_code");
        editor.remove("user_region_code");

        editor.apply();

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

        // Clear the back stack, then show the login screen
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }
}