package org.jorge.twizer.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.andexert.ripple.RippleView;

import org.jorge.twizer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icepick;

public class MainActivity extends DescribedActivity {

    @InjectView(R.id.action_settings)
    RippleView actionSettings;

    private Context mContext;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mContext = getApplicationContext();

        actionSettings.setOnRippleCompleteListener(rippleView -> MainActivity.this.openSettings());
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void openSettings() {
        startActivity(new Intent(mContext, SettingsActivity.class));
    }
}
