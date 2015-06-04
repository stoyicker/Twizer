package org.twizer.android.io.preference.widget;

import android.app.Dialog;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public abstract class MaterialDialogPreference extends Preference {
    protected Dialog mDialog;

    public MaterialDialogPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void dismissDialog() {
        if (mDialog != null)
            mDialog.dismiss();
    }
}
