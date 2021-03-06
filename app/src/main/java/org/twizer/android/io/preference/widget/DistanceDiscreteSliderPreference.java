package org.twizer.android.io.preference.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.afollestad.materialdialogs.MaterialDialog;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.twizer.android.R;
import org.twizer.android.io.preference.PreferenceAssistant;

import java.util.Locale;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class DistanceDiscreteSliderPreference extends MaterialDialogPreference {

    private DiscreteSeekBar mSeekBar;

    public DistanceDiscreteSliderPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        updateIndicatorAndSummary(getContext(), null);
    }

    @Override
    protected void onClick() {
        super.onClick();
        showSliderDialog();
    }

    @SuppressLint("InflateParams")
    private void showSliderDialog() {
        mDialog = new MaterialDialog.Builder(getContext())
                .customView(mSeekBar = (DiscreteSeekBar) ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.widget_discrete_slider, null, Boolean.FALSE), Boolean.FALSE)
                .negativeText(android.R.string.cancel)
                .positiveText(android.R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final MaterialDialog dialog) {
                        PreferenceAssistant.writeSharedInteger(getContext(), DistanceDiscreteSliderPreference.super.getKey(), mSeekBar.getProgress());

                        updateIndicatorAndSummary(getContext(), null);
                    }
                })
                .autoDismiss(Boolean.TRUE).build();

        mSeekBar.setProgress(PreferenceAssistant.readSharedInteger(getContext(), DistanceDiscreteSliderPreference.super.getKey(), getContext().getResources().getInteger(R.integer.default_search_radius)));

        mDialog.show();
    }

    public void updateIndicatorAndSummary(final Context context, @Nullable final Integer newIndex) {
        final Integer index = newIndex == null ? Integer.parseInt(PreferenceAssistant.readSharedString(context, context.getString(R.string.pref_key_search_distance_unit), context.getResources().getString(R.string.default_search_distance_unit_value))) : newIndex;
        final String newFormatter = context.getResources().getStringArray(R.array.search_distance_unit_formatters)[index];

        this.setSummary(String.format(Locale.ENGLISH, newFormatter, PreferenceAssistant.readSharedInteger(context, context.getString(R.string.pref_key_search_radius), context.getResources().getInteger(R.integer.search_radius_lower_cap))));
        if (mSeekBar != null)
            mSeekBar.setIndicatorFormatter(newFormatter);
    }
}
