package org.twizer.android.io.net.api.twitter.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.twizer.android.datamodel.api.twitter.TrendResult;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface TrendService {

    /**
     * <a href="https://dev.twitter.com/rest/reference/get/trends/place">GET trends/place | Twitter Developers</a>
     *
     * @param id       {@link String} ({@link Long}) Required. Yahoo! WOEID for the place. 1
     *                 for world trends.
     * @param exclude  {@link String} Not required. Setting this equal to hashtags will remove all hashtags from the trends list.
     * @param callback {@link Callback <  TrendResultWrapper  >} The callback to execute when the request is done. Happens on the UI thread.
     * @see TrendResult
     */
    @GET("/1.1/trends/place.json")
    void asyncGetTrendingTopics(@Query("id") @NonNull final String id, @Query("exclude") @Nullable final String exclude, @NonNull final Callback<List<TrendResult>> callback);
}
