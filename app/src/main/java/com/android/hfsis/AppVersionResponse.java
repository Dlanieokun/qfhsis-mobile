package com.android.hfsis;

import com.google.gson.annotations.SerializedName;

/**
 * Maps the JSON returned by GET /api/app-version/latest?current=x.y.z
 *
 * { "data": { "version_name": "1.0.4", "download_url": "...", "file_size": 123,
 *             "checksum": "sha256...", "uploaded_at": "...",
 *             "current_version": "1.0.3", "update_available": true } }
 */
public class AppVersionResponse {

    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        @SerializedName("version_name")
        private String versionName;

        @SerializedName("download_url")
        private String downloadUrl;

        @SerializedName("file_size")
        private long fileSize;

        @SerializedName("checksum")
        private String checksum;

        @SerializedName("update_available")
        private Boolean updateAvailable;

        public String getVersionName() { return versionName; }
        public String getDownloadUrl() { return downloadUrl; }
        public long getFileSize() { return fileSize; }
        public String getChecksum() { return checksum; }
        public boolean isUpdateAvailable() { return Boolean.TRUE.equals(updateAvailable); }
    }
}
