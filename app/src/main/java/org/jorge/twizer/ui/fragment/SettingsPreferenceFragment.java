package org.jorge.twizer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.jorge.twizer.R;

import java.util.List;
import java.util.Locale;

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

        findPreference(mContext.getString(R.string.pref_key_about_the_author))
                .setOnPreferenceClickListener(preference -> {
                    showMyLinkedInProfile();
                    return Boolean.TRUE;
                });

        findPreference(mContext.getString(R.string.pref_key_see_the_source))
                .setOnPreferenceClickListener(preference -> {
                    showGitHubRepository();
                    return Boolean.TRUE;
                });
    }

    private void showGitHubRepository() {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext.getString(R
                .string.github_repository_url)));
        startActivity(intent);
    }

    private void showMyLinkedInProfile() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.ENGLISH,
                mContext.getString(R.string.linkedin_intent_pattern), mContext.getString(R.string
                        .author_linkedin_id))));
        final PackageManager packageManager = mContext.getPackageManager();
        final List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (list.isEmpty()) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext.getString(R.string
                    .about_the_author_link)));
        }
        startActivity(intent);
    }
}
