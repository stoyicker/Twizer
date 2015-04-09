package org.jorge.twizer.ui;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import org.jorge.twizer.R;

/**
 * @author stoyicker.
 */
public abstract class UiUtils {

    public static Integer getScreenWidth(final Context context) {
        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static Integer getScreenHeight(final Context context) {
        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setTaskDescription(final Activity activity) {
        final Context context = activity.getApplicationContext();

        activity.setTaskDescription(new ActivityManager.TaskDescription(context.getResources()
                .getString(R.string.app_name),
                BitmapFactory.decodeResource(context.getResources(), R.mipmap
                        .ic_launcher), context.getResources().getColor(R.color
                .material_light_blue_500)));
    }

    public static void circularRevealView(final Context context, final View viewToReveal) {
        final Integer cx = (viewToReveal.getLeft() + viewToReveal.getRight()) / 2,
                cy = (viewToReveal.getTop() + viewToReveal.getBottom()) / 2;
        final Animator animator = ViewAnimationUtils.createCircularReveal
                (viewToReveal, cx,
                        cy, 0, Math.max(viewToReveal.getWidth(),
                                viewToReveal.getHeight()));
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(context.getResources().getInteger(R.integer
                .circular_reveal_duration_millis));
        viewToReveal.setVisibility(View.VISIBLE);
        animator.start();
    }

    public static void circularHideView(final Context context, final View viewToHide) {
        final Integer cx = (viewToHide.getLeft() + viewToHide.getRight()) / 2,
                cy = (viewToHide.getTop() + viewToHide.getBottom()) / 2;
        final Animator animator = ViewAnimationUtils.createCircularReveal
                (viewToHide, cx,
                        cy, Math.max(viewToHide.getWidth(),
                                viewToHide.getHeight()), 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(context.getResources().getInteger(R.integer
                .circular_reveal_duration_millis));
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(final Animator animator) {

            }

            @Override
            public void onAnimationEnd(final Animator animator) {
                viewToHide.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(final Animator animator) {

            }

            @Override
            public void onAnimationRepeat(final Animator animator) {

            }
        });
        animator.start();
    }
}
