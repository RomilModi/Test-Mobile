package com.example.rmodi.checkconstantlocation.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rmodi on 10/21/2016.
 */

public class UserAttendance {
    @SerializedName("user_id")
    public String userId;
    @SerializedName("user_activity")
    public String userActivity;
    @SerializedName("created_at")
    public String createdAt;

    /**
     * @return The userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId The user_id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return The userActivity
     */
    public String getUserActivity() {
        return userActivity;
    }

    /**
     * @param userActivity The user_activity
     */
    public void setUserActivity(String userActivity) {
        this.userActivity = userActivity;
    }

    /**
     * @return The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt The created_at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
