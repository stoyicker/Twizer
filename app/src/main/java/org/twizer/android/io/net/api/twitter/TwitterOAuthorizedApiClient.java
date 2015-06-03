package org.twizer.android.io.net.api.twitter;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;

import org.twizer.android.io.net.api.twitter.service.TrendService;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class TwitterOAuthorizedApiClient extends TwitterApiClient {

    private static final Object LOCK = new Object();
    private static volatile TwitterOAuthorizedApiClient mInstance;

    public static TwitterOAuthorizedApiClient getInstance() {
        TwitterOAuthorizedApiClient ret = mInstance;
        if (mInstance == null)
            synchronized (LOCK) {
                if (mInstance == null) {
                    ret = new TwitterOAuthorizedApiClient();
                    mInstance = ret;
                }
            }

        return ret;
    }

    private TwitterOAuthorizedApiClient() {
        super(Twitter.getSessionManager().getActiveSession());
    }

    public TrendService getTrendService() {
        return getService(TrendService.class);
    }
}


