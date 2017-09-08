package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.example.android.inventory.data.DatabaseContract.InventoryItems;


public class ItemProvider extends ContentProvider {
    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    private static final int ALL_ITEMS = 100;
    private static final int ITEM_ID = 101;

    private static final UriMatcher itemUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        itemUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_ITEMS, ALL_ITEMS);
        itemUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        switch (itemUriMatcher.match(uri)) {
            case ALL_ITEMS:
                cursor = db.query(InventoryItems.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs, null, null,
                        sortOrder);
                break;
            case ITEM_ID:
                // we only want to traverse the id's, hence:
                selection = InventoryItems._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(InventoryItems.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs, null, null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Sorry, no match for you. Uri was: " + uri);
        }

        try {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (itemUriMatcher.match(uri)) {
            case ALL_ITEMS:
                return InventoryItems.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryItems.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Uri " + uri + " couldn't get matched");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Checking for right match
        // inserting is only possible for Base Type
        if (itemUriMatcher.match(uri) != ALL_ITEMS) throw new IllegalArgumentException("Wrong Type");
        // Checking for valid entries
        if (values.getAsString(InventoryItems.COLUMN_NAME) == null ||

                values.getAsInteger(InventoryItems.COLUMN_QUANTITY) == null ||
                values.getAsInteger(InventoryItems.COLUMN_QUANTITY) < 0 ||

                values.getAsInteger(InventoryItems.COLUMN_PRICE) == null ||
                values.getAsInteger(InventoryItems.COLUMN_PRICE) < 0 ) {

            throw new IllegalArgumentException("At least one Information is missing or invalid");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long insertId = db.insert(InventoryItems.TABLE_NAME, null, values);
        if (insertId == 0){
            Log.e(LOG_TAG, "Error inserting Uri " + uri);
            return null;
        }

        // notify Content Resolver about the changed Entry found under uri
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, insertId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // the return value
        int rowsDeleted;
        final int match = itemUriMatcher.match(uri);
        switch (match) {
            case ITEM_ID:
                rowsDeleted = db.delete(InventoryItems.TABLE_NAME, selection, selectionArgs);
                break;
            case ALL_ITEMS:
                rowsDeleted = db.delete(InventoryItems.TABLE_NAME, null, null);
                break;
            default:
                throw new IllegalArgumentException("No matching Uri found for uri " + uri);
        }

        if (rowsDeleted != 0) getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (itemUriMatcher.match(uri)) {
            case ALL_ITEMS:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEM_ID:
                selection = InventoryItems._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("No uri match found for " + uri);
        }
    }

    private int updateItem(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Checking for emptiness and valid input of values
        if (values.size() == 0) return 0;

        if (values.containsKey(InventoryItems.COLUMN_NAME)) {
            if (values.getAsString(InventoryItems.COLUMN_NAME) == null) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
        }

        if (values.containsKey(InventoryItems.COLUMN_QUANTITY)) {
            int quantity = values.getAsInteger(InventoryItems.COLUMN_QUANTITY);
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be empty");
            }
        }

        if (values.containsKey(InventoryItems.COLUMN_PRICE)) {
            int price = values.getAsInteger(InventoryItems.COLUMN_PRICE);
            if (price < 0) {
                throw new IllegalArgumentException("Price cannot be empty");
            }
        }

        if (values.containsKey(InventoryItems.COLUMN_IMAGE)) {
            if (values.getAsByteArray(InventoryItems.COLUMN_IMAGE) == null){
                throw new IllegalArgumentException("Image was null");
            }
        }

        // update-time
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(InventoryItems.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
