package org.jorge.twizer.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.jorge.twizer.ui.UiUtils;

/**
 * @author stoyicker.
 */
public abstract class CircularRevealedFragment extends Fragment {
    private Context mContext;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(final View v, final int left, final int top,
                                       final int right, final int bottom,
                                       final int oldLeft, final int oldTop, final int oldRight,
                                       final int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                UiUtils.circularRevealView(mContext, v);
            }

        });
    }
}
