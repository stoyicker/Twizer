package org.jorge.twizer.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import org.jorge.twizer.DebugUtils;
import org.jorge.twizer.R;

/**
 * @author stoyicker.
 */
public abstract class UiUtils {

    public static Integer getScreenOrientation(final Context context) {
        final WindowManager winMan = (WindowManager) context.getSystemService(Activity
                .WINDOW_SERVICE);
        final Integer rotation = winMan.getDefaultDisplay().getRotation();
        final DisplayMetrics dm = new DisplayMetrics();
        winMan.getDefaultDisplay().getMetrics(dm);
        final Integer width = dm.widthPixels;
        final Integer height = dm.heightPixels;
        final Integer orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    DebugUtils.e("debug", "Unknown screen orientation. Defaulting to portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    DebugUtils.e("debug", "Unknown screen orientation. Defaulting to landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

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
}
