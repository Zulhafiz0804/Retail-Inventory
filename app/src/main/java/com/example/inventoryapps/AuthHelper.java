package com.example.inventoryapps;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthHelper {

    public static String getCurrentStaffId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String role = prefs.getString("user_role", null);
        if ("staff".equals(role)) {
            return prefs.getString("user_id", "unknown");
        }
        return "unknown";
    }

    public static String getCurrentManagerId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String role = prefs.getString("user_role", null);
        if ("manager".equals(role)) {
            return prefs.getString("user_id", "unknown");
        }
        return "unknown";
    }
}
