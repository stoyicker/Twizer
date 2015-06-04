package org.twizer.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.twizer.android.R;
import org.twizer.android.io.preference.PreferenceAssistant;
import org.twizer.android.io.preference.widget.DistanceDiscreteSliderPreference;
import org.twizer.android.io.preference.widget.LogOutPreference;
import org.twizer.android.io.preference.widget.MaterialDialogPreference;

import java.util.List;
import java.util.Locale;

/**
 * @author stoyicker.
 */
public final class SettingsPreferenceFragment extends PreferenceFragment {

    private MaterialDialogPreference mDistanceRadiusPreference, mLogOutPreference;
    private Preference mDistanceUnitPreference;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        final Activity activity = getActivity();
        final Context context = activity.getApplicationContext();

        mDistanceRadiusPreference = (MaterialDialogPreference) findPreference(context.getString(R.string
                .pref_key_search_radius));
        mDistanceUnitPreference = findPreference(context.getString(R.string
                .pref_key_search_distance_unit));

        mDistanceUnitPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            ((DistanceDiscreteSliderPreference) mDistanceRadiusPreference).updateIndicatorAndSummary(context, Integer.parseInt((String) newValue));

            return Boolean.TRUE;
        });

        findPreference(context.getString(R.string.pref_key_search_type_nearby)).setOnPreferenceChangeListener((preference, newValue) -> {
            updateSearchPreferencesEnabledStatus((Boolean) newValue);

            return Boolean.TRUE;
        });

        findPreference(context.getString(R.string.pref_key_about_the_author))
                .setOnPreferenceClickListener(preference -> {
                    showMyLinkedInProfile(context);
                    return Boolean.TRUE;
                });

        findPreference(context.getString(R.string.pref_key_see_the_source))
                .setOnPreferenceClickListener(preference -> {
                    showGitHubRepository(context);
                    return Boolean.TRUE;
                });


        mLogOutPreference = (MaterialDialogPreference) findPreference(context.getString(R.string.pref_key_log_out));
        ((LogOutPreference) mLogOutPreference).setProvidedActivity(getActivity());

        updateSearchPreferencesEnabledStatus(PreferenceAssistant.readSharedBoolean(context, context.getString(R.string.pref_key_search_type_nearby), context.getResources().getBoolean(R.bool.pref_default_search_type_nearby)));

    }

    private void updateSearchPreferencesEnabledStatus(final Boolean isNearbyOnlyModeEnabled) {
        mDistanceRadiusPreference.setEnabled(isNearbyOnlyModeEnabled);
        mDistanceUnitPreference.setEnabled(isNearbyOnlyModeEnabled);
    }

    @Override
    public void onPause() {
        mDistanceRadiusPreference.dismissDialog();
        mLogOutPreference.dismissDialog();
        super.onPause();
    }

    private void showGitHubRepository(final Context context) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R
                .string.github_repository_url)));
        startActivity(intent);
    }

    private void showMyLinkedInProfile(final Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.ENGLISH,
                context.getString(R.string.linkedin_intent_pattern), context.getString(R.string
                        .author_linkedin_id))));
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (list.isEmpty()) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string
                    .about_the_author_link)));
        }
        startActivity(intent);
    }
}
