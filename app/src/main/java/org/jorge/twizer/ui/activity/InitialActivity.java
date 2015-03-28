package org.jorge.twizer.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * @author stoyicker.
 */
public class InitialActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCrashlytics();
    }

    private void initCrashlytics() {
        Fabric.with(this, new Crashlytics());
    }

}
