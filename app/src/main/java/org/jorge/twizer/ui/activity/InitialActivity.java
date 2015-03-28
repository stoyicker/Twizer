package org.jorge.twizer.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

import org.jorge.twizer.DebugUtils;
import org.jorge.twizer.R;

import java.io.File;

import io.fabric.sdk.android.Fabric;

/**
 * @author stoyicker.
 */
public class InitialActivity extends Activity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DebugUtils.autoUnlock(this);

        final Context appContext = getApplicationContext();

        initCrashlytics(appContext);
        flushCacheIfNecessary(appContext);
    }

    private void initCrashlytics(final Context context) {
        Fabric.with(context, new Crashlytics());
    }

    private void flushCacheIfNecessary(final Context context) {
        final File cacheDir;
        final Integer CACHE_SIZE_LIMIT_BYTES = context.getResources().getInteger(R.integer
                .max_cache_size_bytes);
        if ((cacheDir = context.getCacheDir()).length() > CACHE_SIZE_LIMIT_BYTES) {
//            FileManager.recursiveDelete(cacheDir);
        }
    }
}
