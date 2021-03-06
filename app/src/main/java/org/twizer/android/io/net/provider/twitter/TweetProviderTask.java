package org.twizer.android.io.net.provider.twitter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.params.Geocode;

import org.twizer.android.R;
import org.twizer.android.datamodel.ScoredTweetWrapper;
import org.twizer.android.io.net.api.twitter.TwitterTrendServiceExtensionApiClient;
import org.twizer.android.io.preference.PreferenceAssistant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class TweetProviderTask implements Runnable {

    private final Context mContext;
    private final Geocode mGeocode;
    private final String mQuery;
    private final ITweetReceiver mCallback;

    public TweetProviderTask(@NonNull final Context mContext, @Nullable final Geocode mGeocode, @NonNull final String mQuery, @NonNull final ITweetReceiver mCallback) {
        this.mContext = mContext;
        this.mGeocode = mGeocode;
        this.mQuery = mQuery;
        this.mCallback = mCallback;
    }

    /**
     * Simplifies usage of <a href="https://dev.twitter.com/rest/reference/get/search/tweets">GET search/tweets | Twitter Developers</a>
     *
     * @param context  {@link Context} The context.
     * @param geocode  {@link Geocode} When null, the location is not taken into account.
     * @param query    {@link String} Query to run.
     * @param callback {@link TweetProviderTask.ITweetReceiver} The data handler.
     */
    private static void getTweets(@NonNull final Context context, @Nullable final Geocode geocode, final String query, @NonNull final ITweetReceiver callback) {
        final Integer count = context.getResources().getInteger(R.integer
                .max_tweets_per_batch_pre_filter);

        Long sinceId;
        try {
            sinceId = Long.parseLong(PreferenceAssistant.readSharedString(context, context
                    .getString(R
                            .string.pref_key_max_tweet_id), context.getString(R.string
                    .default_max_tweet_id)));
        } catch (NumberFormatException ex) {
            sinceId = Long.parseLong(context.getString(R.string.default_max_tweet_id));
        }

        sinceId++;

        final Long finalSinceId = sinceId;
        TwitterTrendServiceExtensionApiClient.getInstance().getSearchService().tweets(query, geocode, null, null, context.getString(R.string.tweet_search_result_type_mixed), count, null, sinceId, null, null, new Callback<Search>() {
            @Override
            public void success(final Result<Search> result) {
                new AsyncTask<Void, Void, List<ScoredTweetWrapper>>() {
                    @Override
                    protected List<ScoredTweetWrapper> doInBackground(final Void... params) {
                        final List<Tweet> tweetList = result.data.tweets;
                        final List<ScoredTweetWrapper> scoredTweetList = new ArrayList<>();
                        for (final Tweet x : tweetList)
                            scoredTweetList.add(new ScoredTweetWrapper(x));
                        return scoredTweetList;
                    }

                    @Override
                    protected void onPostExecute(final List<ScoredTweetWrapper> scoredTweetList) {
                        if (scoredTweetList.isEmpty())
                            failure((TwitterException) null);

                        Collections.sort(scoredTweetList, new Comparator<ScoredTweetWrapper>() {
                            @Override
                            public int compare(final @NonNull ScoredTweetWrapper lhs, final @NonNull ScoredTweetWrapper rhs) {
                                return rhs.getScore() - lhs.getScore();
                            }
                        });

                        List<String> idList = new ArrayList<>();
                        for (final ScoredTweetWrapper x : scoredTweetList)
                            idList.add(x.getTweetIdStr());

                        final Integer l = context.getResources().getInteger(R.integer
                                .max_tweets_per_batch_post_filter);
                        if (idList.size() > l)
                            idList = idList.subList(0, l);

                        PreferenceAssistant.writeSharedString(context, context.getString(R.string
                                .pref_key_max_tweet_id), String.valueOf(Math.max(finalSinceId, Long.parseLong
                                (getMaxTweetId(context, idList)))));

                        callback.onTweetsProvided(idList, query);
                    }
                }.executeOnExecutor(Executors.newSingleThreadExecutor());
            }

            @Override
            public void failure(final TwitterException e) {
                callback.onFailedToProvideTweets(e);
            }
        });
    }

    private static String getMaxTweetId(final Context context, final List<String> idList) {
        String maxId = context.getString(R.string.default_max_tweet_id);
        for (final String id : idList) {
            if (Long.parseLong(id) > Long.parseLong(maxId))
                maxId = id;
        }

        return maxId;
    }

    @Override
    public void run() {
        getTweets(mContext, mGeocode, mQuery, mCallback);
    }

    public interface ITweetReceiver {

        void onTweetsProvided(final List<String> tweetIds, final String initialQuery);

        void onFailedToProvideTweets(final TwitterException e);
    }
}
