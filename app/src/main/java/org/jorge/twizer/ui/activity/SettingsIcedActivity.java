package org.jorge.twizer.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.jorge.twizer.R;
import org.jorge.twizer.ui.fragment.SettingsPreferenceFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icepick;

/**
 * @author stoyicker.
 */
public class SettingsIcedActivity extends ActionBarActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);

        if (toolbar != null) {
            toolbar.setTitle(R.string.action_settings);
            setSupportActionBar(toolbar);
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(Boolean.TRUE);

        getFragmentManager().beginTransaction().replace(R.id.settings_list,
                new SettingsPreferenceFragment(),
                getApplicationContext().getString(R.string.action_settings))
                .commit();
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

    @Override
    public void onSaveInstanceState(final @NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }
}
