package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.example.android.inventoryapp.Utils.isValidEmail;
import static com.example.android.inventoryapp.Utils.isValidPhone;

public class ItemDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Limits for increase and decrease buttons
    private static final int QUANTITY_LIMIT_MIN = 0;
    private static final int QUANTITY_LIMIT_MAX = 1000;
    private static final int QUANTITY_BUTTONS_STEP = 1;

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

        setupDecreaseButtonListener();
        setupIncreaseListener();
        setupDialListener();
        setupMailListener();
    }

    // Listener for decrease button
    private void setupDecreaseButtonListener() {
        mDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_ITEM_QUANTITY, mQuantity - QUANTITY_BUTTONS_STEP);

                // Update the row pointed by mCurrentItemUri
                int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
                if (rowsAffected != 1) {
                    // If no rows were affected , then there was an error with the update.
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_quantity_update),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Enable or disable the decrease button depending on quantity greater than zero
                    mDecreaseButton.setEnabled(mQuantity > QUANTITY_LIMIT_MIN);
                }
            }
        });
    }

    // Listener for increase button
    private void setupIncreaseListener() {
        mIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_ITEM_QUANTITY, mQuantity + QUANTITY_BUTTONS_STEP);

                // Update the row pointed by mCurrentItemUri
                int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
                if (rowsAffected != 1) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_quantity_update),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Enable or disable the increase button depending on quantity lower to max limit
                    mDecreaseButton.setEnabled(mQuantity < QUANTITY_LIMIT_MAX);
                }
            }
        });
    }

    // Listener for dial ListView
    private void setupDialListener() {
        mSupplierPhoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidPhone(mSupplierPhone)) {
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
                if (!isValidEmail(mSupplierMail)) {
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

                    // TODO: Remove this code that does not work -----------
                    /* But this fails */
/*
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.setType("text/plain");
                    String[] addresses = {mSupplierMail};
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[] {mSupplierMail} );
                    intent.putExtra(Intent.EXTRA_SUBJECT, Uri.encode(getString(R.string
                    .order_to_supplier)));
                    intent.putExtra(Intent.EXTRA_TEXT, Uri.encode(String.format(getString(R.string
                    .mail_body), mItemName)));
*/
                    // TODO: Remove this code that does not work -----------

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

            // TODO: Deal with the image. Retrieve it from the gallery
// Test code fragment. To be removed if not used
/*
                    Uri uri = data.getData();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            // Log.d(TAG, String.valueOf(bitmap));

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
            /* Show a textview if no image provided */
            if (mImageUriString == null) {
                mImageErrorTextView.setText("No image selected fos this item");
                mImageImageView.setVisibility(View.GONE);
                mImageErrorTextView.setVisibility(View.VISIBLE);

            } else {
                try {
                    final Uri imageUri = Uri.parse(mImageUriString);
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    mImageImageView.setImageBitmap(selectedImage);
                    mImageImageView.setContentDescription(getString(R.string.item_image_description));
                    mImageImageView.setVisibility(View.VISIBLE);
                    mImageErrorTextView.setVisibility(View.GONE);
                } catch (FileNotFoundException e) {
                    /* Show an error textview if failed to get image from gallery */
                    mImageImageView.setVisibility(View.GONE);
                    mImageErrorTextView.setVisibility(View.VISIBLE);
                    // TODO: Remove printStackTrace
                    e.printStackTrace();
                }
            }

            // Enable or disable the decrease and increase buttons depending on mQuantity
            mDecreaseButton.setEnabled(mQuantity > QUANTITY_LIMIT_MIN);
            mIncreaseButton.setEnabled(mQuantity < QUANTITY_LIMIT_MAX);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.

        // There are no input fields
    }

}
