package org.twizer.android.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.twizer.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author stoyicker.
 */
public final class TwitterLoginFragment extends CircularRevealedFragment {

    private static volatile Fragment mInstance;
    private static final Object LOCK = new Object();
    private ILoginListener mLoginListener;

    @InjectView(R.id.twitter_login_button)
    TwitterLoginButton mTwitterLoginButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mLoginListener = (ILoginListener) activity;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(activity.getClass().getName() + " should implement " + ILoginListener.class.getName());
        }
    }

    public static Fragment getInstance(final Context context) {
        Fragment ret = mInstance;
        if (ret == null) {
            synchronized (LOCK) {
                ret = mInstance;
                if (ret == null) {
                    ret = Fragment.instantiate(context, TwitterLoginFragment.class.getName());
                    mInstance = ret;
                }
            }
        }
        return ret;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_twitter_login, container, Boolean.FALSE);
        ButterKnife.inject(this, v);
        initTwitterLoginButton();
        return v;
    }

    public void initTwitterLoginButton() {
        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(final Result<TwitterSession> result) {
                if (mLoginListener != null)
                    mLoginListener.onLoginSuccessful();
            }

            @Override
            public void failure(final TwitterException e) {
                if (mLoginListener != null) {
                    mLoginListener.onLoginFailed();
                }
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mTwitterLoginButton.onActivityResult(requestCode, resultCode,
                data);
    }

    public interface ILoginListener {

        void onLoginSuccessful();

        void onLoginFailed();
    }
}
