package com.everon.recipeapp.data;

import android.app.Application;
import android.text.TextUtils;

/**
 * Wrapper for user.
 */
public class UserWrapper extends Application {
    private String username;
    private String userId;
    private static UserWrapper instance;

    public static UserWrapper getInstance() {
        if(instance == null) instance = new UserWrapper();
        return instance;
    }

    public boolean checkIfEmpty() {
        return TextUtils.isEmpty(userId) || TextUtils.isEmpty(username);
    }

    public UserWrapper() {}

    public String getUsername() { return username;}

    public void setUsername(String username) { this.username = username;}

    public String getUserId() { return userId;}

    public void setUserId(String userId) {this.userId = userId;}

}
