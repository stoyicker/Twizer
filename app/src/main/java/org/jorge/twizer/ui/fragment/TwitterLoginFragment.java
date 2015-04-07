package org.jorge.twizer.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.jorge.twizer.R;

/**
 * @author stoyicker.
 */
public class TwitterLoginFragment extends CircularRevealedFragment {

    private static volatile Fragment mInstance;
    private static final Object LOCK = new Object();
    private ILoginListener mLoginListener;

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
        initTwitterLoginButton((TwitterLoginButton) v.findViewById(R.id.twitter_login_button));
        return v;
    }

    public void initTwitterLoginButton(final TwitterLoginButton twitterLoginButton) {
        twitterLoginButton.setOnClickListener(v -> {
            if (mLoginListener != null)
                mLoginListener.onLoginRequested();
        });
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                if (mLoginListener != null)
                    mLoginListener.onLoginSuccessful();
            }

            @Override
            public void failure(TwitterException e) {
                //TODO Check if it's an error (e.g. no internet) or a failure (e.g. bad credentials)
                if (mLoginListener != null) {
                    mLoginListener.onLoginErrored();
//                mLoginListener.onLoginFailed();
                }
            }
        });
    }

    public interface ILoginListener {

        public void onLoginSuccessful();

        public void onLoginRequested();

        public void onLoginErrored();

        public void onLoginFailed();
    }
}
