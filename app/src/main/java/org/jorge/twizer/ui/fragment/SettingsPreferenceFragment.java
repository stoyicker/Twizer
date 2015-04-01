package org.jorge.twizer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.jorge.twizer.R;

/**
 * @author stoyicker.
 */
public class SettingsPreferenceFragment extends PreferenceFragment {

    private Context mContext;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                (mContext);
    }
}
