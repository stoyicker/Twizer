package org.jorge.twizer.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.andexert.ripple.RippleView;

import org.jorge.twizer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author stoyicker.
 */
public class MainActivity extends IcedActivity {

    @InjectView(R.id.action_settings)
    RippleView actionSettings;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        actionSettings.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(final RippleView rippleView) {
                MainActivity.this.openSettings();
            }
        });
    }

    private void openSettings() {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    }
}
