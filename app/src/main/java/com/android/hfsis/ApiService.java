package com.android.hfsis;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/mobile/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @GET("api/users/{id}/status")
    Call<UserStatusResponse> checkUserStatus(@Path("id") int userId);

    // Public endpoint: returns the latest uploaded APK info.
    // "current" = installed version name, so the server can set update_available.
    @GET("api/app-version/latest")
    Call<AppVersionResponse> getLatestAppVersion(@Query("current") String current);

}