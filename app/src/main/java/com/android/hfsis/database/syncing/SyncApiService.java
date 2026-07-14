package com.android.hfsis.database.syncing;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit API interface for the Laravel sync endpoints.
 *
 * PULL: GET  /api/sync/pull?last_synced_at=...  (server → Android)
 * PUSH: POST /api/sync/database                  (Android → server)
 */
public interface SyncApiService {

    /**
     * Fetch all (or updated) records from the Laravel server.
     *
     * Pass lastSyncedAt (ISO-8601 string saved from the previous response's
     * "synced_at" field) for a delta / incremental pull.
     * Pass null to perform a full pull on first install.
     *
     * Example: GET /api/sync/pull?last_synced_at=2026-06-20T10:00:00.000000Z
     *
     * This endpoint now sits behind auth:sanctum on the server (so it can
     * scope results to the logged-in user's assigned location), so a
     * bearer token — formatted as "Bearer <token>" — must be supplied.
     */
    @GET("api/sync/pull")
    Call<SyncPullResponse> pullFromServer(
            @Header("Authorization") String bearerToken,
            @Query("last_synced_at") String lastSyncedAt
    );

    /**
     * Push all local records up to the Laravel server.
     */
    @POST("api/sync/database")
    Call<SyncPushResponse> pushToServer(
            @Body SyncPayload payload
    );

    @POST("api/upload")
    Call<SyncPushResponse> uploadToServer(@Body SyncPayload payload);
}