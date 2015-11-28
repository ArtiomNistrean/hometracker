package com.artiomnist.hometracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

/**
 * Created on 25/11/2015.
 *
 * Model for the Map
 */
public class MapModel {
    private SharedPreferences shPreferences;
    private GoogleMap map = null;

    /**
     * Constructor for MapModel
     *
     * @param context
     */
    public MapModel (Context context) {
        shPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getHome() {
        return shPreferences.getString("home_address", "University Of Exeter");
    }

    public Integer getZoomLevel() {
        String zoom = shPreferences.getString("zoom_level", "14");

        if (zoom.equals("dynamic")) {
            return 12;
        } else {
            return Integer.parseInt(zoom);
        }

    }

    public String getMapType() {
        return shPreferences.getString("map_type", "Normal");
    }

    public boolean getBuildingsEnabled() {
        return shPreferences.getBoolean("enable_buildings", false);
    }

    /**
     * Method returns the Map.
     *
     * @return GoogleMap the instance of the Map.
     */
    public GoogleMap getMap() {
        return map;
    }

}
