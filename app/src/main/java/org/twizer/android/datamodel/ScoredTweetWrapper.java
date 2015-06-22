package org.twizer.android.datamodel;

import com.twitter.sdk.android.core.models.Tweet;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class ScoredTweetWrapper {

    private final Tweet mTweet;
    private final Long mScore;

    public ScoredTweetWrapper(final Tweet tweet, final Long score) {
        this.mTweet = tweet;
        this.mScore = score;
    }
}
