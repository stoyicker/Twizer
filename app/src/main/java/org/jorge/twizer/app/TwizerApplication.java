package org.jorge.twizer.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.jorge.twizer.BuildConfig;

import io.fabric.sdk.android.Fabric;

public final class TwizerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            initStetho();
        }
        initCrashlytics();
    }

    private void initStetho() {
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
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
