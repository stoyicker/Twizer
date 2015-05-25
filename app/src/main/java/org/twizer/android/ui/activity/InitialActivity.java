package org.twizer.android.ui.activity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.twitter.sdk.android.Twitter;

import org.twizer.android.R;
import org.twizer.android.io.files.FileOperations;
import org.twizer.android.io.prefs.PreferenceAssistant;

import java.io.File;

/**
 * @author stoyicker.
 */
public final class InitialActivity extends DescribedActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context appContext = getApplicationContext();

        flushCacheIfNecessary(appContext);
        setInitialTweet(appContext);
        start(appContext);
    }

    private void setInitialTweet(final Context context) {
        if (PreferenceAssistant.readSharedLong(context, PreferenceAssistant.PREF_LAST_TWEET_ID, -1L) == -1L) {
            PreferenceAssistant.writeSharedLong(context, PreferenceAssistant.PREF_LAST_TWEET_ID, Long.parseLong(context.getResources().getString(R.string.inital_tweet_id)));
        }
    }

    private Boolean isUserLoggedIn() {
        return Twitter.getSessionManager().getActiveSession() != null;
    }

    private void flushCacheIfNecessary(final Context context) {
        final File cacheDir;
        final Integer CACHE_SIZE_LIMIT_BYTES = context.getResources().getInteger(R.integer
                .max_cache_size_bytes);
        if ((cacheDir = context.getCacheDir()).length() > CACHE_SIZE_LIMIT_BYTES) {
            if (!FileOperations.recursiveDelete(cacheDir))
                throw new RuntimeException("Cache was full but could not be cleaned.");
        }
    }

    private void start(final Context context) {
        Intent intent = new Intent(context, isUserLoggedIn() ? MainActivity.class : LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityCompat.finishAfterTransition(this);
        //noinspection unchecked
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }
}
