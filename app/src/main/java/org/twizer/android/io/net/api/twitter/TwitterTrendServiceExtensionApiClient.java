package org.twizer.android.io.net.api.twitter;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;

import org.twizer.android.io.net.api.twitter.service.TrendService;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class TwitterTrendServiceExtensionApiClient extends TwitterApiClient {

    private static final Object LOCK = new Object();
    private static volatile TwitterTrendServiceExtensionApiClient mInstance;

    public static TwitterTrendServiceExtensionApiClient getInstance() {
        TwitterTrendServiceExtensionApiClient ret = mInstance;
        if (mInstance == null)
            synchronized (LOCK) {
                if (mInstance == null) {
                    ret = new TwitterTrendServiceExtensionApiClient();
                    mInstance = ret;
                }
            }

        return ret;
    }

    private TwitterTrendServiceExtensionApiClient() {
        super(Twitter.getSessionManager().getActiveSession());
    }

    public TrendService getTrendService() {
        return getService(TrendService.class);
    }
}


