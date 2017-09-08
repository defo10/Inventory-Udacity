package com.example.android.inventory;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.DatabaseContract;
import com.example.android.inventory.data.DatabaseContract.InventoryItems;
import com.example.android.inventory.data.DbHelper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.data;
import static android.R.attr.editable;
import static android.R.attr.id;
import static android.R.attr.value;
import static android.R.attr.x;

public class EditActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    // variables for photo saving
    private static final int CAMERA_REQUEST = 1888;

    public static final int EXISTING_LOADER = 0;
    private Uri currentItemUri;

    private ImageView imageView;
    private EditText nameEditText;
    private CustomEditText priceEditText;

    private TextView idTextView;
    private TextView quantityTextView;

    private boolean edited = false;

    // change edited when any view got touched
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            edited = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // when an item was clicked, currentItemUri has content
        currentItemUri = getIntent().getData();

        getSupportActionBar().setElevation(4);

        // find all Views
        imageView = (ImageView) findViewById(R.id.image_editor_big);

        nameEditText = (EditText) findViewById(R.id.include).findViewById(R.id.edittext_name);
        priceEditText = (CustomEditText) findViewById(R.id.include).findViewById(R.id.edittext_price);

        idTextView = (TextView) findViewById(R.id.include).findViewById(R.id.id_textview);
        quantityTextView = (TextView) findViewById(R.id.include).findViewById(R.id.quantity_textview);

        // set onTouchListener for name and price
        nameEditText.setOnTouchListener(touchListener);
        // Check for Touched
        priceEditText.setOnTouchListener(touchListener);
        // Format to money format
        priceEditText.addTextChangedListener(new PriceTextWatcher(priceEditText));
        // delete press is handled in the custom EditText-class

        // change title according to state
        if (currentItemUri == null) {
            setTitle("Add an Item");
            idTextView.setText("#");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit Item");
            getSupportLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }
    }

    @Override
    protected void onStop() {
        edited = false;
        super.onStop();
    }

    //region Buttons
    // I find it more structured if I use the OnClick-Option in the XML's instead of
    // defining an OnCLickListener in the onCreate-Method
    public void decrementQuantity(View v){
        String currentValueString = quantityTextView.getText().toString();
        int valueInt = Integer.parseInt(currentValueString);
        if (valueInt > 0) {
            valueInt = valueInt - 1;
        }
        quantityTextView.setText(String.valueOf(valueInt));
        edited = true;
    }

    public void incrementQuantity(View v){
        String currentValueString = quantityTextView.getText().toString();
        int valueInt = Integer.parseInt(currentValueString);
        int newValue = valueInt + 1;
        System.out.println("New Value is " + valueInt);
        quantityTextView.setText(String.valueOf(newValue));
        edited = true;
    }

    public void order(View v){
        if (edited){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Some changes were detected. How do you want to proceed, before ordering a new item?");
            builder.setPositiveButton("Save changes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (dialog != null) {
                        if (!saveChanges()) dialog.dismiss();
                        fireOffMailIntent();
                        dialog.dismiss();
                    }
                }
            });
            builder.setNegativeButton("Don't save changes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        } else {
            fireOffMailIntent();
        }

    }

    public void fireOffMailIntent(){
        // get all the values for our mail-intent
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (pref.getString(getString(R.string.MAIL), null).trim().equals("") ||
                pref.getString(getString(R.string.NAME), null).equals("")){
            Toast.makeText(EditActivity.this, "Please define the mail and name in the settings", Toast.LENGTH_SHORT).show();

        } else {
            // fire off the mail intent
            String mail = pref.getString(getString(R.string.MAIL), null).trim();
            String name = pref.getString(getString(R.string.NAME), null);

            String itemName = nameEditText.getText().toString();

            String text = "Hello,\n I'd like to order the following:\n\n" +
                    "\tname:\t" + itemName + "\n" +
                    "\tquantity:\t" + "\n" +
                    "\n\nSincerely," + "\n" +
                    name;


            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", mail, null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order of " + itemName + " | " + name);
            emailIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(emailIntent, "Send email"));
            Toast.makeText(this, "Please enter the currency", Toast.LENGTH_LONG).show();
        }
    }

    //endregion

    //region Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    // called when invalidateOPtionsMenu is called
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_single_entry_edit);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_single_entry_edit:
                showDeleteConfirmationDialog();
                return true;
            case R.id.saveChanges_edit:
                boolean savingHasWorked = saveChanges();
                if (savingHasWorked) finish();
                return true;
            case R.id.make_image_edit:
                dispatchTakePictureIntent();
                edited = true;
                return true;
            case android.R.id.home:
                if (!edited) {
                    NavUtils.navigateUpFromSameTask(this);
                } else {
                    // OnClickListener for the unsaved changes dialog (as called below)
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to parent activity.
                                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                                }
                            };
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!edited) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
    //endregion

    //region Options-Helper
    // Update Entry
    private boolean saveChanges(){
        // get name
        String nameString = nameEditText.getText().toString().trim();
        if (nameString.equals("")){
            Toast.makeText(this, "Don't forget the name", Toast.LENGTH_SHORT).show();
            return false;
        }

        // get price and validate
        String priceString = priceEditText.getText().toString().replaceAll("\\D", "");
        int price;
        try{
            //double priceAsDouble = Double.valueOf(priceString);
            //price = (int) priceAsDouble * 100;
            price = Integer.valueOf(priceString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Price must contain numbers", Toast.LENGTH_SHORT).show();
            return false;
        }

        // get Quantity and validate
        String currentQuantityString = quantityTextView.getText().toString();
        int quantityInt;
        try{
            quantityInt = Integer.parseInt(currentQuantityString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantity must contain numbers", Toast.LENGTH_SHORT).show();
            return false;
        }

        // get Image and validate
        Bitmap imageBit = null;
        try {
            imageBit = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } catch (Exception e){
            Log.e("EditActivity", "Error getting Drawable in saveChanges()");
        }

        if (imageBit == null) {
            Toast.makeText(this, "Don't forget to make a picture", Toast.LENGTH_SHORT).show();
            return false;
        }
        byte[] imageAsByte = DbHelper.getAsByteArray(imageBit);

        // check whether updating is needed
        if (currentItemUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)){
            // Since no fields were modified, we can return early without creating a new pet.
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryItems.COLUMN_NAME, nameString);
        values.put(InventoryItems.COLUMN_PRICE, price);
        values.put(InventoryItems.COLUMN_QUANTITY, quantityInt);
        values.put(InventoryItems.COLUMN_IMAGE, imageAsByte);

        // update or add
        if (currentItemUri == null){
            //add
            Uri newUri = getContentResolver().insert(InventoryItems.CONTENT_URI, values);
            if (newUri == null){
                Toast.makeText(this, "Error saving Item", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Toast.makeText(this, "Succesfully saved", Toast.LENGTH_SHORT).show();
            }
        } else {
            // update
            int rowsUpdated = getContentResolver().update(currentItemUri, values, null, null);
            if (rowsUpdated == 0) {
                Toast.makeText(this, "Error updating Item", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Toast.makeText(this, "Item successfully updated", Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you wish to delete this item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteEntry();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteEntry(){
        // only perform when editing
        if (currentItemUri != null){
            String selection = InventoryItems._ID + " = ?";
            String[] selectionArgs = {String.valueOf(ContentUris.parseId(currentItemUri))};
            int rowsDeleted = getContentResolver().delete(currentItemUri, selection, selectionArgs);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this,"Error deleting item", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Some changes were detected. Do you want to save them?");
        builder.setPositiveButton("Don't save them", discardButtonListener);
        builder.setNegativeButton("Save them", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    boolean savingHasWorked = saveChanges();
                    if (savingHasWorked) finish();
                    dialog.dismiss();
                }
            }
        });

        builder.create().show();
    }

    //endregion

    //region helper methods for image (called in options)

    private void dispatchTakePictureIntent(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }

    //endregion

    //region LoaderManager, used when entry gets edited
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                DatabaseContract.InventoryItems._ID,
                DatabaseContract.InventoryItems.COLUMN_NAME,
                DatabaseContract.InventoryItems.COLUMN_PRICE,
                DatabaseContract.InventoryItems.COLUMN_QUANTITY,
                DatabaseContract.InventoryItems.COLUMN_IMAGE
        };

        return new CursorLoader(this,
                currentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // make sure cursor is not null
        if (data == null || data.getCount() < 1) return;

        // when loader is initialized, we want to move to the first/ next cursor
        // current Cursor shows at the columns
        data.moveToFirst();

        // find columns of attributes
        int indexID = data.getColumnIndex(InventoryItems._ID);
        int indexName = data.getColumnIndex(InventoryItems.COLUMN_NAME);
        int indexPrice = data.getColumnIndex(InventoryItems.COLUMN_PRICE);
        int indexQt = data.getColumnIndex(InventoryItems.COLUMN_QUANTITY);
        int indexIm = data.getColumnIndex(InventoryItems.COLUMN_IMAGE);

        // extract values from Cursor
        int id = data.getInt(indexID);
        String name = data.getString(indexName);
        int price = data.getInt(indexPrice);
        int quant = data.getInt(indexQt);
        // extract image from byte[]
        byte[] imageAsByte = data.getBlob(indexIm);
        Bitmap image = DbHelper.getBitmap(imageAsByte);

        // set Views
        nameEditText.setText("" + name);
        priceEditText.setText("" + price);
        idTextView.setText("" + id);
        quantityTextView.setText("" + quant);
        imageView.setImageBitmap(image);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        priceEditText.setText("" + 0);
        idTextView.setText("");
        quantityTextView.setText("");
    }
    //endregion
}
