package org.jorge.twizer.ui.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.MenuItem;

/**
 * @author stoyicker.
 */
public class SettingsActivity extends IcedActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(Boolean.TRUE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityCompat.finishAfterTransition(this);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
