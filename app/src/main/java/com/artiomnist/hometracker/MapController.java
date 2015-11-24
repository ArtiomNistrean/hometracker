package com.artiomnist.hometracker;

import android.app.FragmentManager;
import android.os.Bundle;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created on 24/11/2015.
 * @author www.artiomnist.com
 *
 * Controller Class for the Map.
 */
public class MapController {

    private GoogleMap map = null;

    /**
     * Initialises the map if it is possible to initialise. (Google Play Services APK must be
     * correctly) This is only done if the map has not already been instantiated. This ensures that
     * only one instance of {@link #setUpMap()}. This is done when {@link #map} is not null.
     *
     * If it isn't installed {@link MapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     *
     * Users may return to the FragmentActivity after the prompt and correctly enabling the Google
     * Play service. The FragmentActivity may not have been entirely destroyed. Therefore, it is
     * most likely paused or stopped. {@link #setUpMapIfNull(FragmentManager)} ()} Should be called
     * again to guarantee that a map is created. This is best to call it in the OnResume Method in
     * the corresponding activity.
     *
     * @param fragmentManager requires a FragmentManager to set up the map.
     */
    public void setUpMapIfNull(FragmentManager fragmentManager) {


        if (map == null) {
            // Attempt to get the map.
            map = ((MapFragment) fragmentManager.findFragmentById(R.id.MainMapID)).getMap();

            // Checking if successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    /**
     * Method adds Markers to the Map.
     *
     * This method must only be called once! It should be called when it is sure that
     * {@link #map} is not null.
     *
     */
    public void setUpMap() {
        map.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Marker"));
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
