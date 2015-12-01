package com.artiomnist.hometracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 *
 * Created on 24/11/2015.
 * @author www.artiomnist.com
 *
 * This class extends the PreferenceActivity. This class represents the Settings for the Application
 * This Activity provides a settings UI, with an inversed colour style, for users to specify their
 * settings. Such settings are: the Home Location, The Map Style, The Zoom Type / Level and the use
 * of 3D Buildings in the Map.
 */
public class SettingsActivity extends PreferenceActivity
        implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Method Loads the XML preferences file.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }


    /**
     * Method registers a callback to be invoked whenever a user changes a preference.
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Method unregisters the listener set in the {@link #onResume()} method. It is always best
     * practice to unregister a listener when the application isn't using it. This cuts down on
     * unnecessary System overheads. This is done in the {@link #onPause()} method.
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    // Fires when the user changes a preference.

    /**
     * Method that detects a SharedPreference change. Method ultimately changes the
     * {@link MainActivity#refreshDisplay} to indicate that the screen needs to be refreshed.
     * The method additionally changes the 'Home Location' Shared Preference to deal with user
     * input errors. This is only done if the user has entered an empty Home Location. The location
     * is set back to the default of "University of Exeter".
     *
     * @param sharedPreferences the changed preference.
     * @param key the key value of the shared preference.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (sharedPreferences.getString("home_address", "University of Exeter").equals("")
                || sharedPreferences.getString("home_address", "University of Exeter").isEmpty()) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("home_address", "University of Exeter");
            editor.commit();

        }
        MainActivity.refreshDisplay = true;
    }
}
