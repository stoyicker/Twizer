package org.twizer.android.ui.activity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.andexert.ripple.RippleView;

import org.twizer.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public final class MainActivity extends DescribedActivity {

    @InjectView(R.id.action_settings)
    RippleView actionSettings;

    private Context mContext;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mContext = getApplicationContext();

        actionSettings.setOnRippleCompleteListener(rippleView -> MainActivity.this.openSettings());
    }

    private void openSettings() {
        //noinspection unchecked
        startActivity(new Intent(mContext, SettingsActivity.class), ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }
}
