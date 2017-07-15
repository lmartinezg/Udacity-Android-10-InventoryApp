package com.example.android.inventoryapp;

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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

public class ItemDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = ItemDetailActivity.class.getSimpleName();

    // Constants for decrease and increase buttons processing
    private static final int QUANTITY_BUTTONS_STEP = 1;
    private static final int OPTION_DECREASE = -1;
    private static final int OPTION_INCREASE = 1;

    /**
     * Identifier for the item data loader
     */
    private static final int EXISTING_ITEM_LOADER = 0;

    /**
     * Content URI for the selected item
     */
    private Uri mCurrentItemUri;

    private String mItemName;
    private int mPrice;
    private int mQuantity;
    private String mSupplierName;
    private String mSupplierPhone;
    private String mSupplierMail;
    private String mImageUriString;

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
    private TextView mImageErrorTextView;
    private Button mDeleteRecordButton;
    private TextView mSupplierDataHeaderTextView;

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
        mImageErrorTextView = (TextView) findViewById(R.id.error_item_image);
        mDeleteRecordButton = (Button) findViewById(R.id.delete_record_bt);
        mSupplierDataHeaderTextView = (TextView) findViewById(R.id.supplier_data_header_tv);

        setupDeleteRecordButtonListener();
        setupDecreaseListener();
        setupIncreaseListener();
        setupDialListener();
        setupMailListener();
    }

    // Listener for delete button
    private void setupDeleteRecordButtonListener() {
        mDeleteRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listener
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                int rowsDeleted = deleteItem();
                // if one row was deleted, finish and return to the main activity
                if (rowsDeleted == 1) {
                    finish();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing in this activity.
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
     * Perform the deletion of the pet in the database.
     */
    private int deleteItem() {
        int rowsAffected = getContentResolver().delete(mCurrentItemUri, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                    Toast.LENGTH_SHORT).show();
        }
        return rowsAffected;
    }


    // Listener for decrease button
    private void setupDecreaseListener() {
        mDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int rowsAffected = changeQuantity(OPTION_DECREASE);
                if (rowsAffected == 1) {
                    // Enable or disable the decrease button depending on quantity greater to min
                    // limit
                    mDecreaseButton.setEnabled(mQuantity > Utils.QUANTITY_LIMIT_MIN);
                }
            }
        });
    }

    // Listener for increase button
    private void setupIncreaseListener() {
        mIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int rowsAffected = changeQuantity(OPTION_INCREASE);
                if (rowsAffected == 1) {
                    // Enable or disable the increase button depending on quantity lower to max limit
                    mDecreaseButton.setEnabled(mQuantity < Utils.QUANTITY_LIMIT_MAX);
                }
            }
        });
    }

    // Process increase or decrease quantity
    private int changeQuantity(int processOption) {

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, mQuantity + (QUANTITY_BUTTONS_STEP *
                processOption));

        // Update the row pointed by mCurrentItemUri
        int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
        if (rowsAffected != 1) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(getApplicationContext(),
                    getString(R.string.error_quantity_update),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Enable or disable the increase button depending on quantity lower to max limit
            mDecreaseButton.setEnabled(mQuantity < Utils.QUANTITY_LIMIT_MAX);
        }
        return rowsAffected;
    }

    // Listener for dial ListView
    private void setupDialListener() {
        mSupplierPhoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utils.isValidPhone(mSupplierPhone)) {
                    // Phone Number is not valid
                    Toast.makeText(getApplicationContext(), R.string.phone_not_valid,
                            Toast.LENGTH_SHORT).show();
                } else {

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + mSupplierPhone));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        // Could not start dial activity
                        Toast.makeText(getApplicationContext(), R.string.could_not_dial,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // Listener for mail ListView
    private void setupMailListener() {
        mSupplierMailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utils.isValidEmail(mSupplierMail)) {
                    // Mail address is not valid
                    Toast.makeText(getApplicationContext(), R.string.email_not_valid,
                            Toast.LENGTH_SHORT).show();
                } else {

                    Intent intent = new Intent(Intent.ACTION_SENDTO);

                    /* This works */
                    String uriText = "mailto:" + mSupplierMail +
                            "?subject=" + Uri.encode(getString(R.string.order_to_supplier)) +
                            "&body=" + String.format(getString(R.string.mail_body),
                            mItemName);
                    Uri uri = Uri.parse(uriText);
                    intent.setData(uri);

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        // Could not start dial activity
                        Toast.makeText(getApplicationContext(), R.string.could_not_mail,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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
            mItemName = cursor.getString(nameColumnIndex);
            mPrice = cursor.getInt(priceColumnIndex);
            mQuantity = cursor.getInt(quantityColumnIndex);
            mSupplierName = cursor.getString(supplierNameColumnIndex);
            mSupplierPhone = cursor.getString(supplierPhoneColumnIndex);
            mSupplierMail = cursor.getString(supplierMailColumnIndex);
            mImageUriString = cursor.getString(itemImageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameTextView.setText(mItemName);
            mPriceTextView.setText(String.valueOf(mPrice));
            mQuantityTextView.setText(String.valueOf(mQuantity));
            mSupplierNameTextView.setText(mSupplierName);
            mSupplierPhoneTextView.setText(mSupplierPhone);
            mSupplierMailTextView.setText(mSupplierMail);

            /* Show a textview if no image provided */
            if (TextUtils.isEmpty(mImageUriString)) {
                mImageImageView.setVisibility(View.GONE);
                mImageErrorTextView.setVisibility(View.VISIBLE);
            } else {
                Log.v(LOG_TAG, "Image URI String: " + mImageUriString);
                mImageImageView.setImageURI(Uri.parse(mImageUriString));
                mImageImageView.setVisibility(View.VISIBLE);
                mImageErrorTextView.setVisibility(View.GONE);
            }

            // Enable or disable the decrease and increase buttons depending on mQuantity
            mDecreaseButton.setEnabled(mQuantity > Utils.QUANTITY_LIMIT_MIN);
            mIncreaseButton.setEnabled(mQuantity < Utils.QUANTITY_LIMIT_MAX);

            // Hide mail and phone views if there is no data for them
            if (TextUtils.isEmpty(mSupplierMail)) {
                mSupplierMailTextView.setVisibility(View.GONE);
            } else {
                mSupplierMailTextView.setVisibility(View.VISIBLE);
            }

            if (TextUtils.isEmpty(mSupplierPhone)) {
                mSupplierPhoneTextView.setVisibility(View.GONE);
            } else {
                mSupplierPhoneTextView.setVisibility(View.VISIBLE);
            }

            // Change supplier data header text
            if (TextUtils.isEmpty(mSupplierPhone) && TextUtils.isEmpty(mSupplierMail)) {
                mSupplierDataHeaderTextView.setText(R.string.supplier_data);
            } else {
                mSupplierDataHeaderTextView.setText(R.string.order_from_supplier);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.

        // There are no input fields
    }

}
