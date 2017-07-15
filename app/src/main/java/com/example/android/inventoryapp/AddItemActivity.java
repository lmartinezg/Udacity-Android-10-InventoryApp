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

public class AddItemActivity extends AppCompatActivity {

    // Uri to local storage image
    Uri mImageUri;
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
    private ImageButton mSelectImage;
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
        mSelectImage = (ImageButton) findViewById(R.id.select_image_bt);
        mSaveButton = (Button) findViewById(R.id.save_bt);

        setupSelectImageListener();
        setupSaveListener();
    }

    // Select an image from the gallery
    private void setupSelectImageListener() {
        mSelectImage.setOnClickListener(new View.OnClickListener() {
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

                // Check for mandatory values
                String toastMessage = null;
                if (TextUtils.isEmpty(mNameEditText.getText().toString())) {
                    toastMessage = getString(R.string.error_no_item_name);
                }
                if (TextUtils.isEmpty(mPriceEditText.getText().toString())) {
                    toastMessage = getString(R.string.error_no_price);
                }
                if (TextUtils.isEmpty(mSupplierNameEditText.getText().toString())) {
                    toastMessage = getString(R.string.error_no_supplier_name);
                }
                // If error, show toast and return
                if (!(TextUtils.isEmpty(toastMessage))) {
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepare variables
                mItemName = mNameEditText.getText().toString().trim();
                mPrice = Integer.parseInt(mPriceEditText.getText().toString());
                mSupplierName = mSupplierNameEditText.getText().toString().trim();
                mSupplierPhone = mSupplierPhoneEditText.getText().toString().trim();
                mSupplierMail = mSupplierMailEditText.getText().toString().trim();

                // Avoid null pointer exception if no image selected
                if (mImageUri != null) {
                    mImageUriString = mImageUri.toString();
                } else {
                    mImageUriString = "";
                }

                // Check for too high number in price
                if (mPrice > Utils.MAX_PRICE) {
                    Toast.makeText(getApplicationContext(),
                            String.format(getString(R.string.price_too_high), Utils.MAX_PRICE),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // Check for valid phone and email, if present
                if (!TextUtils.isEmpty(mSupplierPhone) && !Utils.isValidPhone(mSupplierPhone)) {
                    // Phone Number is not valid
                    Toast.makeText(getApplicationContext(), R.string.phone_not_valid,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!TextUtils.isEmpty(mSupplierMail) && !Utils.isValidEmail(mSupplierMail)) {
                    // Mail address is not valid
                    Toast.makeText(getApplicationContext(), R.string.email_not_valid,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepare values for the new row
                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_ITEM_NAME, mItemName);
                values.put(ItemEntry.COLUMN_ITEM_PRICE, mPrice);
                values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME, mSupplierName);
                values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE, mSupplierPhone);
                values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL, mSupplierMail);
                values.put(ItemEntry.COLUMN_ITEM_IMAGE_URI, mImageUriString);

                Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(getApplicationContext(), R.string.error_inserting_item,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(getApplicationContext(), R.string.new_record_inserted,
                            Toast.LENGTH_SHORT).show();
                    // Finish activity and return to main activity
                    finish();
                }
            }
        });
    }
}
