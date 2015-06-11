package org.twizer.android.app;

import android.app.Application;

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

        initTwitter();
    }

    private void initTwitter() {
        final TwitterAuthConfig authConfig = new TwitterAuthConfig(BuildConfig.TWITTER_CONSUMER_KEY, BuildConfig.TWITTER_CONSUMER_SECRET);
        final Fabric fabricKits = new Fabric.Builder(this).kits(new Twitter(authConfig)).debuggable(BuildConfig.DEBUG).build();
        Fabric.with(fabricKits);
    }
}
