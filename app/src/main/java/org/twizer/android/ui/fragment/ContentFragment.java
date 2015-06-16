package org.twizer.android.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
import org.twizer.android.io.net.provider.twitter.TweetProviderTask;
import org.twizer.android.io.preference.PreferenceAssistant;
import org.twizer.android.ui.widget.BoundNotifyingScrollView;
import org.twizer.android.ui.widget.NiceLoadTweetView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author stoyicker.
 */
public final class ContentFragment extends Fragment implements NiceLoadTweetView.IErrorViewListener, BoundNotifyingScrollView.IScrollBoundNotificationListener, TweetProviderTask.ITweetReceiver {

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
    private ScheduledExecutorService mCurrentQueryExecutor;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
    }

    @Override
    public void onResume() {
        super.onResume();

        reshowLastTweet();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_content, container, Boolean.FALSE);
        ButterKnife.inject(this, view);

        view.post(() -> {
            final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mTweetContainer.getLayoutParams();
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + mSearchBox.getHeight(), layoutParams.rightMargin, layoutParams.bottomMargin);
            final String lastSearchText = PreferenceAssistant.readSharedString(mContext, mContext.getString(R.string.pref_key_last_search_text), mContext.getString(R.string.default_last_search_text));
            mSearchBox.populateEditTextWithoutSearch(Collections.singletonList(lastSearchText));
            if (!TextUtils.isEmpty(lastSearchText))
                startResultGatheringPeriodicTask();
        });

        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
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
            mSearchBox.populateEditTextAndSearch(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initSearchBox(final Context context, final SearchBox searchBox, final Bundle savedInstanceState) {
        initSearchVoiceRecognition(searchBox);
        initSearchables(context, searchBox, savedInstanceState);
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

                mRandomizeFab.performClick();
            }
        });
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        mCurrentQueryExecutor.shutdownNow();
        mCurrentQueryExecutor = null;

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
            animateAndDisableFab();
            PreferenceAssistant.writeSharedString(mContext, mContext.getString(R.string.pref_key_last_search_text), mSearchBox.getSearchText());
        } else {
            final Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
            mSearchBox.startAnimation(shake);
        }
    }

    private synchronized Boolean loadNewTweet() {
        if (TextUtils.isEmpty(mSearchBox.getSearchText()))
            return Boolean.FALSE;

        if (mTweetIdList == null)
            mTweetIdList = new ArrayList<>();

        showNextTweet();

        return Boolean.TRUE;
    }

    private synchronized void startResultGatheringPeriodicTask() {
        if (mCurrentQueryExecutor != null)
            mCurrentQueryExecutor.shutdownNow(); //.shutdown should suffice also
        Location coordinates = null;
        if (PreferenceAssistant.readSharedBoolean(mContext, mContext.getString(R.string.pref_key_search_type_nearby), Boolean.TRUE)) {
            coordinates = CasualLocationProvider.getInstance(mContext).getLastKnownLocation();
        }
        final Geocode geocode = coordinates == null ? null :
                new Geocode(coordinates.getLatitude(), coordinates.getLongitude(), PreferenceAssistant.readSharedInteger(mContext, mContext.getString(R.string.pref_key_search_radius), mContext.getResources().getInteger(R.integer.default_search_radius)), PreferenceAssistant.readSharedString(mContext, mContext.getString(R.string.pref_key_search_distance_unit), mContext.getString(R.string.default_search_distance_unit_value)).contentEquals(mContext.getString(R.string.search_distance_unit_km_value)) ? Geocode.Distance.KILOMETERS : Geocode.Distance.MILES);

        animateAndDisableFab();
        final Runnable gatherer = new TweetProviderTask(mContext, geocode, mSearchBox.getSearchText(), this);
        mCurrentQueryExecutor = Executors.newSingleThreadScheduledExecutor();
        mCurrentQueryExecutor.scheduleAtFixedRate(gatherer, 0L, mContext.getResources().getInteger(R.integer.search_interval_seconds), TimeUnit.SECONDS);
    }

    private void animateAndDisableFab() {
        mRandomizeFab.setEnabled(Boolean.FALSE);
        final RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(mContext.getResources().getInteger(R.integer.rotate_fab_duration_millis));
        rotate.setRepeatCount(10);
        rotate.setRepeatMode(Animation.RESTART);
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {

            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                if (!mRandomizeFab.isEnabled())
                    mRandomizeFab.setEnabled(Boolean.TRUE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRandomizeFab.startAnimation(rotate);
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
    public synchronized void onTweetsProvided(final List<String> tweetIds, final String query) {
        final String currentSearchText;
        if (!TextUtils.isEmpty(query) && !TextUtils.isEmpty(currentSearchText = mSearchBox.getSearchText()) && query.toLowerCase(Locale.ENGLISH).contentEquals(currentSearchText.toLowerCase(Locale.ENGLISH))) {
            if (mTweetIdList == null)
                mTweetIdList = new ArrayList<>();
            mTweetIdList.addAll(tweetIds);
            if (!mTweetIdList.isEmpty())
                mTweetIdList.notify();
        }
    }

    @Override
    public void onFailedToProvideTweets(final TwitterException e) {
        Log.e("ERROR", e.getMessage());
    }

    private synchronized void reshowLastTweet() {
        final String defaultTweetId = mContext.getString(R.string.default_tweet_id);

        try {
            mNiceLoadTweetView.loadTweet(Long.parseLong(PreferenceAssistant.readSharedString(mContext, mContext.getString(R.string.pref_key_last_tweet_id), defaultTweetId)), mRandomizeFab);
        } catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException ex) {
            mNiceLoadTweetView.loadTweet(Long.parseLong(defaultTweetId), mRandomizeFab);
        }
    }

    private synchronized void showNextTweet() {
        if (mCurrentQueryExecutor == null)
            startResultGatheringPeriodicTask();

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(final Void... params) {
                synchronized (mTweetIdList) {
                    while (mTweetIdList.isEmpty())
                        try {
                            mTweetIdList.wait();
                        } catch (final InterruptedException ignored) {
                            Log.e("CONCURRENCY_ERROR", ignored.getMessage());
                        }
                }

                String tweetId;

                try {
                    mNiceLoadTweetView.loadTweet(Long.parseLong(tweetId = mTweetIdList.remove(0)), mRandomizeFab);
                } catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException ex) {
                    mNiceLoadTweetView.loadTweet(Long.parseLong(tweetId = mContext.getString(R.string.default_tweet_id)), mRandomizeFab);
                }
                return tweetId;
            }

            @Override
            protected void onPostExecute(final String tweetId) {
                PreferenceAssistant.writeSharedString(mContext, mContext.getString(R.string.pref_key_last_tweet_id), tweetId);
            }
        }.executeOnExecutor(Executors.newSingleThreadExecutor());
    }
}
