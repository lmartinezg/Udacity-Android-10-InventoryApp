package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
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
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

/**
 * Displays list of items that were entered and stored in the app.
 */
public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getName();

    private static final int ITEM_LOADER = 0;

    private ItemCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open ItemDetailActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the item data
        final ListView itemListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        mCursorAdapter = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        // Setup item click listener
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link ItemDetailActivity}
                Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                // Set the URI in the data field of the Intent
                intent.setData(currentItemUri);
                Log.v(LOG_TAG, "URI: " + currentItemUri.toString());

                // Launch the {@link ItemDetailActivity} to display the data for the current item.
                startActivity(intent);
            }
        });


        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    /**
     * Helper method to insert hardcoded dummy item data into the database. For testing purposes
     * only.
     */
    private void insertDummyItems() {

        Uri newUri;

        ContentValues values = new ContentValues();

        values.clear();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Item 1");
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, 0);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, 10);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME, "dummy supplier name 1");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL, "lmartinezg.gm@gmail.com");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE, "639032157");
        values.put(ItemEntry.COLUMN_ITEM_IMAGE_URI, "dummy supplier image URI 1");
        newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
        Log.v(LOG_TAG, "New row id " + ContentUris.parseId(newUri));

        values.clear();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Item 2");
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, 6);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, 20);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME, "dummy supplier name 2");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL, "dummy supplier email 2");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE, "dummy supplier phone 2");
        values.put(ItemEntry.COLUMN_ITEM_IMAGE_URI, "dummy supplier image URI 2");
        newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
        Log.v(LOG_TAG, "New row id " + ContentUris.parseId(newUri));

        // Row with null values for testing
        values.clear();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Item 3 (with null values)");
        newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
        Log.v(LOG_TAG, "New row id " + ContentUris.parseId(newUri));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyItems();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int it, Bundle args) {
        // Define a projection that specifies the columns we care about
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE};

        String selection = null;
        String[] selectionArgs = null;

        String sortOrder = ItemEntry._ID;

        // Query the database in background
        return new CursorLoader(this, // Parent activity context
                ItemEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include
                selection,              // Selection (null to retrieve all records)
                selectionArgs,          // Selection arguments
                sortOrder);             // Sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Hide loading indicator
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Delete All Records menu option
     * Show a dialog to confirm the deletion
     */
    private void deleteAllItems() {

        // Create an AlertDialog.Builder and set the message, and click listener
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete_all, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                int rowsDeleted = performDeleteAllItems();
                // if rows were deleted, return
                if (rowsDeleted == 1) {
                    return;
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
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
     * Perform the deletion of all item records
     *
     * @return the number of deleted rows
     */
    private int performDeleteAllItems() {

        int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were affected, then there was an error with the delete.
            Toast.makeText(this, "Delete All Entries failed",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, "Delete All Entries successful",
                    Toast.LENGTH_SHORT).show();
        }
        return rowsDeleted;
    }

}