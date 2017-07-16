package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

/**
 * Database helper for Inventory app. Manages database creation and version management.
 */
public class ItemDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = ItemDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";

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

        // TODO: Remove the DROP TABLE section. Was only for testing purposes
/*
        final String SQL_DROP_ITEMS_TABLE =
                "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;
        Log.v(LOG_TAG, SQL_DROP_ITEMS_TABLE);

        // Execute the SQL statement
        db.execSQL(SQL_DROP_ITEMS_TABLE);
*/

        // Create a String that contains the SQL statement to create the items table
        final String SQL_CREATE_ITEMS_TABLE =
                "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                        ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                        ItemEntry.COLUMN_ITEM_SUPPLIER_NAME + " TEXT, " +
                        ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL + " TEXT, " +
                        ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE + " TEXT, " +
                        ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                        ItemEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL DEFAULT 0, " +
                        ItemEntry.COLUMN_ITEM_IMAGE_URI + " TEXT)";

        Log.v(LOG_TAG, SQL_CREATE_ITEMS_TABLE);

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
