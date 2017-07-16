package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

import static android.text.TextUtils.isEmpty;

public class AddItemActivity extends AppCompatActivity {

    private static final String LOG_TAG = AddItemActivity.class.getName();

    // Uri to local storage image
    private Uri mImageUri;
    private String mItemName;
    private int mPrice;
    private int mQuantity;
    private String mSupplierName;
    private String mSupplierPhone;
    private String mSupplierMail;
    private String mImageUriString;
    // Variables for Views
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;
    private EditText mSupplierMailEditText;
    private ImageView mImageImageView;
    private ImageButton mSelectImageButton;
    private Button mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.name_tv);
        mPriceEditText = (EditText) findViewById(R.id.price_tv);
        mSupplierNameEditText = (EditText) findViewById(R.id.supplier_name_tv);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.supplier_phone_tv);
        mSupplierMailEditText = (EditText) findViewById(R.id.supplier_mail_tv);
        mImageImageView = (ImageView) findViewById(R.id.item_image_iv);
        mSelectImageButton = (ImageButton) findViewById(R.id.select_image_bt);
        mSaveButton = (Button) findViewById(R.id.save_bt);

        setupSelectImageListener();
        setupSaveListener();
    }

    // Select an image from the gallery
    private void setupSelectImageListener() {
        mSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                intent.setType("image/*");
                startActivityForResult(intent, Utils.SELECT_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    mImageUri = data.getData();
                    mImageImageView.setImageURI(mImageUri);
                }
                break;
        }
    }

    private void setupSaveListener() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check for required values

                if (isEmpty(mNameEditText.getText().toString())) {
                    showToast(getString(R.string.error_no_item_name));
                    mNameEditText.requestFocus();
                    return;
                }
                if (isEmpty(mPriceEditText.getText().toString())) {
                    showToast(getString(R.string.error_no_price));
                    mPriceEditText.requestFocus();
                    return;
                }
                if (isEmpty(mSupplierNameEditText.getText().toString())) {
                    showToast(getString(R.string.error_no_supplier_name));
                    mSupplierNameEditText.requestFocus();
                    return;
                }
                if (isEmpty(mSupplierMailEditText.getText().toString())) {
                    showToast(getString(R.string.error_no_supplier_email));
                    mSupplierMailEditText.requestFocus();
                    return;
                }
                if (isEmpty(mSupplierPhoneEditText.getText().toString())) {
                    showToast(getString(R.string.error_no_supplier_phone));
                    mSupplierPhoneEditText.requestFocus();
                    return;
                }
                String imageUriString = null;
                if (mImageUri != null) {
                    imageUriString = mImageUri.toString();
                }
                if (TextUtils.isEmpty(imageUriString)) {
                    showToast(getString(R.string.error_no_item_image));
                    mSelectImageButton.requestFocus();
                    return;
                }

                // Prepare variables
                mItemName = mNameEditText.getText().toString().trim();
                mPrice = Integer.parseInt(mPriceEditText.getText().toString());
                mSupplierName = mSupplierNameEditText.getText().toString().trim();
                mSupplierPhone = mSupplierPhoneEditText.getText().toString().trim();
                mSupplierMail = mSupplierMailEditText.getText().toString().trim();

                // Zero initial quantity for new items
                mQuantity = 0;

                // Avoid null pointer exception if no image selected
                if (mImageUri != null) {
                    mImageUriString = mImageUri.toString();
                } else {
                    mImageUriString = "";
                }

                // Checks for correct values

                // Check for too high number in price
                if (mPrice > Utils.MAX_PRICE) {
                    showToast(String.format(getString(R.string.price_too_high), Utils.MAX_PRICE));
                    mPriceEditText.requestFocus();
                    return;
                }
                // Check for valid phone and email, if present
                if (!isEmpty(mSupplierPhone) && !Utils.isValidPhone(mSupplierPhone)) {
                    // Phone Number is not valid
                    showToast(getString(R.string.phone_not_valid));
                    mSupplierPhoneEditText.requestFocus();
                    return;
                }
                if (!isEmpty(mSupplierMail) && !Utils.isValidEmail(mSupplierMail)) {
                    // Mail address is not valid
                    showToast(getString(R.string.email_not_valid));
                    mSupplierMailEditText.requestFocus();
                    return;
                }

                // Prepare values for the new row
                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_ITEM_NAME, mItemName);
                values.put(ItemEntry.COLUMN_ITEM_QUANTITY, mQuantity);
                values.put(ItemEntry.COLUMN_ITEM_PRICE, mPrice);
                values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME, mSupplierName);
                values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE, mSupplierPhone);
                values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL, mSupplierMail);
                values.put(ItemEntry.COLUMN_ITEM_IMAGE_URI, mImageUriString);

                Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    showToast(getString(R.string.error_inserting_item));
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    showToast(getString(R.string.new_record_inserted));
                    // Finish activity and return to main activity
                    finish();
                }
            }
        });
    }

    /**
     * Helper method to show a Toast
     *
     * @param toastMessage The message to show
     */
    private void showToast(String toastMessage) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
    }

}

