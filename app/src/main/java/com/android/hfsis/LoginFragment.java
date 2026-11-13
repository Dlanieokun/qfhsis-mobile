package com.android.hfsis;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageButton btnTogglePassword;
    private boolean isPasswordVisible = false;
    private TextView tvRegisterLink, tvSetApi, tvForgotPassword;
    private NestedScrollView scrollView;
    private View loadingOverlay;
    private TextView tvVersion;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_API_URL = "api_base_url";
    private static final String DEFAULT_API_URL = "https://services.leyteprovince.gov.ph:8282/qfhsis/public/";
    private static final String UPDATE_APK_NAME = "qfhsis-update.apk";

    // ---- In-app update state ----
    private AlertDialog updateDialog;
    private AlertDialog downloadDialog;
    private BroadcastReceiver downloadReceiver;
    private long downloadId = -1;
    private File pendingApk;                         // waiting for "install unknown apps" permission
    private AppVersionResponse.Data latestVersion;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 0. Version check runs first. The rest of the screen only continues
        //    when the app is up to date (or the check could not be completed).
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        checkForUpdate(() -> {
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
            proceedWithLogin(view);
        });
    }

    /** The original onViewCreated body: session check + login UI setup. */
    private void proceedWithLogin(@NonNull View view) {
        // 1. Check if user is already logged in before initializing UI components
        if (checkExistingSession()) {
            return; // Already navigating to Home, no need to set up the login UI
        }

        scrollView = view.findViewById(R.id.scrollView);
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        btnTogglePassword = view.findViewById(R.id.btnTogglePassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegisterLink = view.findViewById(R.id.tvRegisterLink);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        tvSetApi = view.findViewById(R.id.tvSetApi);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        tvVersion = view.findViewById(R.id.tvVersion);
        tvVersion.setText("Version " + BuildConfig.VERSION_NAME);

        // Zoom effect and automatic scroll-up on focus
        View.OnFocusChangeListener zoomFocusListener = (v, hasFocus) -> {
            if (hasFocus) {
                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(200).start();

                scrollView.postDelayed(() -> {
                    if (isAdded() && scrollView != null) {
                        int[] viewLocation = new int[2];
                        v.getLocationOnScreen(viewLocation);

                        int[] scrollLocation = new int[2];
                        scrollView.getLocationOnScreen(scrollLocation);

                        int relativeY = viewLocation[1] - scrollLocation[1];
                        scrollView.smoothScrollBy(0, relativeY - 150);
                    }
                }, 250);
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        };

        etUsername.setOnFocusChangeListener(zoomFocusListener);
        etPassword.setOnFocusChangeListener(zoomFocusListener);

        startIntroAnimation(view);

        // Show / hide password
        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            int cursor = etPassword.getSelectionEnd();

            if (isPasswordVisible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_purple);
                btnTogglePassword.setContentDescription("Hide password");
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_off_purple);
                btnTogglePassword.setContentDescription("Show password");
            }

            // Keep the cursor where the user left it
            etPassword.setSelection(Math.max(cursor, 0));
        });

        tvSetApi.setOnClickListener(v -> showApiUrlDialog());
        tvRegisterLink.setOnClickListener(v -> openInBrowser("register"));
        tvForgotPassword.setOnClickListener(v -> openInBrowser("forgot-password"));

        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter your username (or email) and password", Toast.LENGTH_SHORT).show();
                return;
            }

            handleLogin(email, password);
        });
    }

    // =====================================================================
    //  APP UPDATE FLOW
    // =====================================================================

    /**
     * Asks the server for the latest version. If it differs from the installed one,
     * shows the update dialog; otherwise (or if the check fails, e.g. offline)
     * runs {@code onContinue} so the user can keep using the app.
     */
    private void checkForUpdate(@NonNull Runnable onContinue) {
        if (getActivity() == null) { onContinue.run(); return; }

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String baseUrl = prefs.getString(KEY_API_URL, DEFAULT_API_URL);

        ApiService apiService = RetrofitClient.getClient(baseUrl).create(ApiService.class);
        apiService.getLatestAppVersion(BuildConfig.VERSION_NAME).enqueue(new Callback<AppVersionResponse>() {
            @Override
            public void onResponse(@NonNull Call<AppVersionResponse> call,
                                   @NonNull Response<AppVersionResponse> response) {
                if (!isAdded()) return;

                AppVersionResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null
                        && body.getData().isUpdateAvailable()
                        && !TextUtils.isEmpty(body.getData().getDownloadUrl())) {
                    latestVersion = body.getData();
                    showUpdateDialog(latestVersion);
                } else {
                    // Up to date, or no version published yet (404)
                    onContinue.run();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AppVersionResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                // Don't lock out users who are offline / server unreachable
                onContinue.run();
            }
        });
    }

    private void showUpdateDialog(@NonNull AppVersionResponse.Data version) {
        if (getActivity() == null || !isAdded()) return;
        dismissQuietly(updateDialog);

        updateDialog = new AlertDialog.Builder(requireActivity())
                .setTitle("Update available")
                .setMessage("A new version (" + version.getVersionName() + ") is available. "
                        + "You are using version " + BuildConfig.VERSION_NAME + ".\n\n"
                        + "Do you want to update the application now?")
                .setCancelable(false)
                .setPositiveButton("Yes", (d, w) -> startDownload(version))
                .setNegativeButton("No", (d, w) -> {
                    // User declined: close the whole app
                    if (getActivity() != null) getActivity().finishAffinity();
                })
                .show();
    }

    private void startDownload(@NonNull AppVersionResponse.Data version) {
        if (getActivity() == null || !isAdded()) return;
        Context ctx = requireContext();

        // Remove any leftover APK from a previous attempt
        File apk = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), UPDATE_APK_NAME);
        if (apk.exists()) //noinspection ResultOfMethodCallIgnored
            apk.delete();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(version.getDownloadUrl()))
                .setTitle("Downloading update " + version.getVersionName())
                .setDescription("QFHSIS")
                .setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalFilesDir(ctx, Environment.DIRECTORY_DOWNLOADS, UPDATE_APK_NAME);

        DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadId = dm.enqueue(request);

        downloadDialog = new AlertDialog.Builder(requireActivity())
                .setTitle("Updating")
                .setMessage("Downloading the latest version. Please wait…")
                .setCancelable(false)
                .show();

        registerDownloadReceiver(apk, version);
    }

    private void registerDownloadReceiver(@NonNull File apk, @NonNull AppVersionResponse.Data version) {
        unregisterDownloadReceiver();

        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id != downloadId) return;

                unregisterDownloadReceiver();

                boolean ok = false;
                DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                try (Cursor c = dm.query(new DownloadManager.Query().setFilterById(id))) {
                    if (c != null && c.moveToFirst()) {
                        int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        ok = status == DownloadManager.STATUS_SUCCESSFUL;
                    }
                }

                if (!ok) {
                    onUpdateFailed("Download failed. Please check your connection and try again.");
                    return;
                }

                // Verify integrity off the main thread
                new Thread(() -> {
                    boolean valid = checksumMatches(apk, version.getChecksum());
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        dismissQuietly(downloadDialog);
                        if (valid) {
                            installApk(apk);
                        } else {
                            //noinspection ResultOfMethodCallIgnored
                            apk.delete();
                            onUpdateFailed("The downloaded file is corrupted. Please try again.");
                        }
                    });
                }).start();
            }
        };

        ContextCompat.registerReceiver(requireContext(), downloadReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED);
    }

    private void unregisterDownloadReceiver() {
        if (downloadReceiver != null && getContext() != null) {
            try {
                requireContext().unregisterReceiver(downloadReceiver);
            } catch (IllegalArgumentException ignored) { }
        }
        downloadReceiver = null;
    }

    private void onUpdateFailed(String message) {
        if (!isAdded()) return;
        dismissQuietly(downloadDialog);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        if (latestVersion != null) showUpdateDialog(latestVersion); // let the user retry
    }

    private void installApk(@NonNull File apk) {
        Context ctx = requireContext();

        // Android 8+: the user must allow this app to install packages.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !ctx.getPackageManager().canRequestPackageInstalls()) {
            pendingApk = apk;
            Toast.makeText(ctx, "Please allow installing apps from this app, then come back.",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + ctx.getPackageName())));
            return;
        }

        Uri uri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", apk);
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Returning from the "install unknown apps" settings screen
        if (pendingApk != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            File apk = pendingApk;
            pendingApk = null;
            if (requireContext().getPackageManager().canRequestPackageInstalls()) {
                installApk(apk);
            } else if (latestVersion != null) {
                Toast.makeText(getActivity(), "Permission is required to install the update.",
                        Toast.LENGTH_LONG).show();
                showUpdateDialog(latestVersion);
            }
        }
    }

    @Override
    public void onDestroyView() {
        unregisterDownloadReceiver();
        dismissQuietly(updateDialog);
        dismissQuietly(downloadDialog);
        super.onDestroyView();
    }

    private static void dismissQuietly(@Nullable AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            try { dialog.dismiss(); } catch (Exception ignored) { }
        }
    }

    /** Compares the file's SHA-256 with the server-provided checksum (skipped if none given). */
    private static boolean checksumMatches(File file, @Nullable String expected) {
        if (TextUtils.isEmpty(expected)) return true;
        try (InputStream in = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) md.update(buf, 0, n);

            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) sb.append(String.format("%02x", b));
            return sb.toString().equalsIgnoreCase(expected.trim());
        } catch (Exception e) {
            return false;
        }
    }

    // =====================================================================
    //  EXISTING LOGIN CODE (unchanged)
    // =====================================================================

    /**
     * Opens the given path appended to the saved API base URL in the device browser.
     * Example: path "register" -> https://.../qfhsis/public/register
     */
    private void openInBrowser(String path) {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String baseUrl = prefs.getString(KEY_API_URL, DEFAULT_API_URL);

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(baseUrl + path));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "No browser app found to open the link", Toast.LENGTH_SHORT).show();
        }
    }

    private void startIntroAnimation(View view) {
        View topBar = view.findViewById(R.id.llTopBar);
        View heroCard = view.findViewById(R.id.cvHero);
        View heroGlow = view.findViewById(R.id.viewHeroGlow);
        View loginCard = view.findViewById(R.id.cardLogin);

        if (topBar != null) {
            topBar.setAlpha(0f);
            topBar.setTranslationY(-30f);
        }

        if (heroCard != null && heroGlow != null) {
            heroCard.setAlpha(0f);
            heroCard.setScaleX(0.8f);
            heroCard.setScaleY(0.8f);
            heroGlow.setAlpha(0f);
        }

        if (loginCard != null) {
            loginCard.setAlpha(0f);
            loginCard.setTranslationY(80f);
        }

        if (topBar != null) {
            topBar.animate().alpha(1f).translationY(0f).setDuration(400).start();
        }

        if (heroCard != null && heroGlow != null) {
            heroGlow.animate().alpha(1f).setDuration(500).setStartDelay(150).start();
            heroCard.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).setStartDelay(200).start();
        }

        if (loginCard != null) {
            loginCard.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(350).start();
        }
    }

    private boolean checkExistingSession() {
        if (getActivity() == null) return false;

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String token = prefs.getString("auth_bearer_token", null);
        int userId = prefs.getInt("user_id", -1);

        if (token != null && userId != -1) {
            String name = prefs.getString("user_name", "User");
            Toast.makeText(getActivity(), "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
            navigateToHome();
            return true;
        }
        return false;
    }

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

        loadingOverlay.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient(baseUrl).create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login →");

                loadingOverlay.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if ("success".equals(loginResponse.getStatus())) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("auth_bearer_token", loginResponse.getAccessToken());

                        LoginResponse.UserData user = loginResponse.getUser();
                        editor.putInt("user_id", user.getId());
                        editor.putString("user_name", user.getName());
                        editor.putString("user_username", user.getUsername());
                        editor.putString("user_email", user.getEmail());
                        editor.putString("user_role", user.getRole());
                        editor.putString("user_status", user.getStatus());
                        editor.putString("user_assigned_facility", user.getAssignedFacility());

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

                        editor.apply();

                        Toast.makeText(getActivity(), "Welcome " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Toast.makeText(getActivity(), loginResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    
                } else {
                    String errorMsg = "Invalid authentication metrics.";
                    if (response.code() == 401) {
                        errorMsg = "Incorrect username/email or password.";
                    } else if (response.code() == 403) {
                        errorMsg = "Your account is suspended or inactive.";
                    }
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login →");

                loadingOverlay.setVisibility(View.GONE);

                Toast.makeText(getActivity(), "Network Connection Failed: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}