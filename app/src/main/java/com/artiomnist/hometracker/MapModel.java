package com.artiomnist.hometracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created on 25/11/2015.
 * @author www.artiomnist.com
 *
 * Model for the Map. This Model is never used in the MainActivity, but is used in synergy with
 * the {@link MapController} class. This Class contains all the methods for retreving the users
 * settings and preferences.
 *
 */
public class MapModel {

    private SharedPreferences shPreferences;
    private GoogleMap map = null;

    /**
     * Constructor for MapModel. Gets the Shared Preferences from the Activity Context that is
     * passed through to the controller, and thus down to the model
     *
     * @param context
     */
    public MapModel (Context context) {
        shPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    /**
     * Method gets the Home string that represents the users Home. If the preference is not set the
     * defaulting value of University Of Exeter is returned instead.
     *
     * @return String that represents the home value.
     */
    public String getHome() {
        return shPreferences.getString("home_address", "University Of Exeter");
    }

    /**
     * Method gets the Zoom level that defines how close to zoom into the map on start up and on
     * finding home. If the Zoom String is "Dynamic" Then 12 is returned. 'Why' is explained in the
     * {@link MapController} in the method {@link MapController#setZoomLevel()}
     *
     * @return Integer that represents the zoom level.
     */
    public Integer getZoomLevel() {
        String zoom = shPreferences.getString("zoom_level", "14");

        if (zoom.equals("dynamic")) {
            return 12;
        } else {
            return Integer.parseInt(zoom);
        }

    }

    /**
     * Method gets the String value that defines the style of the map set in the Preferences.
     *
     * @return String value representing the style of the Map.
     */
    public String getMapType() {
        return shPreferences.getString("map_type", "Normal");
    }

    /**
     * Method gets the preference saved for the use of 3D buildings on the map.
     *
     * @return boolean true representing 3D buildings are allowed or boolean false representing 3D
     * buildings are not allowed.
     */
    public boolean getBuildingsEnabled() {
        return shPreferences.getBoolean("enable_buildings", false);
    }

    /**
     * Method returns the Map object. This method is used in the {@link MapController} to set up the
     * map.
     *
     * @return GoogleMap Object the instance of the Map.
     */
    public GoogleMap getMap() {
        return map;
    }

}
