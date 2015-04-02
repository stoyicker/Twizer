package org.jorge.twizer.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.andexert.ripple.RippleView;

import org.jorge.twizer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

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

        final Context context = getApplicationContext();

        scheduleReveal(context);

        actionSettings.setOnRippleCompleteListener(rippleView -> MainActivity.this.openSettings
                (context));
    }

    private void scheduleReveal(final Context context) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            final TranslateAnimation translateAnimation = new TranslateAnimation(Animation
                    .RELATIVE_TO_SELF, 0, Animation
                    .RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, -1); //FIXME Correctly align top
            translateAnimation.setDuration(context.getResources().getInteger(R.integer
                    .splash_anim_duration_millis));
            translateAnimation.setFillAfter(Boolean.TRUE);
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //TODO Reveal the views
                    circularRevealView(actionSettings);
                }

                private void circularRevealView(final View viewToReveal) {
                    final Integer cx = (viewToReveal.getLeft() + viewToReveal.getRight()) / 2,
                            cy = (viewToReveal.getTop() + viewToReveal.getBottom()) / 2;
                    final SupportAnimator animator = ViewAnimationUtils.createCircularReveal
                            (viewToReveal, cx,
                                    cy, 0, Math.max(viewToReveal.getWidth(), viewToReveal.getHeight()));
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(context.getResources().getInteger(R.integer
                            .circular_reveal_duration_millis));
                    viewToReveal.setVisibility(View.VISIBLE);
                    animator.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            logoView.startAnimation(translateAnimation);
        }, context.getResources().getInteger(R.integer
                .splash_delay_millis));
    }

    private void openSettings(final Context context) {
        startActivity(new Intent(context, SettingsIcedActivity.class));
    }
}
