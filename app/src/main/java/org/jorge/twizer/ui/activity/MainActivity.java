package org.jorge.twizer.ui.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.andexert.ripple.RippleView;

import org.jorge.twizer.R;
import org.jorge.twizer.ui.UiUtils;
import org.jorge.twizer.ui.fragment.ContentFragment;
import org.jorge.twizer.ui.fragment.TwitterLoginFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static org.jorge.twizer.ui.UiUtils.circularRevealView;

//import org.jorge.twizer.DebugUtils;

/**
 * @author stoyicker.
 */
public class MainActivity extends DescribedActivity implements TwitterLoginFragment
        .ILoginListener {

    @InjectView(R.id.action_settings)
    RippleView actionSettings;

    @InjectView(R.id.logo)
    View logoView;

    @InjectView(R.id.body)
    ViewGroup bodyGroup;

    private Context mContext;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mContext = getApplicationContext();

        actionSettings.setOnRippleCompleteListener(rippleView -> MainActivity.this.openSettings());

        //TODO Try to login while the splash is shown so that I know which screen to choose
        scheduleTwitterLoginScreenReveal();
    }

    private void scheduleTwitterLoginScreenReveal() {
        final Fragment fragment = TwitterLoginFragment.getInstance(mContext);
        scheduleSplashAwayWithContentReveal(fragment, Boolean.FALSE, mContext.getString(R.string.fragment_tag_twitter_login));
    }

    private void scheduleMainScreenReveal() {
        final Fragment fragment = ContentFragment.getInstance(mContext);
        scheduleSplashAwayWithContentReveal(fragment, Boolean.TRUE, mContext.getString(R.string.fragment_tag_content));
    }

    private void scheduleSplashAwayWithContentReveal(final Fragment contentToReveal, final Boolean revealSettings, final String fragmentTag) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            final Integer shift = -(UiUtils.getScreenHeight(mContext) - logoView
                    .getMeasuredHeight()) / 2;
            final Animation translateAnimation = new TranslateAnimation(Animation
                    .RELATIVE_TO_SELF, 0, Animation
                    .RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                    Animation.ABSOLUTE, shift);

            translateAnimation.setDuration(mContext.getResources().getInteger(R.integer
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
                    lp.setMargins(0, logoView.getHeight(), 0, 0);

                    bodyGroup.setLayoutParams(lp);
                    getFragmentManager().beginTransaction().replace(R.id.content_layout,
                            contentToReveal, fragmentTag)
                            .commitAllowingStateLoss();
                    if (revealSettings)
                        circularRevealView(mContext, actionSettings);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            logoView.startAnimation(translateAnimation);
        }, mContext.getResources().getInteger(R.integer
                .splash_delay_millis));
    }

    private void immediateMoveSplashToCenter() {
        final Integer shift = UiUtils.getScreenHeight(mContext) - logoView
                .getMeasuredHeight() / 2;
        final Animation translateAnimation = new TranslateAnimation(Animation
                .RELATIVE_TO_SELF, 0, Animation
                .RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.ABSOLUTE, shift);
        translateAnimation.setDuration(mContext.getResources().getInteger(R.integer
                .splash_anim_duration_millis));
        translateAnimation.setFillAfter(Boolean.TRUE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Fragment fragment = getFragmentManager().findFragmentByTag(mContext.getString(R.string.fragment_tag_twitter_login));
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void openSettings() {
        startActivity(new Intent(mContext, SettingsActivity.class));
    }

    @Override
    public void onLoginSuccessful() {
//        DebugUtils.d("debug", "onLoginSuccessful");
        immediateMoveSplashToCenter();
        scheduleMainScreenReveal();
    }

    @Override
    public void onLoginFailed() {
//        DebugUtils.d("debug", "onLoginFailed");
        //TODO Show some complaint
        scheduleTwitterLoginScreenReveal();
    }

    @Override
    public void onLoginErrored() {
//        DebugUtils.d("debug", "onLoginErrored");
        //TODO Show some error message and delete existent credentials, if any
        scheduleTwitterLoginScreenReveal();
    }
}
