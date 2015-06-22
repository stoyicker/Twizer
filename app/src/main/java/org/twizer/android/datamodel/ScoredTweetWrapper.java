package org.twizer.android.datamodel;

import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.MentionEntity;
import com.twitter.sdk.android.core.models.Tweet;

import org.twizer.android.io.db.SQLiteDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class ScoredTweetWrapper {

    private static final Integer POINTS_PER_USER_MATCH = 5;
    private static final Integer POINTS_PER_HASHTAG_MATCH = 1;

    private final Tweet mTweet;
    private final Integer mScore;

    public ScoredTweetWrapper(final Tweet tweet) {
        this.mTweet = tweet;
        this.mScore = calculateScore();
    }

    private Integer calculateScore() {
        Integer currentSum = 0;

        final List<HashtagEntity> hashtags = mTweet.entities.hashtags;
        final List<String> usernames = new ArrayList<>();
        usernames.add(mTweet.user.screenName);
        final List<MentionEntity> mentionEntities = mTweet.entities.userMentions;
        for (final MentionEntity x : mentionEntities)
            usernames.add(x.screenName);

        final SQLiteDAO sqliteDaoInstance = SQLiteDAO.getInstance();

        for (final HashtagEntity hashtag : hashtags)
            if (sqliteDaoInstance.containsHashtag(hashtag))
                currentSum += POINTS_PER_HASHTAG_MATCH;

        for (final String username : usernames) {
            if (sqliteDaoInstance.containsUsername(username))
                currentSum += POINTS_PER_USER_MATCH;
        }

        return currentSum;
    }

    public final String getTweetIdStr() {
        return mTweet.idStr;
    }

    public final Integer getScore() {
        return mScore;
    }
}
