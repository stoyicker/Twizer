package org.twizer.android.ui.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import org.twizer.android.ui.UiUtils;

/**
 * @author stoyicker.
 */
public abstract class DescribedActivity extends Activity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            UiUtils.setTaskDescription(this);
    }

}
