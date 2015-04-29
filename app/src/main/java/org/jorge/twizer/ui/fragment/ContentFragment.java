package org.jorge.twizer.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quinny898.library.persistentsearch.SearchBox;

import org.jorge.twizer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author stoyicker.
 */
public class ContentFragment extends Fragment {

    @InjectView(R.id.search_box)
    SearchBox mSearchBox;

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

        mSearchBox.enableVoiceRecognition(this);
    }
}
