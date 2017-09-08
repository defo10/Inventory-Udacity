package com.example.android.inventory;


import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class Settings extends PreferenceFragment {

    public Settings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        // PreferenceFragment is not supported by any support library
        // so I had to use the deprecated method
        if (Build.VERSION.SDK_INT > 22){
            view.setBackgroundColor(getResources().getColor(android.R.color.background_light, getActivity().getTheme()));
        } else {
            view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_settings, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.saveChanges_settings) {
            getActivity().onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


}
