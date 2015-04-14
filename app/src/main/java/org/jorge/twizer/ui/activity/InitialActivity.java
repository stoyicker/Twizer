package org.jorge.twizer.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.jorge.twizer.BuildConfig;
import org.jorge.twizer.R;
import org.jorge.twizer.io.files.FileOperations;

import java.io.File;

import io.fabric.sdk.android.Fabric;

/**
 * @author stoyicker.
 */
public class InitialActivity extends DescribedActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context appContext = getApplicationContext();

        initCrashlytics();
        flushCacheIfNecessary(appContext);
        start(appContext);
    }

    private void initCrashlytics() {
        final TwitterAuthConfig authConfig = new TwitterAuthConfig(BuildConfig.TWITTER_KEY, BuildConfig.TWITTER_SECRET);
        Fabric fabricKits;
        if (BuildConfig.USE_CRASHLYTICS) {
            fabricKits = new Fabric.Builder(this).kits(new Crashlytics(), new Twitter(authConfig)).debuggable(BuildConfig.DEBUG).build();
        } else {
            fabricKits = new Fabric.Builder(this).kits(new Twitter(authConfig)).debuggable(BuildConfig.DEBUG).build();
        }
        Fabric.with(fabricKits);
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
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityCompat.finishAfterTransition(this);
        startActivity(intent);
    }
}
