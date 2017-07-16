package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

/**
 * {@link ContentProvider} for items app.
 */
public class ItemProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = ItemProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the items table
     */
    private static final int ITEMS = 100;

    /**
     * URI matcher code for the content URI for a single Item in the items table
     */
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // Content URI of the form "content://com.example.android.items/items
        // Used to provide access to MULTIPLE rows of the items table.
        // Will map to the integer code {@link #items}
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);

        // Content URI of the form "content://com.example.android.items/items/#
        // Used to provide access to a single row of the items table.
        // Will map to the integer code {@link #item_id}
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    /**
     * Database helper object
     */
    private ItemDbHelper mDbHelper;

    /**
     * Context to help access to resources
     */
    private Context mContext;
    private Resources mResources;

    @Override
    public boolean onCreate() {
        mDbHelper = new ItemDbHelper(getContext());
        mContext = getContext();
        mResources = mContext.getResources();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Query the items table with the given projection, selection,
                // selection arguments, and sort order.
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                // Query the items table for an individual row identified by its id, extracted
                // from the URI

                // Prepare the selection and selectionArgs variables with the proper values
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, null);
                break;
            default:
                throw new IllegalArgumentException(String.format(mResources.getString(R.string
                        .error_unknown_URI_query), uri));
        }

        // Set notification URI on the Cursor,
        cursor.setNotificationUri(mContext.getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert a Item into the database
     *
     * @param uri           The URI for the items table
     * @param contentValues The values for the columns
     * @return The new content URI
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException(String.format(mResources.getString(R.string
                        .error_insertion_not_supported), uri));
        }
    }

    /**
     * Insert a Item into the database
     *
     * @param uri    The URI for the items table
     * @param values The values for the columns
     * @return The new content URI
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        // If there is a name, check that it is not null nor blank
        if (values.containsKey(ItemEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
            if (name == null || name.equals("")) {
                throw new IllegalArgumentException(mResources.getString(R.string
                        .error_name_required));
            }
        }

        Integer quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
        // If not present, default to zero
        if (quantity == null) {
            quantity = 0;
        }

        Integer price = values.getAsInteger(ItemEntry.COLUMN_ITEM_PRICE);
        // If not present, default to zero
        if (price == null) {
            price = 0;
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new Item with the given values
        long id = database.insert(ItemEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, String.format(mResources.getString(R.string.error_insert_failed), uri));
            return null;
        }

        // Notify listeners
        mContext.getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Update rows in items table
     *
     * @param uri           The URI of the row to be updated
     * @param contentValues The values
     * @param selection     Selection criteria
     * @param selectionArgs Selection arguments
     * @return Number of updated rows
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Update multiple rows, according to the selection criteria
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // Update a single row
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(String.format(mResources.getString(R.string
                        .error_update_not_supported), uri));
        }
    }

    /**
     * Update individual or multiple rows in the database
     *
     * @param uri           The URI to access the table or one single row
     * @param values        The new values for the rows
     * @param selection     The selection criteria
     * @param selectionArgs The selection arguments
     * @return Number of updated rows
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If there is a name, check that it is not null nor blank
        if (values.containsKey(ItemEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
            if (name == null || name.equals("")) {
                throw new IllegalArgumentException(mResources.getString(R.string
                        .error_name_required));
            }
        }

        // All other columns are optional

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int updatedRows = database.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);
        if (updatedRows != 0) {
            // Notify listeners
            mContext.getContentResolver().notifyChange(uri, null);
        }

        return updatedRows;
    }

    /**
     * Delete one or multiple records from the items table
     *
     * @param uri           The URI to access the table or a single row in the table
     * @param selection     The selection criteria
     * @param selectionArgs The selection arguments
     * @return Number of deleted rows
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int deletedRows;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete multiple rows according to the selection criteria
                deletedRows = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                // Delete a single row identified in the URI
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deletedRows = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(String.format(mResources.getString(R.string
                        .error_deletion_not_supported), uri));
        }
        if (deletedRows != 0) {
            // Notify listeners
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(String.format(mResources.getString(R.string
                        .error_unknown_URI), uri, match));
        }
    }
}
