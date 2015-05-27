package org.twizer.android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import org.twizer.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public class NiceLoadTweetView extends FrameLayout {

    private Context mContext;
    private View mTweetView;

    @InjectView(R.id.loadError)
    View mErrorView;

    @InjectView(R.id.progressView)
    View mProgressView;

    private IErrorViewListener mErrorListener;

    public NiceLoadTweetView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.widget_niceloadtweetview, this);
        ButterKnife.inject(this);

        mErrorView.setOnClickListener(v -> {
            if (mErrorListener != null)
                mErrorListener.onErrorViewClick();
        });
    }

    public synchronized void loadTweet(final Long tweetId) {
        if (mTweetView != null)
            removeView(mTweetView);
        if (mErrorView.getVisibility() != View.GONE)
            mErrorView.setVisibility(View.GONE);
        if (mProgressView.getVisibility() != View.VISIBLE)
            mProgressView.setVisibility(View.VISIBLE);
        TweetUtils.loadTweet(tweetId, new LoadCallback<Tweet>() {
            @Override
            public void success(final Tweet tweet) {
                NiceLoadTweetView.this.post(() -> {
                    mErrorView.setVisibility(View.GONE);
                    NiceLoadTweetView.this.addView(mTweetView = new TweetView(mContext, tweet));
                });
            }

            @Override
            public void failure(final TwitterException exception) {
                mTweetView = null;
                NiceLoadTweetView.this.post(() -> {
                    mProgressView.setVisibility(View.GONE);
                    mErrorView.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    public void setErrorListener(IErrorViewListener mErrorListener) {
        this.mErrorListener = mErrorListener;
    }

    public interface IErrorViewListener {
        void onErrorViewClick();
    }
}
