package org.twizer.android.io.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public abstract class PreferenceAssistant {

    public static final String PREF_LAST_TWEET_ID = "PREF_LAST_TWEET_ID";

    public static void writeSharedLong(final Context ctx, @NonNull final String settingName, final Long settingValue) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(settingName, settingValue);
        editor.apply();
    }

    public static Long readSharedLong(final Context ctx, @NonNull final String settingName, final Long defaultValue) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getLong(settingName, defaultValue);
    }
}
