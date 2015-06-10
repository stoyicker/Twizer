package org.twizer.android.io.net.provider.twitter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.services.params.Geocode;

import org.twizer.android.R;
import org.twizer.android.io.net.api.twitter.TwitterOAuthorizedApiClient;

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
     * @param count    {@link Integer} When null, defaults to 15 (Twitter-established default limit).
     * @param callback {@link org.twizer.android.io.net.provider.twitter.TweetProvider.ITweetReceiver} The response handler.
     */
    public static void getTweets(@NonNull final Context context, @Nullable final Geocode geocode, final String query, @Nullable final Integer count, @NonNull final ITweetReceiver callback) {
        TwitterOAuthorizedApiClient.getInstance().getSearchService().tweets(query, geocode, null, null, context.getString(R.string.tweet_search_result_type_popular), count, null, null, null, null, new Callback<Search>() {
            @Override
            public void success(final Result<Search> result) {
                //TODO Implement the success case and call the callback back
            }

            @Override
            public void failure(final TwitterException e) {
                callback.onFailedToProvideTweets(e);
            }
        });
    }

    public interface ITweetReceiver {

        void onTweetsProvided(final List<String> tweetIds);

        void onFailedToProvideTweets(final TwitterException e);
    }
}
