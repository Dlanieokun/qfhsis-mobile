package com.android.hfsis.database.syncing;

/**
 * Response envelope returned by POST /api/sync/database.
 *
 * {
 *   "status":  "success",
 *   "message": "Database synchronized successfully on server backend."
 * }
 */
public class SyncPushResponse {

    /** "success" or "error" */
    public String status;

    /** Human-readable result/error message from the server. */
    public String message;
}