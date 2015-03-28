package org.jorge.twizer.io.files;

import java.io.File;

/**
 * @author stoyicker.
 */
public abstract class FileOperations {

    public static Boolean recursiveDelete(final File target) {
        if (target == null || !target.exists()) return Boolean.FALSE;
        if (target.isDirectory()) {
            String[] children = target.list();
            for (String aChildren : children) {
                Boolean success = recursiveDelete(new File(target, aChildren));
                if (!success) {
                    return Boolean.FALSE;
                }
            }
        }

        return target.delete();
    }
}
