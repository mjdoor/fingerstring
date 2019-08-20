package com.example.mdooreleyers.mdooreleyersproject1;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtility {
    public static String getStringPreference(Context context, String key, String defaultValue)
    {
        SharedPreferences preferences = context.getSharedPreferences("fingerstring", Context.MODE_PRIVATE);
        return preferences.getString(key, defaultValue);
    }

    public static int getIntPreference(Context context, String key, int defaultValue)
    {
        SharedPreferences preferences = context.getSharedPreferences("fingerstring", Context.MODE_PRIVATE);
        return preferences.getInt(key, defaultValue);
    }

    public static boolean getBooleanPreference(Context context, String key, boolean defaultValue)
    {
        SharedPreferences preferences = context.getSharedPreferences("fingerstring", Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defaultValue);
    }
}
