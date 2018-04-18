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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;

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

    public void exportDB() {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "pernumeratorExport.csv");
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = getReadableDatabase();
            String sqlString = "SELECT " +
                    ItemContract.ItemEntry._ID + ", " +
                    ItemContract.ItemEntry.COLUMN_ITEM_NAME + ", " +
                    ItemContract.ItemEntry.COLUMN_ITEM_TYPE + ", " +
                    ItemContract.ItemEntry.COLUMN_ITEM_OWNER + ", " +
                    ItemContract.ItemEntry.COLUMN_ITEM_WEIGHT + ", " +
                    ItemContract.ItemEntry.COLUMN_ITEM_PRICE + ", " +
                    ItemContract.ItemEntry.COLUMN_ITEM_DIM_L + ", " +
                    ItemContract.ItemEntry.COLUMN_ITEM_DIM_W + ", " +
                    ItemContract.ItemEntry.COLUMN_ITEM_DIM_H + ", " +
                    ItemContract.ItemEntry.COLUMN_ITEM_BARCODE + " FROM items";
            Cursor curCSV = db.rawQuery(sqlString,null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext())
            {
                //Which column you want to exprort
                String arrStr[] ={
                        curCSV.getString(0),
                        curCSV.getString(1),
                        curCSV.getString(2),
                        curCSV.getString(3),
                        curCSV.getString(4),
                        curCSV.getString(5),
                        curCSV.getString(6),
                        curCSV.getString(7),
                        curCSV.getString(8),
                        curCSV.getString(9),
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        }
        catch(Exception sqlEx)
        {
            Log.e("ItemDbHelper", sqlEx.getMessage(), sqlEx);
        }
    }



}