package org.jorge.twizer.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import org.jorge.twizer.ui.UiUtils;

/**
 * @author stoyicker.
 */
public abstract class DescribedActivity extends Activity {
    //TODO Convert this into DescribedIcedActivity
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UiUtils.setTaskDescription(this);
    }

}
