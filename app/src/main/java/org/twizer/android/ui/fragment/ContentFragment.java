package org.twizer.android.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.twizer.android.R;
import org.twizer.android.io.prefs.PreferenceAssistant;
import org.twizer.android.ui.widget.NiceLoadTweetView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author stoyicker.
 */
public final class ContentFragment extends Fragment {

    private static final String KEY_IS_SEARCH_BOX_OPEN = "IS_SEARCH_BOX_OPEN";

    @InjectView(R.id.searchBox)
    SearchBox mSearchBox;

    @InjectView(R.id.tweetContainer)
    ViewGroup mTweetContainer;

    @InjectView(R.id.niceLoadTweetView)
    NiceLoadTweetView mNiceLoadTweetView;

    Context mContext;
    private Boolean mWasSearchBoxOpen;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_content, container, Boolean.FALSE);
        ButterKnife.inject(this, view);

        view.post(() -> {
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTweetContainer.getLayoutParams();
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + mSearchBox.getHeight(), layoutParams.rightMargin, layoutParams.bottomMargin);
        });

        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mWasSearchBoxOpen = savedInstanceState.getBoolean(KEY_IS_SEARCH_BOX_OPEN);
        }

        initSearchBox(mSearchBox);
        loadInitialTweet(mNiceLoadTweetView);
    }

    private void loadInitialTweet(final NiceLoadTweetView niceLoadTweetView) {
        final Long tweetId = PreferenceAssistant.readSharedLong(mContext, PreferenceAssistant.PREF_LAST_TWEET_ID, -1L);

        niceLoadTweetView.loadTweet(tweetId);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == Activity.RESULT_OK) {
            final ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mSearchBox.populateEditText(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initSearchBox(final SearchBox searchBox) {
        initSearchVoiceRecognition(searchBox);
        initSearchables(searchBox);
        initSearchBoxVisibility(searchBox);
    }

    private void initSearchBoxVisibility(final SearchBox searchBox) {
        if (mWasSearchBoxOpen != null)
            searchBox.post(() -> {
                if (mWasSearchBoxOpen)
                    searchBox.openSearch(Boolean.FALSE);
                else
                    searchBox.mockSearch();
            });
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_IS_SEARCH_BOX_OPEN, mSearchBox.isOpen());
    }

    private void initSearchVoiceRecognition(final SearchBox searchBox) {
        searchBox.enableVoiceRecognition(this);
    }

    private void initSearchables(final SearchBox searchBox) {
        //TODO Replace this by the trending topics retrieved from the API
        final Drawable resultDrawable = mContext.getDrawable(R.drawable.ic_search_suggestion);
        searchBox.addSearchable(new SearchResult("1", resultDrawable));
        searchBox.addSearchable(new SearchResult("2", resultDrawable));
        searchBox.addSearchable(new SearchResult("3", resultDrawable));
        searchBox.addSearchable(new SearchResult("4", resultDrawable));
        searchBox.addSearchable(new SearchResult("5", resultDrawable));
        searchBox.addSearchable(new SearchResult("16", resultDrawable));
        searchBox.addSearchable(new SearchResult("7", resultDrawable));
        searchBox.addSearchable(new SearchResult("8", resultDrawable));
        searchBox.addSearchable(new SearchResult("9", resultDrawable));
        searchBox.addSearchable(new SearchResult("10", resultDrawable));
    }
}