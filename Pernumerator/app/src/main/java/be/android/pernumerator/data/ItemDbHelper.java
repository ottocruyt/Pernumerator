/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.android.pernumerator.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper for Pernumerator app. Manages database creation and version management.
 */
public class ItemDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ItemDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "pernumerator.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ItemDbHelper}.
     *
     * @param context of the app
     */
    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the be.android.pernumerator table
        String SQL_CREATE_ITEMS_TABLE =  "CREATE TABLE " + ItemContract.ItemEntry.TABLE_NAME + " ("
                + ItemContract.ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemContract.ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + ItemContract.ItemEntry.COLUMN_ITEM_TYPE + " TEXT, "
                + ItemContract.ItemEntry.COLUMN_ITEM_OWNER + " INTEGER NOT NULL, "
                + ItemContract.ItemEntry.COLUMN_ITEM_WEIGHT + " FLOAT NOT NULL DEFAULT 0, "
                + ItemContract.ItemEntry.COLUMN_ITEM_PRICE + " FLOAT NOT NULL DEFAULT 0, "
                + ItemContract.ItemEntry.COLUMN_ITEM_DIM_L + " FLOAT NOT NULL DEFAULT 0, "
                + ItemContract.ItemEntry.COLUMN_ITEM_DIM_W + " FLOAT NOT NULL DEFAULT 0, "
                + ItemContract.ItemEntry.COLUMN_ITEM_DIM_H + " FLOAT NOT NULL DEFAULT 0, "
                + ItemContract.ItemEntry.COLUMN_ITEM_IMG + " BLOB, "
                + ItemContract.ItemEntry.COLUMN_ITEM_BARCODE + " TEXT);";


        // Execute the SQL statement
        db.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}