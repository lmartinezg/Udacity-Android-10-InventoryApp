package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = CatalogActivity.class.getName();

    private static final int ITEM_LOADER = 0;

    ItemCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

/*
        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
*/

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
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ItemEntry#CONTENT_URI},
                // For example, the URI would be "content://com.example.android.items/items/2"
                // if the pet with ID 2 was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                // Set the URI in the data field of the Intent
                intent.setData(currentPetUri);
                Log.v(LOG_TAG, "URI: " + currentPetUri.toString());

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });


        // Prepare the loader.  Either re-connect with an existing one,
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

        Uri newUri = null;

        ContentValues values = new ContentValues();

        values.clear();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Item 1");
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, 0);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, 10);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME, "dummy supplier name 1");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL, "dummy supplier email 1");
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE, "dummy supplier phone 1");
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
        //values.put(ItemEntry.COLUMN_ITEM_QUANTITY, 9);
        //values.put(ItemEntry.COLUMN_ITEM_PRICE, 30);
        //values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME, "dummy supplier name 3");
        //values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL, "dummy supplier email 3");
        //values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE, "dummy supplier phone 3");
        //values.put(ItemEntry.COLUMN_ITEM_IMAGE_URI, "dummy supplier image URI 3");
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

        // Quthe database in background
        return new CursorLoader(this, // Parent activity context
                ItemEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include
                selection,              // Selection (null to retrieve all records)
                selectionArgs,          // Selection arguments
                sortOrder);             // Sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void deleteAllItems() {
        int rowsAffected = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the delete.
            Toast.makeText(this, "Delete All Entries failed",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, "Delete All Entries successful",
                    Toast.LENGTH_SHORT).show();
        }

    }

}