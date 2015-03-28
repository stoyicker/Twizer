package org.jorge.twizer; /**
 * @author stoyicker.
 */

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

@SuppressWarnings({"UnusedDeclaration"})
public abstract class DebugUtils {

    public static void showTrace(final String tag, final Exception source) {
        final StackTraceElement[] trace = source.getStackTrace();
        final StringBuilder toPrint = new StringBuilder("");
        for (StackTraceElement x : trace) {
            toPrint.append("Class ").append(x.getClassName()).append(" -  ").append(x
                    .getMethodName()).append(":").append(x.getLineNumber()).append("\n");
        }
        Log.d(tag, toPrint.toString());
    }

    public static void writeToFile(final String data, final Context context,
                                   final String fileName) {
        File f;
        if ((f = new File(fileName)).exists())
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        try {
            final OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(
                            context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Crashlytics.logException(e);
        }
    }

    public static void logArray(final String tag, final String arrayName, final Object[] array) {
        if (!BuildConfig.DEBUG) return;
        Log.d(tag, "Logging array " + arrayName);
        for (Object x : array)
            Log.d(tag, x + "\n");
    }

    public static void d(final String tag, final String msg) {
        if (!BuildConfig.DEBUG) return;
        Log.d(tag, msg);
    }

    /**
     * Show the activity over the lockscreen and wake up the device. If you launched the app
     * manually both of these conditions are already true. If you deployed from the IDE,
     * however, this will save you from hundreds of power button presses and pattern swiping per
     * day!
     * <p/>
     * Taken from https://gist.github.com/JakeWharton/f50f3b4d87e57d8e96e9
     */
    public static void autoUnlock(final Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        PowerManager power = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock lock =
                power.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager
                                .ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.ON_AFTER_RELEASE,
                        "Rise and shine!");
        lock.acquire();
        lock.release();
    }
}
