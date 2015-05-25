package org.twizer.android.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import org.twizer.android.ui.UiUtils;

/**
 * @author stoyicker.
 */
public abstract class DescribedActivity extends Activity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UiUtils.setTaskDescription(this);
    }

}
