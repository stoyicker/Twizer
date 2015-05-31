package org.twizer.android.io.net.api.twitter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.twizer.android.R;
import org.twizer.android.datamodel.TwitterTrendRequest;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public abstract class TwitterApiClient {

    private static final Object LOCK = new Object();
    private static ITwitterApi apiService;

    public static ITwitterApi getContactApiClient(final Context context) {
        ITwitterApi ret = apiService;
        if (ret == null)
            synchronized (LOCK) {
                ret = apiService;
                if (ret == null) {
                    final RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(context.getString(R.string.twitter_api_endpoint)).setRequestInterceptor(request -> {
                        //TODO This
                        request.addHeader("Authorization", getToken());
                    }).build();
                    ret = restAdapter.create(ITwitterApi.class);
                    apiService = ret;
                }
            }

        return ret;
    }

    public interface ITwitterApi {

        /**
         * <a href="https://dev.twitter.com/rest/reference/get/trends/place">GET trends/place | Twitter Developers</a>
         *
         * @param id       {@link String} ({@link Long}) Required. Yahoo! WOEID for the place. 1
         *                 for
         *                 world
         *                 trends.
         * @param exclude  {@link String} Not required. Setting this equal to hashtags will remove all hashtags from the trends list.
         * @param callback {@link Callback<TwitterTrendRequest>} The callback to execute when the request is done. Happens on the UI thread.
         * @see org.twizer.android.datamodel.TwitterTrendRequest
         */
        @GET("/trends/place.json")
        void asyncGetTrendingTopics(@NonNull final String id, @Nullable final String exclude, @NonNull final Callback<TwitterTrendRequest> callback);
    }
}
