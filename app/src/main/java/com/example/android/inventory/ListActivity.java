package com.example.android.inventory;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventory.data.DatabaseContract;

public class ListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;
    ItemCursorAdapter mItemCursorAdapter;

    public static FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });

        ListView itemsListView = (ListView) findViewById(R.id.list);
        TextView emptyListText = (TextView) findViewById(R.id.noContent);

        itemsListView.setEmptyView(emptyListText);
        mItemCursorAdapter = new ItemCursorAdapter(this, null);
        itemsListView.setAdapter(mItemCursorAdapter);

        getSupportLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    // used when opening the settings fragment
    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
            fab.setVisibility(View.VISIBLE);
        }

    }

    //region OptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete_all_entries_list) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you wish to delete all items?");
            builder.setPositiveButton("Delete All", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    int rowsDeleted = getContentResolver().delete(DatabaseContract.InventoryItems.CONTENT_URI, null, null);
                    Log.v("ListActivity", rowsDeleted + " rows deleted from database");
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
            return true;
        }

        if (id == R.id.settings){
            fab.setVisibility(View.INVISIBLE);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_list_activity, new Settings())
                    .addToBackStack(null)
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region DatabaseLoader
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
                DatabaseContract.InventoryItems.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mItemCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemCursorAdapter.swapCursor(null);
    }
    //endregion
}
