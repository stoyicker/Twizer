package org.twizer.android.ui.activity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

        actionSettings.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(final RippleView rippleView) {
                MainActivity.this.openSettings();
            }
        });
    }

    private void openSettings() {
        final Intent intent = new Intent(mContext, SettingsActivity.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            //noinspection unchecked
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        else
            startActivity(intent);
    }
}
