package com.android.hfsis.network;

import com.android.hfsis.database.syncing.SyncPayload;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SyncApiService {
    @POST("api/sync/database")
    Call<ResponseBody> uploadDatabasePayload(@Body SyncPayload payload);
}