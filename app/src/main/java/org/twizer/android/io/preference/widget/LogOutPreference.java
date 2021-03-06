package org.twizer.android.io.preference.widget;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;

import com.afollestad.materialdialogs.MaterialDialog;
import com.twitter.sdk.android.Twitter;

import org.twizer.android.R;
import org.twizer.android.io.preference.PreferenceAssistant;
import org.twizer.android.ui.activity.LoginActivity;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public class LogOutPreference extends MaterialDialogPreference {

    private Activity mProvidedActivity;

    public LogOutPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        super.onClick();
        showConfirmLogOutDialog(getContext());
    }

    private void showConfirmLogOutDialog(final Context context) {
        new MaterialDialog.Builder(context)
                .content(R.string.confirm_log_out)
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.pref_title_log_out)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final MaterialDialog dialog) {
                        logOut(mProvidedActivity);
                    }
                })
                .autoDismiss(Boolean.TRUE)
                .show();
    }

    public void setProvidedActivity(final Activity activity) {
        mProvidedActivity = activity;
    }

    private void logOut(final Context context) {
        Twitter.getSessionManager().clearActiveSession();
        cleanSessionPreferences(context);
        Twitter.logOut();
        final Intent intent = new Intent(context.getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityCompat.finishAfterTransition(mProvidedActivity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            //noinspection unchecked
            getContext().startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(mProvidedActivity).toBundle());
        else
            getContext().startActivity(intent);
    }

    private void cleanSessionPreferences(final Context context) {
        PreferenceAssistant.writeSharedString(context, context.getString(R.string.pref_key_max_tweet_id), context.getString(R.string.default_max_tweet_id));
        PreferenceAssistant.writeSharedString(context, context.getString(R.string.pref_key_last_search_text), context.getString(R.string.default_last_search_text));
        PreferenceAssistant.writeSharedString(context, context.getString(R.string.pref_key_last_tweet_id), context.getString(R.string.default_tweet_id));
    }
}
