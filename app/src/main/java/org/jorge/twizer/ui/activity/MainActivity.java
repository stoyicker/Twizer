package org.jorge.twizer.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.andexert.ripple.RippleView;

import org.jorge.twizer.DebugUtils;
import org.jorge.twizer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author stoyicker.
 */
public class MainActivity extends IcedActivity {

    @InjectView(R.id.action_settings)
    RippleView actionSettings;

    @InjectView(R.id.logo)
    View logoView;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        DebugUtils.d("debug", "onCreate(d)");

        final Context context = getApplicationContext();

        scheduleReveal(context);

        actionSettings.setOnRippleCompleteListener(rippleView -> MainActivity.this.openSettings
                (context));
    }

    private void scheduleReveal(final Context context) {
        DebugUtils.d("debug", "scheduleReveal");
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            final TranslateAnimation translateAnimation = new TranslateAnimation(Animation
                    .RELATIVE_TO_SELF, 0, Animation
                    .RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, -1);
            translateAnimation.setDuration(context.getResources().getInteger(R.integer
                    .splash_anim_duration_millis));
            translateAnimation.setFillAfter(Boolean.TRUE);
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    DebugUtils.d("debug", "onAnimationStart");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //TODO Reveal the views
                    DebugUtils.d("debug", "onAnimationEnd");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    DebugUtils.d("debug", "onAnimationRepeat");
                }
            });
            DebugUtils.d("debug", "About to startAnimation");
            logoView.startAnimation(translateAnimation);
            DebugUtils.d("debug", "Animation started");
        }, context.getResources().getInteger(R.integer
                .splash_delay_millis));
    }

    private void openSettings(final Context context) {
        startActivity(new Intent(context, SettingsIcedActivity.class));
    }
}
