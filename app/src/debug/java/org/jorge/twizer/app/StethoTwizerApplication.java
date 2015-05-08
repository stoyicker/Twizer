package org.jorge.twizer.app;

import com.facebook.stetho.Stetho;

public final class StethoTwizerApplication extends TwizerApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        initStetho();
    }

    private void initStetho() {
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }
}
