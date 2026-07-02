package com.android.hfsis;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String token_type;

    @SerializedName("user")
    private UserData user;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getAccessToken() { return accessToken; }
    public UserData getUser() { return user; }

    public static class UserData {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("role")
        private String role;

        @SerializedName("assigned_facility")
        private String assignedFacility;

        @SerializedName("barangay")
        private String barangay;

        @SerializedName("municipality")
        private String municipality;

        @SerializedName("province")
        private String province;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getAssignedFacility() { return assignedFacility; }
        public String getBarangay() { return barangay; }
        public String getMunicipality() { return municipality; }
        public String getProvince() { return province; }
    }
}