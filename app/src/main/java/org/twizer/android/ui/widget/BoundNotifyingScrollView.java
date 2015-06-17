package org.twizer.android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class BoundNotifyingScrollView extends ScrollView {

    private Boolean isScrollable, isBoundReached = Boolean.FALSE;
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
            if (isBoundReached && y != oldy) {
                isBoundReached = Boolean.FALSE;
                mListener.onBoundAbandoned();
            }
            if (!this.canScrollVertically(1) && this.canScrollVertically(-1)) {
                isBoundReached = Boolean.TRUE;
                mListener.onBottomBoundReached();
            } else if (!this.canScrollVertically(-1) && this.canScrollVertically(1)) {
                isBoundReached = Boolean.TRUE;
                mListener.onTopBoundReached();
            }
        }
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
