package org.jorge.twizer.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;

import icepick.Icepick;

/**
 * @author stoyicker.
 */
public abstract class IcedActivity extends Activity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(final @NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

}
