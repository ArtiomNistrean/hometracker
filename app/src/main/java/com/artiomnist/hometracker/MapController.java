package com.artiomnist.hometracker;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * Created on 24/11/2015.
 * @author www.artiomnist.com
 *
 * Controller Class for the Map.
 */
public class MapController {

    private GoogleMap map;
    private MapModel model;
    private Geocoder geocoder;
    private boolean validLocation;

    public MapController(Context context) {
        model = new MapModel(context);
        geocoder = new Geocoder(context);
        map = model.getMap();
    }

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
        //map.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Marker"));
        setHomeLocation();
        // Zoom and Stuff
    }

    public void refreshMap() {
        map.clear();
        setUpMap();
    }

//    TODO CLEAN THIS THE FUCK UP
    private void setHomeLocation() {
        if (MainActivity.mobileConnected || MainActivity.wifiConnected) {
            // get the home string
            String location = model.getHome();

            if (location.equals("") || location.isEmpty()) {
                location = "University of Exeter";
            }

            List<Address> addressBook = null;
            try {
                // get the List<Addresses>
                addressBook = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Check to see if any address where found.
            if (addressBook.isEmpty()) {
                validLocation = false;
                // Tell user that they have entered an invalid location

            } else {
                // Get the First and Only Address
                validLocation = true;
                Address homeLocation = addressBook.get(0);

                // Get the Lat and Long Co-ords
                LatLng coords = new LatLng(homeLocation.getLatitude(), homeLocation.getLongitude());

                // Add the Marker
                map.addMarker(new MarkerOptions().position(coords).title("Home"));
            }
        } else {
            // SHOW NETWORK ERROR
        }
    }

    public boolean getLocationError() {
        return validLocation;
    }

    public MapModel getMapModel() {
        return model;
    }

}
