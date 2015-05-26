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

    public NiceLoadTweetView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.widget_niceloadtweetview, this);
        ButterKnife.inject(this);
    }

    public synchronized void loadTweet(final Long tweetId) {
        if (mTweetView != null)
            removeView(mTweetView);
        if (mProgressView.getVisibility() != View.VISIBLE)
            mProgressView.setVisibility(View.VISIBLE);
        TweetUtils.loadTweet(tweetId, new LoadCallback<Tweet>() {
            @Override
            public void success(final Tweet tweet) {
                NiceLoadTweetView.this.addView(mTweetView = new TweetView(mContext, tweet));
                mErrorView.setVisibility(View.GONE);
            }

            @Override
            public void failure(final TwitterException exception) {
                mTweetView = null;
                mProgressView.setVisibility(View.GONE);
                mErrorView.setVisibility(View.VISIBLE);
            }
        });
    }
}
