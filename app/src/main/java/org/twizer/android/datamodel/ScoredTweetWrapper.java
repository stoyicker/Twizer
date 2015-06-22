package org.twizer.android.datamodel;

import android.os.AsyncTask;

import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.MentionEntity;
import com.twitter.sdk.android.core.models.Tweet;

import org.twizer.android.io.db.SQLiteDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class ScoredTweetWrapper {

    private static final Integer POINTS_PER_USER_MATCH = 5;
    private static final Integer POINTS_PER_HASHTAG_MATCH = 1;

    private final Tweet mTweet;
    private Integer mScore = -1;

    public ScoredTweetWrapper(final Tweet tweet) {
        this.mTweet = tweet;
        calculateScore();
    }

    private void calculateScore() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(final Void... params) {
                Integer currentSum = 0;

                final List<String> usernames = new ArrayList<>();
                usernames.add(mTweet.user.screenName);
                final List<MentionEntity> mentionEntities = mTweet.entities.userMentions;
                if (mentionEntities != null)
                    for (final MentionEntity x : mentionEntities)
                        usernames.add(x.screenName);

                final SQLiteDAO sqliteDaoInstance = SQLiteDAO.getInstance();

                for (final String username : usernames) {
                    if (sqliteDaoInstance.containsUsername(username))
                        currentSum += POINTS_PER_USER_MATCH;
                }

                final List<HashtagEntity> hashtags = mTweet.entities.hashtags;
                if (hashtags != null)
                    for (final HashtagEntity hashtag : hashtags)
                        if (sqliteDaoInstance.containsHashtag(hashtag))
                            currentSum += POINTS_PER_HASHTAG_MATCH;

                return currentSum;
            }

            @Override
            protected void onPostExecute(final Integer sum) {
                ScoredTweetWrapper.this.setScore(sum);
            }
        }.executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    public final String getTweetIdStr() {
        return mTweet.idStr;
    }

    public final Integer getScore() {
        return mScore;
    }

    public void setScore(final Integer score) {
        this.mScore = score;
    }
}
