package org.twizer.android.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.twizer.android.BuildConfig;

import io.fabric.sdk.android.Fabric;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public class TwizerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initCrashlytics();
    }

    private void initCrashlytics() {
        final TwitterAuthConfig authConfig = new TwitterAuthConfig(BuildConfig.TWITTER_CONSUMER_KEY, BuildConfig.TWITTER_CONSUMER_SECRET);
        Fabric fabricKits;
        if (BuildConfig.USE_CRASHLYTICS) {
            fabricKits = new Fabric.Builder(this).kits(new Crashlytics(), new Twitter(authConfig)).debuggable(BuildConfig.DEBUG).build();
        } else {
            fabricKits = new Fabric.Builder(this).kits(new Twitter(authConfig)).debuggable(BuildConfig.DEBUG).build();
        }
        Fabric.with(fabricKits);
    }
}
