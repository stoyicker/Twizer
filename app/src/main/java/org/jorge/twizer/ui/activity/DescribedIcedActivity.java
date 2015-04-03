package org.jorge.twizer.ui.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.jorge.twizer.ui.UiUtils;

import icepick.Icepick;

/**
 * @author stoyicker.
 */
public abstract class DescribedIcedActivity extends Activity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            UiUtils.setTaskDescription(this);
    }

    @Override
    public void onSaveInstanceState(final @NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

}
