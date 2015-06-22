package org.twizer.android.io.db.base;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * Adapted from ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * This is an example of base class to use when designing an app that is intended to be
 * maintained and therefore updates to the database schema are likely to happen.
 */
public abstract class RobustSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final List<String> mTableNames = new LinkedList<>();


    /**
     * Standard constructor.
     *
     * @param context {@link Context} Context.
     * @param name    {@link String} Database name
     * @param factory {@link CursorFactory} Custom cursor factory, if other than the default one.
     * @param version {@link Integer} Database version number.
     */
    public RobustSQLiteOpenHelper(final Context context, final String name,
                                  final CursorFactory factory, final Integer version) {
        super(context, name, factory, version);
    }

    /**
     * Adds a table name to the set of table names kept during runtime should they be needed
     *
     * @param tableName {@link String} The name of the table to add
     */
    protected static void addTableName(final String tableName) {
        mTableNames.add(tableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final SQLiteDatabase db) {
        dropAllTables(db);
    }

    /**
     * Defers the upgrade logic to an alternative method that is enforced to be implemented by
     * subclasses and, upon SQL errors, tries to heal the database.
     *
     * @see SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)
     */
    @Override
    public final void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        try {
            onRobustUpgrade(db, oldVersion, newVersion);
        } catch (SQLiteException e) {
            // The database has entered an unknown state. Try to recover.
            try {
                regenerateTables(db);
            } catch (SQLiteException e2) {
                dropAndCreateTables(db);
            }
        }
    }

    /**
     * Enforces a pattern to handle database upgrades in a safer way.
     *
     * @param db         {@link SQLiteDatabase} The database object
     * @param oldVersion {@link Integer} Old database version number
     * @param newVersion {@link Integer} New database version number
     * @throws SQLiteException To allow subclasses to report exceptions
     * @see RobustSQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)
     */
    public abstract void onRobustUpgrade(final SQLiteDatabase db, final int oldVersion,
                                         final int newVersion) throws SQLiteException;

    /**
     * Gracefully archives the currently existing tables and create new ones accordingly
     *
     * @param db {@link SQLiteDatabase} The database object
     */
    private void regenerateTables(final SQLiteDatabase db) {
        dropAllTablesWithPrefix(db, "OLD_");

        for (final String tableName : mTableNames)
            db.execSQL("ALTER TABLE " + tableName + " RENAME TO OLD_"
                    + tableName);

        onCreate(db);

        for (final String tableName : mTableNames)
            repopulateTable(db, tableName);

        dropAllTablesWithPrefix(db, "OLD_");
    }

    /**
     * Populates the new version of a table with the contents of its corresponding archived one
     *
     * @param db        {@link SQLiteDatabase} The database object
     * @param tableName {@link String} The table name
     */
    private void repopulateTable(final SQLiteDatabase db, final String tableName) {
        final String columns = getTableColumnNames(db, tableName);

        final String sql = "INSERT INTO " + tableName + " (" + columns + ") SELECT " + columns +
                " FROM" +
                " OLD_" + tableName;
        db.execSQL(sql);
    }

    /**
     * Retrieves the names of the columns for a given table
     *
     * @param db        {@link SQLiteDatabase} The database object
     * @param tableName {@link String} The table name
     * @return {@link String} A CSV string with the names of the columns in the given table
     */
    private String getTableColumnNames(final SQLiteDatabase db, final String tableName) {
        final StringBuilder sb = new StringBuilder();

        final Cursor fields = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        while (fields.moveToNext()) {
            if (!fields.isFirst())
                sb.append(", ");
            sb.append(fields.getString(1));
        }
        fields.close();

        return sb.toString();
    }

    /**
     * Deletes all tables in both runtime memory and the database and restarts the database
     * creation process
     *
     * @param db {@link SQLiteDatabase} The database object
     */
    private void dropAndCreateTables(final SQLiteDatabase db) {
        dropAllTables(db);
        onCreate(db);
    }

    /**
     * Deletes the tables that begin with the provided prefix from both runtime memory and the
     * database
     *
     * @param db     {@link SQLiteDatabase} The database object
     * @param prefix {@link String} The prefix to select the tables that should be dropped
     */
    private void dropAllTablesWithPrefix(final SQLiteDatabase db, final String prefix) {
        final List<String> deletedTables = new LinkedList<>();
        for (String tableName : mTableNames) {
            db.execSQL("DROP TABLE IF EXISTS " + prefix + tableName);
            deletedTables.add(prefix + tableName);
        }
        for (String tableName : deletedTables)
            mTableNames.remove(tableName);
    }

    /**
     * Shorthand to drop all tables. Same effect as {@link #dropAllTablesWithPrefix
     * (SQLiteDatabase, String)} with an empty string as prefix.
     *
     * @param db {@link SQLiteDatabase} The database object
     */
    private void dropAllTables(final SQLiteDatabase db) {
        dropAllTablesWithPrefix(db, "");
    }
}
