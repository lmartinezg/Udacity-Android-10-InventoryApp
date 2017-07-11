package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

import static com.example.android.inventoryapp.R.id.quantity;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */
public class ItemCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = ItemCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        // Find the columns of item attributes that we're interested in
        final int idColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
        final int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        final int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        final int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);

        // Read the item attributes from the Cursor for the current item
        String itemName = cursor.getString(nameColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);
        int itemPrice = cursor.getInt(priceColumnIndex);

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        quantityTextView.setText(String.valueOf(itemQuantity));
        priceTextView.setText(String.valueOf(itemPrice));

        // Enable or disable the sale button depending on quantity greater than zero
        saleButton.setEnabled(itemQuantity != 0);

        // Save the position for this item
        saleButton.setTag(cursor.getPosition());

        // Setup a listener for the sale button
        saleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the position saved previously with setTag()
                int position = (int) v.getTag();

                // Move the cursor to the selected item position
                cursor.moveToPosition(position);

                // Retrieve quantity from cursor and update only if greater than zero (redundant
                // sanity check)
                int quantity = cursor.getInt(quantityColumnIndex);
                if (quantity <= 0) {
                    Toast.makeText(context, R.string.error_no_quantity_to_sell, Toast
                            .LENGTH_SHORT).show();
                } else {
                    long id = cursor.getInt(idColumnIndex);
                    Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity - 1);

                    int rowsAffected = context.getContentResolver().update(currentItemUri, values, null, null);
                    if (rowsAffected != 1) {
                        Toast.makeText(context, R.string.error_quantity_update, Toast
                                .LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.quantity_update_successful, Toast
                                .LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}
