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
package be.android.pernumerator;

import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;

import be.android.pernumerator.data.ItemContract;
import be.android.pernumerator.data.ItemDbHelper;
import be.android.pernumerator.data.ItemImageHandler;
import be.android.pernumerator.data.ItemProvider;

//TODO add type spinner with possibility of adding a new one
//TODO database update handler
//TODO check if refactoring is needed
//TODO implement search
//TODO add preferences (via preferences API) to get use flash or not
//TODO change catalog to only show one type (selected in the TypeActivity)


/**
 * Displays list of items that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the item data loader */
    private static final int ITEM_LOADER = 0;
    private static final int TYPE_LOADER = 1;
    private Uri mCurrentItemUri;
    private String mWhichClickListener;
    private String mDisplayType;

    /** Adapter for the ListView */
    ItemCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the item data
        ListView itemListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        mWhichClickListener = intent.getStringExtra("whichClickListener");

        // Setup an Adapter to create a list item for each row of item data in the Cursor.
        // There is no item data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.putExtra("whichClickListener", "short");
                // Form the content URI that represents the specific item that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ItemEntry#CONTENT_URI}.
                // For example, the URI would be "content://be.android.pernumerator/be.android.pernumerator/2"
                // if the item with ID 2 was clicked on.
                Uri currentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentItemUri);

                // Launch the {@link EditorActivity} to display the data for the current item.
                startActivity(intent);
            }
        });
        // Setup the item LONG click listener
        itemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.putExtra("whichClickListener", "long");
                // Form the content URI that represents the specific item that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ItemEntry#CONTENT_URI}.
                // For example, the URI would be "content://be.android.pernumerator/be.android.pernumerator/2"
                // if the item with ID 2 was clicked on.
                Uri currentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentItemUri);

                // Launch the {@link EditorActivity} to display the data for the current item.
                startActivity(intent);
                return true;
            }
        });



        // Kick off the loader
        getLoaderManager().initLoader(TYPE_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded item data into the database. For debugging purposes only.
     */
    private void insertItem() throws IOException {
        // Create a ContentValues object where column names are the keys,
        // and oneplus 5T's item attributes are the values.
        ContentValues values = new ContentValues();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.oneplus5t);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, "Oneplus 5T");
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_TYPE, "Electronics");
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_OWNER, ItemContract.ItemEntry.OWNER_OTTO);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_WEIGHT, 0.162);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, 10);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_L, 0.15);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_W, 0.10);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_H, 0.002);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_IMG, ItemImageHandler.getBytes(bitmap));
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_BARCODE, "12345678910");

        // Insert a new row for Oneplus into the provider using the ContentResolver.
        // Use the {@link ItemEntry#CONTENT_URI} to indicate that we want to insert
        // into the items database table.
        // Receive the new content URI that will allow us to access Oneplus's data in the future.
        Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all items in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(ItemContract.ItemEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from item database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu

        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                try {
                    insertItem();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "onOptionsItemSelected: IOException");
                }
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
            case android.R.id.home:
                Intent homeIntent = new Intent(this, TypeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_TYPE };
        // Define the sort method of the query
        String sortOrder = ItemContract.ItemEntry.COLUMN_ITEM_TYPE;

        switch(i) {
            case(ITEM_LOADER):
                String selectionItem = ItemContract.ItemEntry.COLUMN_ITEM_TYPE + "='" + mDisplayType +"'";
                // This loader will execute the ContentProvider's query method on a background thread
                return new CursorLoader(this,   // Parent activity context
                        ItemContract.ItemEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selectionItem,                   // No selection clause
                        null,                   // No selection arguments
                        sortOrder);                  // Default sort order

            case(TYPE_LOADER):
                String idOfClickedItem = String.valueOf(ContentUris.parseId(mCurrentItemUri));
                String selectionType = ItemContract.ItemEntry._ID + "=" + idOfClickedItem;
                return new CursorLoader(this,ItemContract.ItemEntry.CONTENT_URI,projection,selectionType,null,sortOrder);
            default:
                return null;

        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ItemCursorAdapter} with this new cursor containing updated item data
        switch(loader.getId()){
            case(ITEM_LOADER):
                mCursorAdapter.swapCursor(data);
                break;
            case(TYPE_LOADER):
                if (data.moveToFirst()) {
                    int typeColumnIndex = data.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_TYPE);
                    String type = data.getString(typeColumnIndex);
                    mDisplayType = type;
                    android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
                    mActionBar.setTitle(mDisplayType);
                    getLoaderManager().initLoader(ITEM_LOADER, null, this);
                }
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        switch(loader.getId()){
            case(ITEM_LOADER):
                mCursorAdapter.swapCursor(null);
                break;
            case(TYPE_LOADER):
                break;
        }

    }

}
