package org.jorge.twizer.ui.fragment;

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

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.jorge.twizer.R;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author stoyicker.
 */
public class ContentFragment extends Fragment {

    @InjectView(R.id.search_box)
    SearchBox mSearchBox;

    Context mContext;

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

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initSearchBox();
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

    private void initSearchBox() {
        initSearchVoiceRecognition();
        initSearchables();
    }

    private void initSearchVoiceRecognition() {
        mSearchBox.enableVoiceRecognition(this);
    }

    private void initSearchables() {
        //TODO Replace this by actual logic
        final Drawable resultDrawable = mContext.getDrawable(R.drawable.ic_search_suggestion);
        mSearchBox.addSearchable(new SearchResult("1", resultDrawable));
        mSearchBox.addSearchable(new SearchResult("2", resultDrawable));
        mSearchBox.addSearchable(new SearchResult("3", resultDrawable));
        mSearchBox.addSearchable(new SearchResult("4", resultDrawable));
        mSearchBox.addSearchable(new SearchResult("5", resultDrawable));
        mSearchBox.addSearchable(new SearchResult("16", resultDrawable));
        mSearchBox.addSearchable(new SearchResult("7", resultDrawable));
        mSearchBox.addSearchable(new SearchResult("8", resultDrawable));
        mSearchBox.addSearchable(new SearchResult("9", resultDrawable));
        mSearchBox.addSearchable(new SearchResult("10", resultDrawable));
    }
}
