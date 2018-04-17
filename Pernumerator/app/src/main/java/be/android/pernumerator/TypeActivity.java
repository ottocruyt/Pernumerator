package be.android.pernumerator;

import android.app.LoaderManager;
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
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;

import be.android.pernumerator.data.ItemContract;
import be.android.pernumerator.data.ItemImageHandler;

public class TypeActivity extends AppCompatActivity  implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the item data loader */
    private static final int ITEM_LOADER = 1;
    /** Adapter for the ListView */
    TypeCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TypeActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the item data
        ListView typeListView = findViewById(R.id.listType);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View typeEmptyView = findViewById(R.id.empty_view_type);
        typeListView.setEmptyView(typeEmptyView);

        // Setup an Adapter to create a list item for each row of item data in the Cursor.
        // There is no item data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new TypeCursorAdapter(this, null);
        typeListView.setAdapter(mCursorAdapter);
        // Setup the item click listener
        typeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link catalog}
                Intent intent = new Intent(TypeActivity.this, CatalogActivity.class);
                intent.putExtra("whichClickListener", "short");
                // Form the content URI that represents the specific item that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ItemEntry#CONTENT_URI}.
                // For example, the URI would be "content://be.android.pernumerator/be.android.pernumerator/2"
                // if the item with ID 2 was clicked on.
                Uri currentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, id);
                // Set the URI on the data field of the intent
                intent.setData(currentItemUri);
                // Launch the {@link CatalogActivity} to display the data for the current item.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_type.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_type, menu);
        return true;
    }
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(ItemContract.ItemEntry.CONTENT_URI, null, null);
        Log.v("TypeActivity", rowsDeleted + " rows deleted from item database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {ItemContract.ItemEntry._ID, ItemContract.ItemEntry.COLUMN_ITEM_TYPE, "COUNT("+ItemContract.ItemEntry._ID+")"}; //?
        // Define the sort method of the query
        String sortOrder = ItemContract.ItemEntry.COLUMN_ITEM_TYPE;
        String selection = ItemContract.ItemEntry._ID + " > -1 GROUP BY "+ItemContract.ItemEntry.COLUMN_ITEM_TYPE;

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ItemContract.ItemEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                selection,                   // no selection, but workaround for GROUP BY
                null,                   // No selection arguments
                sortOrder);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ItemCursorAdapter} with this new cursor containing updated item data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
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
        }
        return super.onOptionsItemSelected(item);
    }
    private void insertItem() throws IOException {
        // Create a ContentValues object where column names are the keys,
        // and oneplus 5T's item attributes are the values.
        ContentValues values1 = new ContentValues();
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.oneplus5t);
        values1.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, "Oneplus 5T");
        values1.put(ItemContract.ItemEntry.COLUMN_ITEM_TYPE, "Electronics");
        values1.put(ItemContract.ItemEntry.COLUMN_ITEM_OWNER, ItemContract.ItemEntry.OWNER_OTTO);
        values1.put(ItemContract.ItemEntry.COLUMN_ITEM_WEIGHT, 0.162);
        values1.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, 399);
        values1.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_L, 0.15);
        values1.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_W, 0.10);
        values1.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_H, 0.002);
        values1.put(ItemContract.ItemEntry.COLUMN_ITEM_IMG, ItemImageHandler.getBytes(bitmap1));
        values1.put(ItemContract.ItemEntry.COLUMN_ITEM_BARCODE, "12345678910");
        Uri newUri1 = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values1);
        ContentValues values2 = new ContentValues();
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),R.drawable.oneplus5t);
        values2.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, "Macbook PRO");
        values2.put(ItemContract.ItemEntry.COLUMN_ITEM_TYPE, "Electronics");
        values2.put(ItemContract.ItemEntry.COLUMN_ITEM_OWNER, ItemContract.ItemEntry.OWNER_LINDE);
        values2.put(ItemContract.ItemEntry.COLUMN_ITEM_WEIGHT, 1.542);
        values2.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, 2400);
        values2.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_L, 0.45);
        values2.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_W, 0.20);
        values2.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_H, 0.020);
        values2.put(ItemContract.ItemEntry.COLUMN_ITEM_IMG, ItemImageHandler.getBytes(bitmap2));
        values2.put(ItemContract.ItemEntry.COLUMN_ITEM_BARCODE, "012345789");
        Uri newUri2 = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values2);
        ContentValues values3 = new ContentValues();
        Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(),R.drawable.oneplus5t);
        values3.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, "Paasbloem");
        values3.put(ItemContract.ItemEntry.COLUMN_ITEM_TYPE, "Consumables");
        values3.put(ItemContract.ItemEntry.COLUMN_ITEM_OWNER, ItemContract.ItemEntry.OWNER_BOTH);
        values3.put(ItemContract.ItemEntry.COLUMN_ITEM_WEIGHT, 1);
        values3.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, 3.99);
        values3.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_L, 0.15);
        values3.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_W, 0.15);
        values3.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_H, 0.200);
        values3.put(ItemContract.ItemEntry.COLUMN_ITEM_IMG, ItemImageHandler.getBytes(bitmap3));
        values3.put(ItemContract.ItemEntry.COLUMN_ITEM_BARCODE, "147102357");
        Uri newUri3 = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values3);
    }


}
