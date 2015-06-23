package org.twizer.android.ui.widget;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.MentionEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import org.twizer.android.R;
import org.twizer.android.io.db.SQLiteDAO;

import java.util.List;
import java.util.concurrent.Executors;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class NiceLoadTweetLayout extends FrameLayout {

    private Context mContext;
    private View mTweetView;

    @InjectView(R.id.loadError)
    View mErrorView;

    @InjectView(R.id.progressView)
    View mProgressView;

    private IErrorViewListener mErrorListener;
    private Tweet mTweetData;

    public NiceLoadTweetLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.widget_niceloadtweetview, this);
        ButterKnife.inject(this);

        mErrorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mErrorListener != null)
                    mErrorListener.onErrorViewClick();
            }
        });
    }

    public synchronized void loadTweet(final Long tweetId, final View controlButton) {
        if (mErrorView.getVisibility() != View.GONE)
            mErrorView.setVisibility(View.GONE);
        if (mProgressView.getVisibility() != View.VISIBLE)
            mProgressView.setVisibility(View.VISIBLE);
        TweetUtils.loadTweet(tweetId, new LoadCallback<Tweet>() {
            @Override
            public synchronized void success(final Tweet tweet) {
                NiceLoadTweetLayout.this.post(new Runnable() {
                    @Override
                    public void run() {
                        mErrorView.setVisibility(View.GONE);
                        if (mTweetView != null)
                            removeView(mTweetView);
                        NiceLoadTweetLayout.this.addView(mTweetView = new TweetView(mContext, tweet));
                        controlButton.clearAnimation();
                        controlButton.setEnabled(Boolean.TRUE);
                    }
                });
            }

            @Override
            public synchronized void failure(final TwitterException exception) {
                Log.e("ERROR", exception.getMessage());
                if (mTweetView != null)
                    removeView(mTweetView);
                mTweetView = null;
                NiceLoadTweetLayout.this.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressView.setVisibility(View.GONE);
                        mErrorView.setVisibility(View.VISIBLE);
                        controlButton.clearAnimation();
                        controlButton.setEnabled(Boolean.TRUE);
                    }
                });
            }
        });

        Twitter.getApiClient().getStatusesService().show(tweetId, Boolean.TRUE, null, null, new Callback<Tweet>() {
            @Override
            public void success(final Result<Tweet> result) {
                NiceLoadTweetLayout.this.setCurrentTweetData(result.data);
            }

            @Override
            public void failure(final TwitterException e) {
                NiceLoadTweetLayout.this.setCurrentTweetData(null);
            }
        });
    }

    private void setCurrentTweetData(final Tweet data) {
        mTweetData = data;
    }

    public void setErrorListener(IErrorViewListener mErrorListener) {
        this.mErrorListener = mErrorListener;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(final Void[] params) {

                        storeTweetAsLiked();
                        return null;
                    }

                    private void storeTweetAsLiked() {
                        if (mTweetData != null) {
                            final SQLiteDAO sqliteDaoInstance = SQLiteDAO.getInstance();

                            final String username = mTweetData.user.screenName;

                            sqliteDaoInstance.insertUsername(username);

                            final List<MentionEntity> mentions = mTweetData.entities.userMentions;

                            if (mentions != null)
                                for (final MentionEntity mention : mentions)
                                    sqliteDaoInstance.insertUsername(mention.screenName);

                            final List<HashtagEntity> hashtags = mTweetData.entities.hashtags;

                            if (hashtags != null)
                                for (final HashtagEntity hashtag : hashtags)
                                    sqliteDaoInstance.insertHashtag(hashtag);

                            Log.d("debug", "Tweet liked");
                        }
                    }
                }.executeOnExecutor(Executors.newSingleThreadExecutor());
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
        }

        return super.onInterceptTouchEvent(ev);
    }


    public interface IErrorViewListener {
        void onErrorViewClick();
    }
}
