package com.android.hfsis;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/mobile/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);
}