package org.jorge.twizer.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.andexert.ripple.RippleView;

import org.jorge.twizer.R;
import org.jorge.twizer.ui.UiUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * @author stoyicker.
 */
public class MainActivity extends DescribedIcedActivity {

    @InjectView(R.id.action_settings)
    RippleView actionSettings;

    @InjectView(R.id.logo)
    View logoView;

    @InjectView(R.id.body)
    ViewGroup bodyGroup;

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
        final Integer initialOrientation = UiUtils.getScreenOrientation(context);
        //noinspection ResourceType
        setRequestedOrientation(initialOrientation);
        handler.postDelayed(() -> {
            Integer shift;
            TranslateAnimation translateAnimation;
            if (initialOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                    initialOrientation ==
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                shift = -(UiUtils.getScreenHeight(context) - logoView
                        .getMeasuredHeight()) / 2;
                translateAnimation = new TranslateAnimation(Animation
                        .RELATIVE_TO_SELF, 0, Animation
                        .RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                        Animation.ABSOLUTE, shift);
            } else {
                shift = -(UiUtils.getScreenWidth(context) - logoView
                        .getMeasuredWidth()) / 2;
                translateAnimation = new TranslateAnimation(Animation
                        .RELATIVE_TO_SELF, 0, Animation
                        .ABSOLUTE, shift, Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0);
            }
            translateAnimation.setDuration(context.getResources().getInteger(R.integer
                    .splash_anim_duration_millis));
            translateAnimation.setFillAfter(Boolean.TRUE);
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)
                            bodyGroup.getLayoutParams();
                    if (initialOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                            initialOrientation ==
                                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
                        lp.setMargins(0, logoView.getHeight(), 0, 0);
                    else {
                        lp.setMargins(logoView.getWidth(), 0, 0, 0);
                    }
                    bodyGroup.setLayoutParams(lp);
                    Integer childrenAmount = bodyGroup.getChildCount() - 1;
                    for (; childrenAmount >= 0; childrenAmount--) {
                        final View c = bodyGroup.getChildAt(childrenAmount);
                        circularRevealView(c);
                    }
                    circularRevealView(actionSettings);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                }

                private void circularRevealView(final View viewToReveal) {
                    final Integer cx = (viewToReveal.getLeft() + viewToReveal.getRight()) / 2,
                            cy = (viewToReveal.getTop() + viewToReveal.getBottom()) / 2;
                    final SupportAnimator animator = ViewAnimationUtils.createCircularReveal
                            (viewToReveal, cx,
                                    cy, 0, Math.max(viewToReveal.getWidth(),
                                            viewToReveal.getHeight()));
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
