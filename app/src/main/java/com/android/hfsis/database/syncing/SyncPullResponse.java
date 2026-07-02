package com.android.hfsis.database.syncing;

import com.google.gson.annotations.SerializedName;

/**
 * Top-level JSON envelope returned by GET /api/sync/pull.
 *
 * {
 *   "status":    "success",
 *   "synced_at": "2026-06-22T10:00:00.000000Z",
 *   "data":      { ... }
 * }
 */
public class SyncPullResponse {

    /** "success" or "error" */
    public String status;

    /**
     * ISO-8601 timestamp of this pull.
     * Save this in SharedPreferences and send it as "last_synced_at"
     * on the next pull so only changed rows are returned (delta sync).
     */
    @SerializedName("synced_at")
    public String syncedAt;

    /** The actual payload containing all entity lists. */
    public SyncPullPayload data;
}