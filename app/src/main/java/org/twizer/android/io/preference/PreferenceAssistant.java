package org.twizer.android.io.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public abstract class PreferenceAssistant {

    public static void writeSharedInteger(final Context ctx, @NonNull final String settingName, final Integer settingValue) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(settingName, settingValue);
        editor.apply();
    }

    public static void writeSharedString(final Context ctx, @NonNull final String settingName, final String settingValue) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static Integer readSharedInteger(final Context ctx, @NonNull final String settingName, final Integer defaultValue) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getInt(settingName, defaultValue);
    }

    public static String readSharedString(final Context ctx, @NonNull final String settingName, final String defaultValue) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getString(settingName, defaultValue);
    }

    public static Boolean readSharedBoolean(final Context ctx, @NonNull final String settingName,
                                            final Boolean defaultValue) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(settingName, defaultValue);
    }
}
