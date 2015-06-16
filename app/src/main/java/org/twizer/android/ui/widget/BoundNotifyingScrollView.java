package org.twizer.android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class BoundNotifyingScrollView extends ScrollView {

    private Boolean isScrollable;
    private IScrollBoundNotificationListener mListener;

    public BoundNotifyingScrollView(final Context context) {
        super(context);
    }

    public BoundNotifyingScrollView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public BoundNotifyingScrollView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(final int x, final int y, final int oldx, final int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (isScrollable == null)
            isScrollable = calculateIsScrollable();
        if (isScrollable) {
            if (!this.canScrollVertically(1)) {
                mListener.onBottomBoundReached();
            } else if (!this.canScrollVertically(-1)) {
                mListener.onTopBoundReached();
            }
            return;
        }
        mListener.onBoundAbandoned();
    }

    public void setBoundNotificationListener(final IScrollBoundNotificationListener listener) {
        mListener = listener;
    }

    private Boolean calculateIsScrollable() {
        final Integer childHeight = this.getChildAt(0).getHeight();
        return this.getHeight() < childHeight + this.getPaddingTop() + this.getPaddingBottom();
    }

    public interface IScrollBoundNotificationListener {

        void onTopBoundReached();

        void onBottomBoundReached();

        void onBoundAbandoned();
    }
}
