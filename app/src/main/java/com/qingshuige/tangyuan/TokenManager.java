package com.qingshuige.tangyuan;

import android.content.SharedPreferences;

public class TokenManager {
    private SharedPreferences prefs;

    public TokenManager() {
        prefs = TangyuanApplication.getSharedPreferences();
    }

    public String getToken() {
        return prefs.getString("JwtToken", null);
    }

    public String getPhoneNumber() {
        return prefs.getString("phoneNumber", null);
    }

    public String getPassword() {
        return prefs.getString("password", null);
    }

    public void setToken(String token) {
        prefs.edit().putString("JwtToken", token).apply();
    }

    public void setPhoneNumberAndPassword(String phoneNumber, String password) {
        prefs.edit()
                .putString("phoneNumber", phoneNumber)
                .putString("password", password)
                .apply();
    }
}
