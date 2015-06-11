package org.twizer.android.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.services.params.Geocode;

import org.twizer.android.R;
import org.twizer.android.datamodel.TrendResultWrapper;
import org.twizer.android.datamodel.TrendWrapper;
import org.twizer.android.io.net.api.twitter.TwitterTrendServiceExtensionApiClient;
import org.twizer.android.io.net.provider.geo.CasualLocationProvider;
import org.twizer.android.io.net.provider.twitter.TweetProvider;
import org.twizer.android.io.preference.PreferenceAssistant;
import org.twizer.android.ui.widget.BoundNotifyingScrollView;
import org.twizer.android.ui.widget.NiceLoadTweetView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

//FIXME Why the fuck does rotating make the box behave weird when there are things on it? Also, why does it trigger a search?
//FIXME Why does the tweet load fail so much? Where are my preferences?

/**
 * @author stoyicker.
 */
public final class ContentFragment extends Fragment implements NiceLoadTweetView.IErrorViewListener, BoundNotifyingScrollView.IScrollBoundNotificationListener, TweetProvider.ITweetReceiver {

    private static final String KEY_IS_SEARCH_BOX_OPEN = "IS_SEARCH_BOX_OPEN";
    private static final String KEY_SEARCHABLES = "SEARCHABLES";
    private static final String KEY_TWEET_LIST = "TWEET_LIST";
    private List<String> mTweetIdList;

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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_content, container, Boolean.FALSE);
        ButterKnife.inject(this, view);

        view.post(() -> {
            final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mTweetContainer.getLayoutParams();
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + mSearchBox.getHeight(), layoutParams.rightMargin, layoutParams.bottomMargin);
            mSearchBox.populateEditText(Collections.singletonList(PreferenceAssistant.readSharedString(mContext, mContext.getString(R.string.pref_key_last_search_text), mContext.getString(R.string.default_last_search_text))));
        });

        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mWasSearchBoxOpen = savedInstanceState.getBoolean(KEY_IS_SEARCH_BOX_OPEN);
            mTweetIdList = savedInstanceState.getStringArrayList(KEY_TWEET_LIST);
        }

        initTweetView(mNiceLoadTweetView);
        initSearchBox(mContext, mSearchBox, savedInstanceState);
        initFab(mTweetContainer);
    }

    private void initFab(final BoundNotifyingScrollView scrollView) {
        scrollView.setBoundNotificationListener(this);
    }

    private void initTweetView(final NiceLoadTweetView niceLoadTweetView) {
        niceLoadTweetView.setErrorListener(this);
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
        initSearchBoxListener(searchBox);
    }

    private void initSearchBoxListener(final SearchBox searchBox) {
        searchBox.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {

            }

            @Override
            public void onSearchCleared() {

            }

            @Override
            public void onSearchClosed() {

            }

            @Override
            public void onSearchTermChanged() {

            }

            @Override
            public void onSearch(final String result) {
                if (mTweetIdList != null && !mTweetIdList.isEmpty())
                    mTweetIdList.clear();
                PreferenceAssistant.writeSharedString(mContext, mContext.getString(R.string.pref_key_last_search_text), mSearchBox.getSearchText());
                mRandomizeFab.performClick();
            }
        });
    }

    private void initSearchBoxVisibility(final SearchBox searchBox) {
        if (mWasSearchBoxOpen != null)
            searchBox.post(() -> {
                if (mWasSearchBoxOpen)
                    searchBox.openSearch(searchBox.getSearchables().isEmpty());
                else
                    searchBox.mockSearch();
            });
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_IS_SEARCH_BOX_OPEN, mSearchBox.isOpen());
        outState.putStringArrayList(KEY_SEARCHABLES, mSearchBox.getSearchableNames());
        outState.putStringArrayList(KEY_TWEET_LIST, (ArrayList<String>) mTweetIdList);
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
        TwitterTrendServiceExtensionApiClient.getInstance().getTrendService().asyncGetTrendingTopics(
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
                                final List<TrendWrapper> trendList = trendResultWrappers.get(0).getTrends();
                                final List<String> trendNames = new LinkedList<>();
                                for (final TrendWrapper trend : trendList) {
                                    trendNames.add(trend.getName());
                                }

                                createSearchablesFromStringList(trendNames, searchBox);
                            }

                            @Override
                            public void failure(final RetrofitError error) {
                                Log.e("ERROR", error.getMessage());
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
    public void clickRandomizeFab() {
        if (loadNewTweet()) {
            final RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(500);
            mRandomizeFab.startAnimation(rotate);
        } else {
            final Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
            mSearchBox.startAnimation(shake);
        }
    }

    private Boolean loadNewTweet() {
        if (TextUtils.isEmpty(mSearchBox.getSearchText()))
            return Boolean.FALSE;

        Location location = null;
        if (PreferenceAssistant.readSharedBoolean(mContext, mContext.getString(R.string.pref_key_search_type_nearby), Boolean.TRUE)) {
            location = CasualLocationProvider.getInstance(mContext).getLastKnownLocation();
        }

        if (mTweetIdList == null)
            mTweetIdList = new ArrayList<>();

        if (mTweetIdList.isEmpty())
            fillTweetList(location);
        else
            showNextTweet();

        return Boolean.TRUE;
    }

    private void fillTweetList(@Nullable final Location coordinates) {
        final Geocode geocode = coordinates == null ? null :
                new Geocode(coordinates.getLatitude(), coordinates.getLongitude(), PreferenceAssistant.readSharedInteger(mContext, mContext.getString(R.string.pref_key_search_radius), mContext.getResources().getInteger(R.integer.default_search_radius)), PreferenceAssistant.readSharedString(mContext, mContext.getString(R.string.pref_key_search_distance_unit), mContext.getString(R.string.default_search_distance_unit_value)).contentEquals(mContext.getString(R.string.search_distance_unit_km_value)) ? Geocode.Distance.KILOMETERS : Geocode.Distance.MILES);

        TweetProvider.getTweets(mContext, geocode, mSearchBox.getSearchText(), this);
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

    @Override
    public void onTweetsProvided(final List<String> tweetIds) {
        mTweetIdList.addAll(tweetIds);
        showNextTweet();
    }

    @Override
    public void onFailedToProvideTweets(final TwitterException e) {
        Log.e("ERROR", e.getMessage());
    }

    private void showNextTweet() {
        String tweetId;

        try {
            mNiceLoadTweetView.loadTweet(Long.parseLong(tweetId = mTweetIdList.remove(0)), mRandomizeFab);
        } catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException ex) {
            mNiceLoadTweetView.loadTweet(Long.parseLong(tweetId = mContext.getString(R.string.default_tweet_id)), mRandomizeFab);
        }

        PreferenceAssistant.writeSharedString(mContext, mContext.getString(R.string.pref_key_last_tweet_id), tweetId);
    }
}
