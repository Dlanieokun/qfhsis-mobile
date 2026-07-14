package com.android.hfsis;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("user")
    private UserData user;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public UserData getUser() {
        return user;
    }

    public static class UserData {

        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("role")
        private String role;

        @SerializedName("status")
        private String status;

        @SerializedName("assigned_facility")
        private String assignedFacility;

        // Backend sends this as a JSON array (may contain multiple barangays)
        @SerializedName("barangay")
        private List<String> barangay;

        @SerializedName("municipality")
        private String municipality;

        @SerializedName("province")
        private String province;

        @SerializedName("region")
        private String region;

        // Backend sends this as a JSON array, parallel to "barangay"
        @SerializedName("barangay_codes")
        private List<String> barangayCodes;

        @SerializedName("municipality_code")
        private String municipalityCode;

        @SerializedName("province_code")
        private String provinceCode;

        @SerializedName("region_code")
        private String regionCode;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public String getStatus() {
            return status;
        }

        public String getAssignedFacility() {
            return assignedFacility;
        }

        public List<String> getBarangay() {
            return barangay;
        }

        public String getMunicipality() {
            return municipality;
        }

        public String getProvince() {
            return province;
        }

        public String getRegion() {
            return region;
        }

        public List<String> getBarangayCodes() {
            return barangayCodes;
        }

        public String getMunicipalityCode() {
            return municipalityCode;
        }

        public String getProvinceCode() {
            return provinceCode;
        }

        public String getRegionCode() {
            return regionCode;
        }
    }
}