package org.twizer.android.io.net.provider.twitter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.params.Geocode;

import org.twizer.android.R;
import org.twizer.android.io.net.api.twitter.TwitterTrendServiceExtensionApiClient;
import org.twizer.android.io.preference.PreferenceAssistant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public abstract class TweetProvider {

    /**
     * Simplifies usage of <a href="https://dev.twitter.com/rest/reference/get/search/tweets">GET search/tweets | Twitter Developers</a>
     *
     * @param context  {@link Context} The context.
     * @param geocode  {@link Geocode} When null, the location is not taken into account.
     * @param query    {@link String} Query to run.
     * @param callback {@link org.twizer.android.io.net.provider.twitter.TweetProvider.ITweetReceiver} The data handler.
     */
    public static void getTweets(@NonNull final Context context, @Nullable final Geocode geocode, final String query, @NonNull final ITweetReceiver callback) {
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

        TwitterTrendServiceExtensionApiClient.getInstance().getSearchService().tweets(query, geocode, null, null, context.getString(R.string.tweet_search_result_type_popular), count, null, sinceId, null, null, new Callback<Search>() {
            @Override
            public void success(final Result<Search> result) {
                final List<Tweet> tweetList = result.data.tweets;
                List<String> idList = new ArrayList<>();
                for (final Tweet x : tweetList)
                    idList.add(x.idStr);

                //TODO Sort

                final Integer l = context.getResources().getInteger(R.integer
                        .max_tweets_per_batch_post_filter);
                if (idList.size() > l)
                    idList = idList.subList(0, l);

                PreferenceAssistant.writeSharedString(context, context.getString(R.string
                        .pref_key_max_tweet_id), getMaxTweetId(context, idList));

                callback.onTweetsProvided(idList);
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

    public interface ITweetReceiver {

        void onTweetsProvided(final List<String> tweetIds);

        void onFailedToProvideTweets(final TwitterException e);
    }
}
