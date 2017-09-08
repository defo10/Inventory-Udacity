package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.inventory.data.DatabaseContract;
import com.example.android.inventory.data.DatabaseContract.InventoryItems;
import com.example.android.inventory.data.DbHelper;

import java.text.NumberFormat;

import static com.example.android.inventory.R.id.decrement_list_button;
import static com.example.android.inventory.R.id.list_clickaction;

public class ItemCursorAdapter extends CursorAdapter {
    public ItemCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        view.setTag(vh);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        ViewHolder vh = (ViewHolder) view.getTag();
        // name
        String name = cursor.getString(cursor.getColumnIndex(InventoryItems.COLUMN_NAME));
        vh.nameTextView.setText(name);

        // price
        double value = cursor.getInt(cursor.getColumnIndex(InventoryItems.COLUMN_PRICE));
        String text = NumberFormat.getCurrencyInstance().format((value/100));
        vh.priceTextView.setText(text);

        // Quantity
        // quantity is used again in the Button-OnClickListener below
        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryItems.COLUMN_QUANTITY));
        vh.quantity_view.setText(String.valueOf(quantity));

        // image
        byte[] thumbnailAsByte = cursor.getBlob(cursor.getColumnIndex(InventoryItems.COLUMN_IMAGE));
        Bitmap thumbnail = DbHelper.getBitmap(thumbnailAsByte);
        vh.imageView.setImageBitmap(thumbnail);

        // default onTouchListener
        final int id = cursor.getInt(cursor.getColumnIndex(InventoryItems._ID));
        vh.viewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditActivity.class);
                // this is where we know from that the want to edit an entry and not
                // create a new one
                intent.setData(ContentUris.withAppendedId(DatabaseContract.InventoryItems.CONTENT_URI, id));
                context.startActivity(intent);
            }
        });

        vh.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditActivity.class);
                // this is where we know from that the want to edit an entry and not
                // create a new one
                intent.setData(ContentUris.withAppendedId(DatabaseContract.InventoryItems.CONTENT_URI, id));
                context.startActivity(intent);
            }
        });

        vh.quantity_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditActivity.class);
                // this is where we know from that the want to edit an entry and not
                // create a new one
                intent.setData(ContentUris.withAppendedId(DatabaseContract.InventoryItems.CONTENT_URI, id));
                context.startActivity(intent);
            }
        });

        // decrement listener
        vh.decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity <= 0) return;
                ContentValues cv = new ContentValues();
                cv.put(InventoryItems.COLUMN_QUANTITY, quantity - 1);
                context.getContentResolver().update(
                        ContentUris.withAppendedId(InventoryItems.CONTENT_URI, id),
                        cv,
                        null, null);
            }
        });

    }

    private static class ViewHolder{
        private final TextView nameTextView;
        private final TextView priceTextView;
        private final TextView quantity_view;
        private final ImageView imageView;
        private final RelativeLayout viewGroup;
        private final Button decrementButton;

        private ViewHolder(View view){
            nameTextView = (TextView) view.findViewById(R.id.name);
            priceTextView = (TextView) view.findViewById(R.id.price_and_currency);
            quantity_view = (TextView) view.findViewById(R.id.quantity_view);
            imageView = (ImageView) view.findViewById(R.id.image_small);
            viewGroup = (RelativeLayout) view.findViewById(list_clickaction);
            decrementButton = (Button) view.findViewById(decrement_list_button);
        }
    }
}
