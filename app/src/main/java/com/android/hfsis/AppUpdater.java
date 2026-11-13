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
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Checks for a newer app version, asks the user, then downloads and installs it.
 *
 * Usage inside a Fragment:
 *   onViewCreated -> updater = new AppUpdater(this); updater.checkThrottled(() -> { ... });
 *   onResume      -> updater.onResume();
 *   onDestroyView -> updater.destroy();
 */
public class AppUpdater {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_API_URL = "api_base_url";
    private static final String DEFAULT_API_URL = "https://services.leyteprovince.gov.ph:8282/qfhsis/public/";
    private static final String UPDATE_APK_NAME = "qfhsis-update.apk";
    private static final long THROTTLE_MS = 5 * 60 * 1000L;

    /** Time of the last successful "up to date" answer from the server (process-wide). */
    private static long lastCheckedAt = 0;

    private final Fragment fragment;
    private AlertDialog updateDialog;
    private AlertDialog downloadDialog;
    private BroadcastReceiver downloadReceiver;
    private long downloadId = -1;
    private File pendingApk;
    private AppVersionResponse.Data latestVersion;

    public AppUpdater(@NonNull Fragment fragment) {
        this.fragment = fragment;
    }

    /** Like {@link #check}, but skips the request if the app was verified up to date in the last 5 minutes. */
    public void checkThrottled(@NonNull Runnable onContinue) {
        if (System.currentTimeMillis() - lastCheckedAt < THROTTLE_MS) {
            onContinue.run();
            return;
        }
        check(onContinue);
    }

    /**
     * Runs {@code onContinue} when the app is up to date or the check could not be done
     * (offline etc.). When an update exists, shows the dialog instead.
     */
    public void check(@NonNull Runnable onContinue) {
        if (fragment.getActivity() == null) { onContinue.run(); return; }

        SharedPreferences prefs = fragment.requireActivity()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String baseUrl = prefs.getString(KEY_API_URL, DEFAULT_API_URL);

        ApiService api = RetrofitClient.getClient(baseUrl).create(ApiService.class);
        api.getLatestAppVersion(BuildConfig.VERSION_NAME).enqueue(new Callback<AppVersionResponse>() {
            @Override
            public void onResponse(@NonNull Call<AppVersionResponse> call,
                                   @NonNull Response<AppVersionResponse> response) {
                if (!fragment.isAdded()) return;

                AppVersionResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null
                        && body.getData().isUpdateAvailable()
                        && !TextUtils.isEmpty(body.getData().getDownloadUrl())) {
                    latestVersion = body.getData();
                    showUpdateDialog(latestVersion);
                } else {
                    if (response.isSuccessful()) lastCheckedAt = System.currentTimeMillis();
                    onContinue.run();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AppVersionResponse> call, @NonNull Throwable t) {
                if (!fragment.isAdded()) return;
                onContinue.run(); // offline: don't lock the user out
            }
        });
    }

    /** Call from the fragment's onResume (handles return from the "install unknown apps" screen). */
    public void onResume() {
        if (pendingApk == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !fragment.isAdded()) return;

        File apk = pendingApk;
        pendingApk = null;
        if (fragment.requireContext().getPackageManager().canRequestPackageInstalls()) {
            installApk(apk);
        } else if (latestVersion != null) {
            Toast.makeText(fragment.getActivity(), "Permission is required to install the update.",
                    Toast.LENGTH_LONG).show();
            showUpdateDialog(latestVersion);
        }
    }

    /** Call from the fragment's onDestroyView. */
    public void destroy() {
        unregisterDownloadReceiver();
        dismissQuietly(updateDialog);
        dismissQuietly(downloadDialog);
    }

    // ------------------------------------------------------------------

    private void showUpdateDialog(@NonNull AppVersionResponse.Data version) {
        if (fragment.getActivity() == null || !fragment.isAdded()) return;
        dismissQuietly(updateDialog);

        updateDialog = new AlertDialog.Builder(fragment.requireActivity())
                .setTitle("Update available")
                .setMessage("A new version (" + version.getVersionName() + ") is available. "
                        + "You are using version " + BuildConfig.VERSION_NAME + ".\n\n"
                        + "Do you want to update the application now?")
                .setCancelable(false)
                .setPositiveButton("Yes", (d, w) -> startDownload(version))
                .setNegativeButton("No", (d, w) -> {
                    if (fragment.getActivity() != null) fragment.getActivity().finishAffinity();
                })
                .show();
    }

    private void startDownload(@NonNull AppVersionResponse.Data version) {
        if (fragment.getActivity() == null || !fragment.isAdded()) return;
        Context ctx = fragment.requireContext();

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

        downloadDialog = new AlertDialog.Builder(fragment.requireActivity())
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

                new Thread(() -> {
                    boolean valid = checksumMatches(apk, version.getChecksum());
                    if (fragment.getActivity() == null) return;
                    fragment.getActivity().runOnUiThread(() -> {
                        if (!fragment.isAdded()) return;
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

        ContextCompat.registerReceiver(fragment.requireContext(), downloadReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED);
    }

    private void unregisterDownloadReceiver() {
        if (downloadReceiver != null && fragment.getContext() != null) {
            try {
                fragment.requireContext().unregisterReceiver(downloadReceiver);
            } catch (IllegalArgumentException ignored) { }
        }
        downloadReceiver = null;
    }

    private void onUpdateFailed(String message) {
        if (!fragment.isAdded()) return;
        dismissQuietly(downloadDialog);
        Toast.makeText(fragment.getActivity(), message, Toast.LENGTH_LONG).show();
        if (latestVersion != null) showUpdateDialog(latestVersion);
    }

    private void installApk(@NonNull File apk) {
        Context ctx = fragment.requireContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !ctx.getPackageManager().canRequestPackageInstalls()) {
            pendingApk = apk;
            Toast.makeText(ctx, "Please allow installing apps from this app, then come back.",
                    Toast.LENGTH_LONG).show();
            fragment.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + ctx.getPackageName())));
            return;
        }

        Uri uri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", apk);
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        fragment.startActivity(intent);
    }

    private static void dismissQuietly(@Nullable AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            try { dialog.dismiss(); } catch (Exception ignored) { }
        }
    }

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
}
