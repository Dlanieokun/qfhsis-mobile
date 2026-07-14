package com.android.hfsis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink, tvSetApi;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_API_URL = "api_base_url";
    private static final String DEFAULT_API_URL = "http://192.168.10.91:3030/";

    public LoginFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Check if user is already logged in before initializing UI components
        checkExistingSession();

        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegisterLink = view.findViewById(R.id.tvRegisterLink);
        tvSetApi = view.findViewById(R.id.tvSetApi);

        tvSetApi.setOnClickListener(v -> showApiUrlDialog());

        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            handleLogin(email, password);
        });
    }

    /**
     * Checks if a user profile and valid token exist in SharedPreferences.
     * If true, skips login and immediately navigates to HomeFragment.
     */
    private void checkExistingSession() {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Check if an auth token and user ID exist (indicating a complete authenticated profile)
        String token = prefs.getString("auth_bearer_token", null);
        int userId = prefs.getInt("user_id", -1);

        if (token != null && userId != -1) {
            // Optional: Show a quick toast greeting the returning user
            String name = prefs.getString("user_name", "User");
            Toast.makeText(getActivity(), "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();

            // Redirect immediately to HomeFragment without showing the login interface
            navigateToHome();
        }
    }

    /**
     * Helper method to handle fragment redirection to HomeFragment
     */
    private void navigateToHome() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void showApiUrlDialog() {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentUrl = prefs.getString(KEY_API_URL, DEFAULT_API_URL);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Configure API Base URL");

        final EditText input = new EditText(getActivity());
        input.setText(currentUrl);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newUrl = input.getText().toString().trim();
            if (!newUrl.isEmpty()) {
                if (!newUrl.endsWith("/")) {
                    newUrl += "/";
                }
                prefs.edit().putString(KEY_API_URL, newUrl).apply();
                Toast.makeText(getActivity(), "API URL updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void handleLogin(String email, String password) {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String baseUrl = prefs.getString(KEY_API_URL, DEFAULT_API_URL);

        btnLogin.setEnabled(false);
        btnLogin.setText("Authenticating...");

        ApiService apiService = RetrofitClient.getClient(baseUrl).create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if ("success".equals(loginResponse.getStatus())) {
                        // Open SharedPreferences editor
                        SharedPreferences.Editor editor = prefs.edit();

                        // Save authentication session token
                        editor.putString("auth_bearer_token", loginResponse.getAccessToken());

                        // Extract and automatically save all user meta parameters
                        LoginResponse.UserData user = loginResponse.getUser();
                        editor.putInt("user_id", user.getId());
                        editor.putString("user_name", user.getName());
                        editor.putString("user_email", user.getEmail());
                        editor.putString("user_role", user.getRole());
                        editor.putString("user_status", user.getStatus());
                        editor.putString("user_assigned_facility", user.getAssignedFacility());

                        // barangay / barangay_codes arrive as JSON arrays; join for storage
                        editor.putString("user_barangay",
                                user.getBarangay() != null ? TextUtils.join(", ", user.getBarangay()) : "");
                        editor.putString("user_barangay_codes",
                                user.getBarangayCodes() != null ? TextUtils.join(", ", user.getBarangayCodes()) : "");

                        editor.putString("user_municipality", user.getMunicipality());
                        editor.putString("user_province", user.getProvince());
                        editor.putString("user_region", user.getRegion());
                        editor.putString("user_municipality_code", user.getMunicipalityCode());
                        editor.putString("user_province_code", user.getProvinceCode());
                        editor.putString("user_region_code", user.getRegionCode());

                        // Apply updates asynchronously
                        editor.apply();

                        Toast.makeText(getActivity(), "Welcome " + user.getName() + "!", Toast.LENGTH_SHORT).show();

                        // Automatically switch to the Home screen fragment dashboard layout right away
                        navigateToHome();
                    } else {
                        Toast.makeText(getActivity(), loginResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = "Invalid authentication metrics.";
                    if (response.code() == 401) {
                        errorMsg = "Incorrect email address or security password.";
                    } else if (response.code() == 403) {
                        errorMsg = "Your account is suspended or inactive.";
                    }
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                Toast.makeText(getActivity(), "Network Connection Failed: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}