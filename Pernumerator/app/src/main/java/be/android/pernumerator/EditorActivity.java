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

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

import be.android.pernumerator.data.ItemContract;

/**
 * Allows user to create a new item or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the item data loader */
    private static final int EXISTING_ITEM_LOADER = 0;

    /** Content URI for the existing item (null if it's a new item) */
    private Uri mCurrentItemUri;

    /** EditText field to enter the item's name */
    private EditText mNameEditText;

    /** EditText field to enter the item's type */
    private EditText mTypeEditText;

    /** EditText field to enter the item's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the item's price */
    private EditText mPriceEditText;

    /** EditText field to enter the item's length */
    private EditText mLengthEditText;
    /** EditText field to enter the item's width */
    private EditText mWidthEditText;
    /** EditText field to enter the item's height */
    private EditText mHeightEditText;

    /** EditText field to enter the item's owner */
    private Spinner mOwnerSpinner;

    /**
     * Owner of the item. The possible valid values are in the ItemContract.java file:
     * {@link ItemContract.ItemEntry#OWNER_BOTH}, {@link ItemContract.ItemEntry#OWNER_OTTO}, {@link ItemContract.ItemEntry#OWNER_LINDE} or
     * {@link ItemContract.ItemEntry#OWNER_OTHER}.
     */
    private int mOwner = ItemContract.ItemEntry.OWNER_BOTH;

    /** Boolean flag that keeps track of whether the item has been edited (true) or not (false) */
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // If the intent DOES NOT contain a item content URI, then we know that we are
        // creating a new item.
        if (mCurrentItemUri == null) {
            // This is a new item, so change the app bar to say "Add a Item"
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit Item"
            setTitle(getString(R.string.editor_activity_title_edit_item));

            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mTypeEditText = (EditText) findViewById(R.id.edit_item_type);
        mWeightEditText = (EditText) findViewById(R.id.edit_item_weight);
        mOwnerSpinner = (Spinner) findViewById(R.id.spinner_owner);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mLengthEditText = (EditText) findViewById(R.id.edit_item_length);
        mWidthEditText = (EditText) findViewById(R.id.edit_item_width);
        mHeightEditText = (EditText) findViewById(R.id.edit_item_height);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mTypeEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mLengthEditText.setOnTouchListener(mTouchListener);
        mWidthEditText.setOnTouchListener(mTouchListener);
        mHeightEditText.setOnTouchListener(mTouchListener);
        mOwnerSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the owner of the item.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter ownerSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_owner_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        ownerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mOwnerSpinner.setAdapter(ownerSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mOwnerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.owner_otto))) {
                        mOwner = ItemContract.ItemEntry.OWNER_OTTO;
                    } else if (selection.equals(getString(R.string.owner_linde))) {
                        mOwner = ItemContract.ItemEntry.OWNER_LINDE;
                    } else if (selection.equals(getString(R.string.owner_other))) {
                        mOwner = ItemContract.ItemEntry.OWNER_OTHER;
                    } else {
                        mOwner = ItemContract.ItemEntry.OWNER_BOTH;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mOwner = ItemContract.ItemEntry.OWNER_BOTH;
            }
        });
    }

    /**
     * Get user input from editor and save item into database.
     */
    private boolean saveItem() {

        // return value determines if after saving, the EditorActivity should be closed or not (true = close, false = stay open)
        boolean exitEditorAfterReturn = true;
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String typeString = mTypeEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String lengthString = mLengthEditText.getText().toString().trim();
        String widthString = mWidthEditText.getText().toString().trim();
        String heightString = mHeightEditText.getText().toString().trim();


        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(typeString) &&
                TextUtils.isEmpty(weightString) && mOwner == ItemContract.ItemEntry.OWNER_BOTH &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(lengthString) &&
                TextUtils.isEmpty(widthString) && TextUtils.isEmpty(heightString)) {
            // Since no fields were modified, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            // -> even if you press V, if nothing is changed it will just not save it and go to main
            return exitEditorAfterReturn;
        }

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        // First check for name (otherwise don't start):

        if (nameString == null || nameString.equals("")) {
            exitEditorAfterReturn = false;
            Toast.makeText(this, getString(R.string.editor_insert_item_noname), Toast.LENGTH_SHORT).show();
            return exitEditorAfterReturn;
        }

        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_TYPE, typeString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_OWNER, mOwner);
        // If the weight is not provided by the user, don't try to parse the string into an
        // float value. Use 0 by default.
        float weight = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Float.parseFloat(weightString);
        }
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_WEIGHT, weight);
        // If the price is not provided by the user, don't try to parse the string into an
        // float value. Use 0 by default.
        float price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Float.parseFloat(priceString);
        }
        // put it in the values
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, price);

        // If the length is not provided by the user, don't try to parse the string into an
        // float value. Use 0 by default.
        float length = 0;
        if (!TextUtils.isEmpty(lengthString)) {
            length = Float.parseFloat(lengthString);
        }
        // put it in the values
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_L, length);

        // If the width is not provided by the user, don't try to parse the string into an
        // float value. Use 0 by default.
        float width = 0;
        if (!TextUtils.isEmpty(widthString)) {
            width = Float.parseFloat(widthString);
        }
        // put it in the values
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_W, width);

        // If the height is not provided by the user, don't try to parse the string into an
        // float value. Use 0 by default.
        float height = 0;
        if (!TextUtils.isEmpty(heightString)) {
            height = Float.parseFloat(heightString);
        }
        // put it in the values
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_DIM_H, height);

        // Determine if this is a new or existing item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return exitEditorAfterReturn;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
            menuItemDelete.setVisible(false);
            MenuItem menuItemEdit = menu.findItem(R.id.action_edit);
            menuItemEdit.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                boolean exitToCatalog = saveItem();
                // Exit activity
                if (exitToCatalog) {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Edit" menu option
            case R.id.action_edit:
                enableAllViews(true);
                // Show toast confirming edit mode
                Toast.makeText(this, getString(R.string.editor_edit_mode_on),
                        Toast.LENGTH_SHORT).show();
                return false; // false means don't go to the catalog, but stay in edit activity
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the items table
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_TYPE,
                ItemContract.ItemEntry.COLUMN_ITEM_OWNER,
                ItemContract.ItemEntry.COLUMN_ITEM_WEIGHT,
                ItemContract.ItemEntry.COLUMN_ITEM_PRICE,
                ItemContract.ItemEntry.COLUMN_ITEM_DIM_L,
                ItemContract.ItemEntry.COLUMN_ITEM_DIM_W,
                ItemContract.ItemEntry.COLUMN_ITEM_DIM_H};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
            int typeColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_TYPE);
            int ownerColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_OWNER);
            int weightColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_WEIGHT);
            int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
            int lengthColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_DIM_L);
            int widthColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_DIM_W);
            int heightColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_DIM_H);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String type = cursor.getString(typeColumnIndex);
            int owner = cursor.getInt(ownerColumnIndex);
            float weight = cursor.getFloat(weightColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            float length = cursor.getFloat(lengthColumnIndex);
            float width = cursor.getFloat(widthColumnIndex);
            float height = cursor.getFloat(heightColumnIndex);

            // Update the views on the screen with the values from the database
            // first format the numbers
            DecimalFormat weightFormat = new DecimalFormat("###0.000");
            DecimalFormat priceFormat = new DecimalFormat("###0.00");
            DecimalFormat dimFormat = new DecimalFormat("###0.000");
            String formattedWeight = weightFormat.format(weight);
            String formattedPrice = priceFormat.format(price);
            String formattedLength = dimFormat.format(length);
            String formattedWidth = dimFormat.format(width);
            String formattedHeight = dimFormat.format(height);

            mNameEditText.setText(name);
            mTypeEditText.setText(type);
            mWeightEditText.setText(formattedWeight);
            mPriceEditText.setText(formattedPrice);
            mLengthEditText.setText(formattedLength);
            mWidthEditText.setText(formattedWidth);
            mHeightEditText.setText(formattedHeight);
            // Owner is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is BOTH, 1 is OTTO, 2 is LINDE, 3 is OTHER).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (owner) {
                case ItemContract.ItemEntry.OWNER_OTTO:
                    mOwnerSpinner.setSelection(1);
                    break;
                case ItemContract.ItemEntry.OWNER_LINDE:
                    mOwnerSpinner.setSelection(2);
                    break;
                case ItemContract.ItemEntry.OWNER_OTHER:
                    mOwnerSpinner.setSelection(3);
                default:
                    mOwnerSpinner.setSelection(0);
                    break;
            }
        }
        //disable views for editing
        enableAllViews(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mTypeEditText.setText("");
        mWeightEditText.setText("");
        mPriceEditText.setText("");
        mLengthEditText.setText("");
        mWidthEditText.setText("");
        mHeightEditText.setText("");
        mOwnerSpinner.setSelection(0); // Select "Unknown" gender
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
    // enable - disable all edit fields
    private void enableAllViews(boolean enableDisable) {
        //ArrayList<EditText> myEditTextList = new ArrayList<EditText>();
        View editParentLayout = (View) findViewById(R.id.edit_layout_parent);
        ArrayList<View> allViewsWithinMyParentView = getAllChildren(editParentLayout);
        for (View child : allViewsWithinMyParentView) {
            if (child instanceof EditText) {
                child.setEnabled(enableDisable);
                Log.d("Child", "child found");
            } //else if (child instanceof Spinner) {                  --> doesn't find spinner
              //  child.setEnabled(enableDisable);
        }
        mOwnerSpinner.setEnabled(enableDisable); //method doesn't work for spinner - so fix with hardcode

    }

    private ArrayList<View> getAllChildren(View v) {

        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<View>();

        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {

            View child = vg.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }
}