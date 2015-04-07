package org.jorge.twizer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;

import com.twitter.sdk.android.Twitter;

import org.jorge.twizer.R;
import org.jorge.twizer.ui.activity.MainActivity;

import java.util.List;
import java.util.Locale;

/**
 * @author stoyicker.
 */
public class SettingsPreferenceFragment extends PreferenceFragment {

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        final Context context = getActivity().getApplicationContext();

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


        findPreference(context.getString(R.string.pref_key_log_out)).setOnPreferenceClickListener(preference -> {
            if (Boolean.TRUE) { //TODO Show a confirmation materialdialog
                logOutAndRelaunch(context);
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        });

    }

    private void logOutAndRelaunch(final Context context) {
        Twitter.logOut();
        final Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityCompat.finishAfterTransition(getActivity());
        startActivity(intent);
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
