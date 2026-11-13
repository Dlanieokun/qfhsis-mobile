package com.android.hfsis;

import com.google.gson.annotations.SerializedName;

public class UserStatusResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("user_status")
    private String userStatus;

    @SerializedName("is_inactive")
    private boolean isInactive;

    @SerializedName("message")
    private String message;

    public String getStatus() { return status; }

    public int getUserId() { return userId; }

    public String getUserStatus() { return userStatus; }

    public boolean isInactive() { return isInactive; }

    public String getMessage() { return message; }
}
