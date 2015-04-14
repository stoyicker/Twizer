package org.jorge.twizer.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jorge.twizer.R;

/**
 * @author stoyicker.
 */
public class ContentFragment extends CircularRevealedFragment {

    private static volatile Fragment mInstance;
    private static final Object LOCK = new Object();

    public static Fragment getInstance(final Context context) {
        Fragment ret = mInstance;
        if (ret == null) {
            synchronized (LOCK) {
                ret = mInstance;
                if (ret == null) {
                    ret = Fragment.instantiate(context, ContentFragment.class.getName());
                    mInstance = ret;
                }
            }
        }
        return ret;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content, container, Boolean.FALSE);
    }

}