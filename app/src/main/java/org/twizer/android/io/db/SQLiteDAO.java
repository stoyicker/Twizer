package org.twizer.android.io.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.models.HashtagEntity;

import org.jetbrains.annotations.Contract;
import org.twizer.android.BuildConfig;
import org.twizer.android.R;
import org.twizer.android.io.db.base.RobustSQLiteOpenHelper;
import org.twizer.android.ui.UiUtils;

import java.util.Locale;

/**
 * DAO. It enforces all database operations to be performed on a background thread.
 *
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class SQLiteDAO extends RobustSQLiteOpenHelper {

    public static final Object DB_LOCK = new Object();
    private static final String TOP_HASHTAGS_TABLE_NAME_PATTERN = "%s__TABLE_TOP_HASHTAGS",
            TOP_USERNAMES_TABLE_NAME_PATTERN = "%s__TABLE_TOP_USERNAMES";
    private static final String TABLE_KEY_HASHTAG = "TABLE_KEY_HASHTAG";
    private static final String TABLE_KEY_USERNAME = "TABLE_KEY_USERNAME";
    private static Context mContext;
    private static SQLiteDAO singleton;

    /**
     * Standard constructor.
     *
     * @param _context {@link Context} Context.
     */
    private SQLiteDAO(@NonNull Context _context) {
        super(_context, _context.getString(R.string.database_name), null, BuildConfig.VERSION_CODE);
        mContext = _context;
    }

    /**
     * Initialization method. It takes care of constructing the singleton and calling it more
     * than once will have no effect. It must be invoked prior to any calls to {@link SQLiteDAO#getInstance()}
     * as the later does not have the ability to instantiate the singleton. The reason for this
     * is that constructing the database object requires a context which, if set as a parameter
     * in {@link SQLiteDAO#getInstance()}, would get passed around every time a database
     * operation needs to be performed, but would only be used the first time.
     *
     * @param _context {@link Context} Context
     */
    public synchronized static void setup(@NonNull final Context _context) {
        if (singleton == null) {
            singleton = new SQLiteDAO(_context);
            mContext = _context;
        }
    }

    private String getTopHashtagsTableName() {
        return String.format(Locale.ENGLISH, TOP_HASHTAGS_TABLE_NAME_PATTERN, Twitter.getSessionManager()
                .getActiveSession().getUserName());
    }

    private String getTopUsersTableName() {
        return String.format(Locale.ENGLISH, TOP_USERNAMES_TABLE_NAME_PATTERN, Twitter.getSessionManager
                ().getActiveSession().getUserName());
    }

    /**
     * Retrieves the singleton instance
     *
     * @return {@link SQLiteDAO} The singleton instance to use when performing database operations
     * @see SQLiteDAO#setup(Context)
     */
    public synchronized static SQLiteDAO getInstance() {
        if (UiUtils.isMainThread()) {
            throw new DatabaseOnMainThreadException(mContext);
        }
        if (singleton == null)
            throw new IllegalStateException("SQLiteDAO.setup(Context) must be called before trying to retrieve the instance.");
        return singleton;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRobustUpgrade(final SQLiteDatabase db, final int oldVersion,
                                final int newVersion) throws SQLiteException {
        //For now unused as there is no older version from the database yet
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final SQLiteDatabase db) {
        super.onCreate(db);

        final String topHashtagsTableName = getTopHashtagsTableName(), topUsersTableName =
                getTopUsersTableName();
        final String createTopHashtagsTableCommand = "CREATE TABLE IF NOT EXISTS " + topHashtagsTableName + " ( " +
                TABLE_KEY_HASHTAG + " TEXT PRIMARY KEY ON CONFLICT IGNORE " +
                " )";
        final String createTopUserNamesTableCommand = "CREATE TABLE IF NOT EXISTS " + topUsersTableName + " ( " +
                TABLE_KEY_USERNAME + " TEXT PRIMARY KEY ON CONFLICT IGNORE " +
                " )";

        synchronized (DB_LOCK) {
            db.execSQL(createTopHashtagsTableCommand);
            db.execSQL(createTopUserNamesTableCommand);
            RobustSQLiteOpenHelper.addTableName(topHashtagsTableName);
            RobustSQLiteOpenHelper.addTableName(topUsersTableName);
        }
    }

    /**
     * Escapes a string.
     *
     * @param input {@link String} The string to escape.
     * @return {@link String} The escaped string.
     */
    @Contract("null -> null")
    private String escapeString(final String input) {
        if (input == null)
            return null;

        return "'" + input.replace("'", "''") + "'";
    }

    private ContentValues mapHashtagToStorable(@NonNull final HashtagEntity hashtag) {
        final ContentValues ret = new ContentValues();
        ret.put(TABLE_KEY_HASHTAG, escapeString(hashtag.text));
        return ret;
    }

    public Boolean insertHashtag(@NonNull final HashtagEntity hashtag) {
        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues storableHashtag = mapHashtagToStorable(hashtag);

        Boolean inserted;

        synchronized (DB_LOCK) {
            db.beginTransaction();
            inserted = db.insert(getTopHashtagsTableName(), null, storableHashtag) != -1;
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return inserted;
    }

    public Boolean containsHashtag(@NonNull final HashtagEntity hashtag) {
        final SQLiteDatabase db = getReadableDatabase();

        Boolean found;

        final Cursor c = db.query(Boolean.TRUE, getTopHashtagsTableName(), new
                String[]{TABLE_KEY_HASHTAG}, TABLE_KEY_HASHTAG + " = ?", new String[]{escapeString
                (hashtag.text)}, null, null, null, null);
        found = c.getCount() > 0;
        c.close();

        return found;
    }

    private ContentValues mapUsernameToStorable(@NonNull final String username) {
        final ContentValues ret = new ContentValues();
        ret.put(TABLE_KEY_USERNAME, escapeString(username));
        return ret;
    }

    public Boolean insertUsername(@NonNull final String username) {
        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues storableUsername = mapUsernameToStorable(username);

        Boolean inserted;

        synchronized (DB_LOCK) {
            db.beginTransaction();
            inserted = db.insert(getTopUsersTableName(), null, storableUsername) != -1;
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return inserted;
    }

    public Boolean containsUsername(@NonNull final String username) {
        final SQLiteDatabase db = getReadableDatabase();

        Boolean found;

        final Cursor c = db.query(Boolean.TRUE, getTopUsersTableName(), new
                String[]{TABLE_KEY_USERNAME}, TABLE_KEY_USERNAME + " = ?", new String[]{escapeString
                (username)}, null, null, null, null);
        found = c.getCount() > 0;
        c.close();

        return found;
    }

    /**
     * Simple exception used on {@link SQLiteDAO#getInstance()} to avoid execution of database
     * operations on the UI thread.
     */
    private static class DatabaseOnMainThreadException extends RuntimeException {

        private DatabaseOnMainThreadException(final Context context) {
            super(context.getString(R.string.error_database_operation_on_main_thread));
        }
    }
}