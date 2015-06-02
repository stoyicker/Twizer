package org.twizer.android.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.twizer.android.R;
import org.twizer.android.datamodel.Trend;
import org.twizer.android.datamodel.TrendResultWrapper;
import org.twizer.android.io.net.api.twitter.TwitterOAuthorizedApiClient;
import org.twizer.android.io.prefs.PreferenceAssistant;
import org.twizer.android.ui.widget.NiceLoadTweetView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author stoyicker.
 */
public final class ContentFragment extends Fragment implements NiceLoadTweetView.IErrorViewListener {

    private static final String KEY_IS_SEARCH_BOX_OPEN = "IS_SEARCH_BOX_OPEN";

    @InjectView(R.id.searchBox)
    SearchBox mSearchBox;

    @InjectView(R.id.tweetContainer)
    ViewGroup mTweetContainer;

    @InjectView(R.id.niceLoadTweetView)
    NiceLoadTweetView mNiceLoadTweetView;

    Context mContext;
    private Boolean mWasSearchBoxOpen;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_content, container, Boolean.FALSE);
        ButterKnife.inject(this, view);

        view.post(() -> {
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTweetContainer.getLayoutParams();
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + mSearchBox.getHeight(), layoutParams.rightMargin, layoutParams.bottomMargin);
        });

        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mWasSearchBoxOpen = savedInstanceState.getBoolean(KEY_IS_SEARCH_BOX_OPEN);
        }

        initSearchBox(mContext, mSearchBox);
        setupTweetView(mNiceLoadTweetView);
    }

    private void setupTweetView(final NiceLoadTweetView niceLoadTweetView) {
        niceLoadTweetView.setErrorListener(this);
        final String defaultTweetId;

        final String tweetId = PreferenceAssistant.readSharedString(mContext, mContext.getString(R.string.pref_key_last_tweet_id), defaultTweetId = mContext.getString(R.string.inital_tweet_id));

        try {
            niceLoadTweetView.loadTweet(Long.parseLong(tweetId));
        } catch (NumberFormatException ex) {
            niceLoadTweetView.loadTweet(Long.parseLong(defaultTweetId));
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == Activity.RESULT_OK) {
            final ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mSearchBox.populateEditText(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initSearchBox(final Context context, final SearchBox searchBox) {
        initSearchVoiceRecognition(searchBox);
        initSearchables(context, searchBox);
        initSearchBoxVisibility(searchBox);
    }

    private void initSearchBoxVisibility(final SearchBox searchBox) {
        if (mWasSearchBoxOpen != null)
            searchBox.post(() -> {
                if (mWasSearchBoxOpen)
                    searchBox.openSearch(Boolean.FALSE);
                else
                    searchBox.mockSearch();
            });
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_IS_SEARCH_BOX_OPEN, mSearchBox.isOpen());
    }

    private void initSearchVoiceRecognition(final SearchBox searchBox) {
        searchBox.enableVoiceRecognition(this);
    }

    private void initSearchables(final Context context, final SearchBox searchBox) {
        TwitterOAuthorizedApiClient.getContactApiClient(context).asyncGetTrendingTopics(
                context.getString(R.string.trend_location_id_world),
                PreferenceAssistant.readSharedBoolean(context, context.getString(R.string
                        .pref_key_include_hashtags), Boolean.TRUE) ? null : context.getString(R.string
                        .special_hashtag_exclusion_key), new
                        Callback<TrendResultWrapper>() {
                            @Override
                            public void success(final TrendResultWrapper trendResultWrapper, final Response
                                    response) {
                                Log.d("debug", "Trends loaded successfully"); //TODO Remove this log
                                final Drawable resultDrawable = mContext.getDrawable(R.drawable.ic_search_suggestion);
                                final List<Trend> trendList = trendResultWrapper.getTrends();

                                searchBox.clearSearchables();

                                for (Trend trend : trendList)
                                    searchBox.addSearchable(new SearchResult(trend.getName(), resultDrawable));
                            }

                            @Override
                            public void failure(final RetrofitError error) {
                                Log.e("NETWORKERROR?", error.toString());
                            }
                        });
    }

    @Override
    public void onErrorViewClick() {
        final String defaultTweetId;

        //TODO This has to be a brand new, DIFFERENT tweet
        final String tweetId = PreferenceAssistant.readSharedString(mContext, mContext.getString(R.string.pref_key_last_tweet_id), defaultTweetId = mContext.getString(R.string.inital_tweet_id));

        try {
            mNiceLoadTweetView.loadTweet(Long.parseLong(tweetId));
        } catch (NumberFormatException ex) {
            mNiceLoadTweetView.loadTweet(Long.parseLong(defaultTweetId));
        }
    }
}
