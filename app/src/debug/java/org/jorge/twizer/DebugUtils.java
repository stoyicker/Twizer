package org.jorge.twizer; /**
 * @author stoyicker.
 */

import android.content.Context;
import android.util.Log;

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

    public static void e(final String tag, final String msg) {
        if (!BuildConfig.DEBUG) return;
        Log.e(tag, msg);
    }
}
