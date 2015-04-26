package org.jorge.twizer.ui.activity;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;

import org.jorge.twizer.R;
import org.jorge.twizer.ui.UiUtils;
import org.jorge.twizer.ui.fragment.TwitterLoginFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author stoyicker.
 */
public class LoginActivity extends DescribedActivity implements TwitterLoginFragment
        .ILoginListener {

    @InjectView(R.id.logo)
    View logoView;

    @InjectView(R.id.body)
    ViewGroup bodyGroup;

    private Context mContext;
    private Toast mLoginErrorToast = null;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        mContext = getApplicationContext();

        scheduleTwitterLoginScreenReveal();
    }

    private void scheduleTwitterLoginScreenReveal() {
        final Fragment fragment = TwitterLoginFragment.getInstance(mContext);
        scheduleSplashAwayWithContentReveal(fragment, getString(R.string.fragment_tag_twitter_login));
    }

    private void scheduleSplashAwayWithContentReveal(final Fragment contentToReveal, final String fragmentTag) {
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
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            logoView.startAnimation(translateAnimation);
        }, mContext.getResources().getInteger(R.integer
                .splash_delay_millis));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Fragment fragment = getFragmentManager().findFragmentByTag(mContext.getString(R.string.fragment_tag_twitter_login));
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onLoginSuccessful() {
        ActivityCompat.finishAfterTransition(this);
        //noinspection unchecked
        startActivity(new Intent(mContext, MainActivity.class), ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    @Override
    public void onLoginFailed() {
        if (mLoginErrorToast == null)
            mLoginErrorToast = Toast.makeText(mContext, R.string.error_msg_login, Toast.LENGTH_SHORT);
        mLoginErrorToast.show();
    }
}
