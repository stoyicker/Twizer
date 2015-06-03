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
import android.widget.RelativeLayout;

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.twizer.android.R;
import org.twizer.android.datamodel.Trend;
import org.twizer.android.datamodel.TrendResultWrapper;
import org.twizer.android.io.net.api.twitter.TwitterOAuthorizedApiClient;
import org.twizer.android.io.prefs.PreferenceAssistant;
import org.twizer.android.ui.widget.BoundNotifyingScrollView;
import org.twizer.android.ui.widget.NiceLoadTweetView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author stoyicker.
 */
public final class ContentFragment extends Fragment implements NiceLoadTweetView.IErrorViewListener, BoundNotifyingScrollView.IScrollBoundNotificationListener {

    private static final String KEY_IS_SEARCH_BOX_OPEN = "IS_SEARCH_BOX_OPEN";
    private static final String KEY_SEARCHABLES = "SEARCHABLES";

    @InjectView(R.id.searchBox)
    SearchBox mSearchBox;

    @InjectView(R.id.tweetContainer)
    BoundNotifyingScrollView mTweetContainer;

    @InjectView(R.id.niceLoadTweetView)
    NiceLoadTweetView mNiceLoadTweetView;

    @InjectView(R.id.randomizeFab)
    View mRandomizeFab;

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
            final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mTweetContainer.getLayoutParams();
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

        initSearchBox(mContext, mSearchBox, savedInstanceState);
        setupTweetView(mNiceLoadTweetView);
        setupFab(mTweetContainer);
    }

    private void setupFab(final BoundNotifyingScrollView scrollView) {
        scrollView.setBoundNotificationListener(this);
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

    private void initSearchBox(final Context context, final SearchBox searchBox, final Bundle savedInstanceState) {
        initSearchVoiceRecognition(searchBox);
        initSearchables(context, searchBox, savedInstanceState);
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
        outState.putStringArrayList(KEY_SEARCHABLES, mSearchBox.getSearchableNames());
    }

    private void initSearchVoiceRecognition(final SearchBox searchBox) {
        searchBox.enableVoiceRecognition(this);
    }

    private void initSearchables(final Context context, final SearchBox searchBox, final Bundle savedInstanceState) {
        List<String> searchables = null;

        if (savedInstanceState != null)
            searchables = savedInstanceState.getStringArrayList(KEY_SEARCHABLES);

        if (searchables != null && !searchables.isEmpty()) {
            createSearchablesFromStringList(searchables, searchBox);
        } else {
            loadSearchablesFromNetwork(context, searchBox);
        }
    }

    private void loadSearchablesFromNetwork(final Context context, final SearchBox searchBox) {
        TwitterOAuthorizedApiClient.getInstance().getTrendService().asyncGetTrendingTopics(
                context.getString(R.string.trend_location_id_world),
                PreferenceAssistant.readSharedBoolean(context, context.getString(R.string
                        .pref_key_include_hashtags), Boolean.TRUE) ? null : context.getString(R.string
                        .special_hashtag_exclusion_key), new
                        Callback<List<TrendResultWrapper>>() {
                            @Override
                            public void success(final List<TrendResultWrapper> trendResultWrappers, final Response
                                    response) {
                                if (trendResultWrappers.isEmpty())
                                    return;
                                final List<Trend> trendList = trendResultWrappers.get(0).getTrends();
                                final List<String> trendNames = new LinkedList<>();
                                for (final Trend trend : trendList) {
                                    trendNames.add(trend.getName());
                                }

                                createSearchablesFromStringList(trendNames, searchBox);
                            }

                            @Override
                            public void failure(final RetrofitError error) {
                                Log.e("NETWORKERROR?", error.toString());
                            }
                        });
    }

    private void createSearchablesFromStringList(final List<String> searchableNames, final SearchBox searchBox) {
        searchBox.clearSearchables();

        final Drawable resultDrawable = mContext.getDrawable(R.drawable.ic_search_suggestion);

        for (final String searchableName : searchableNames)
            searchBox.addSearchable(new SearchResult(searchableName, resultDrawable));
    }

    @Override
    public void onErrorViewClick() {
        mRandomizeFab.performClick();
    }

    @OnClick(R.id.randomizeFab)
    public void loadNewTweet() {
        final String defaultTweetId;

        //TODO This has to be a brand new, DIFFERENT tweet (right now it is just the last one)
        final String tweetId = PreferenceAssistant.readSharedString(mContext, mContext.getString(R.string.pref_key_last_tweet_id), defaultTweetId = mContext.getString(R.string.inital_tweet_id));

        try {
            mNiceLoadTweetView.loadTweet(Long.parseLong(tweetId));
        } catch (NumberFormatException ex) {
            mNiceLoadTweetView.loadTweet(Long.parseLong(defaultTweetId));
        }
    }

    @Override
    public void onTopBoundReached() {
    }

    @Override
    public void onBottomBoundReached() {
        mRandomizeFab.setVisibility(View.GONE);
    }

    @Override
    public void onBoundAbandoned() {
        mRandomizeFab.setVisibility(View.VISIBLE);
    }
}
