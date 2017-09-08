package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.android.inventory.data.DatabaseContract.InventoryItems;

import java.io.ByteArrayOutputStream;

import static android.R.attr.bitmap;


public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TABLE =  "CREATE TABLE " + InventoryItems.TABLE_NAME + " ("
                + InventoryItems._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryItems.COLUMN_NAME + " TEXT NOT NULL, "
                + InventoryItems.COLUMN_PRICE + " INTEGER, "
                + InventoryItems.COLUMN_QUANTITY + " INTEGER, "
                + InventoryItems.COLUMN_IMAGE + " BLOB);";

        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No upgrade foreseeable in the near future
    }

    /**
     * Utility methods to store and retrieve images into the database
     */

    public static byte[] getAsByteArray(Bitmap bm){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static Bitmap getBitmap(byte[] image){
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
