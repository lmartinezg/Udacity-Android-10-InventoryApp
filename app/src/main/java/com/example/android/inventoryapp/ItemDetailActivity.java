package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

public class ItemDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Limits for increase and decrease buttons
    private static final int QUANTITY_LIMIT_MIN = 0;
    private static final int QUANTITY_LIMIT_MAX = 1000;

    /**
     * Identifier for the item data loader
     */
    private static final int EXISTING_ITEM_LOADER = 0;

    /**
     * Content URI for the selected item
     */
    private Uri mCurrentItemUri;

    private String name;
    private int price;
    private int quantity;
    private String supplier_name;
    private String supplier_phone;
    private String supplier_mail;

    // Variables for Views
    private TextView mNameTextView;
    private TextView mPriceTextView;
    private TextView mQuantityTextView;
    private TextView mSupplierNameTextView;
    private TextView mSupplierPhoneTextView;
    private TextView mSupplierMailTextView;
    private ImageButton mIncreaseButton;
    private ImageButton mDecreaseButton;
    private ImageView mImageImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Retrieve data from Intent
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // Initialize a loader to read the item data from the database
        // and display the current values in the editor
        getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mNameTextView = (TextView) findViewById(R.id.name_tv);
        mPriceTextView = (TextView) findViewById(R.id.price_tv);
        mQuantityTextView = (TextView) findViewById(R.id.quantity_tv);
        mDecreaseButton = (ImageButton) findViewById(R.id.decrease_bt);
        mIncreaseButton = (ImageButton) findViewById(R.id.increase_bt);
        mSupplierNameTextView = (TextView) findViewById(R.id.supplier_name_tv);
        mSupplierPhoneTextView = (TextView) findViewById(R.id.supplier_phone_tv);
        mSupplierMailTextView = (TextView) findViewById(R.id.supplier_mail_tv);
        mImageImageView = (ImageView) findViewById(R.id.item_image_iv);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Projection to retrieve all columns from items table
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_SUPPLIER_NAME,
                ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE,
                ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL,
                ItemEntry.COLUMN_ITEM_IMAGE_URI};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, // Parent activity context
                mCurrentItemUri,        // Query the content URI for the current item
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
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ItemEntry
                    .COLUMN_ITEM_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ItemEntry
                    .COLUMN_ITEM_SUPPLIER_PHONE);
            int supplierMailColumnIndex = cursor.getColumnIndex(ItemEntry
                    .COLUMN_ITEM_SUPPLIER_EMAIL);
            int itemImageColumnIndex = cursor.getColumnIndex(ItemEntry
                    .COLUMN_ITEM_IMAGE_URI);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            String supplierMail = cursor.getString(supplierMailColumnIndex);
            String imageUri = cursor.getString(itemImageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameTextView.setText(name);
            mPriceTextView.setText(String.valueOf(price));
            mQuantityTextView.setText(String.valueOf(quantity));
            mSupplierNameTextView.setText(supplierName);
            mSupplierPhoneTextView.setText(supplierPhone);
            mSupplierMailTextView.setText(supplierMail);

            // TODO: Deal view the image. Retrieve it from the gallery

            // Enable or disable the decrease and increase buttons depending on quantity
            mDecreaseButton.setEnabled(quantity > QUANTITY_LIMIT_MIN);
            mIncreaseButton.setEnabled(quantity < QUANTITY_LIMIT_MAX);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.

        // There are no input fields
    }


}
