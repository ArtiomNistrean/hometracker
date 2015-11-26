package com.artiomnist.hometracker;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
     * If it isn't installed {@link SupportMapFragment} (and
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
            map = ((SupportMapFragment) fragmentManager.findFragmentById(R.id.MainMapID)).getMap();

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


        setHomeLocation(); // Adds the Marker
        setMapStyle(); // Sets the Map Type / Style
        setBuildings(); // Sets the 3D Buildings on Map

        setZoomLevel();

    }

    public void refreshMap() {
        map.clear();
        setUpMap();
    }

//    TODO CLEAN THIS THE FUCK UP
    private void setHomeLocation() {
        if (MainActivity.mobileConnected || MainActivity.wifiConnected) {
            if (IsInternetAvailable()) {
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
                // CONNECTED BUT NO INTERNET ACCESS TODO
            }
        } else {
            // SHOW NE TODO
        }
    }

    private boolean IsInternetAvailable() {
//      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//
//      StrictMode.setThreadPolicy(policy);

        try {
            InetAddress ipAddr = new NetTask().execute("www.google.com").get();

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void setZoomLevel() {
        model.getZoomLevel();

        //CameraPosition cameraPosition = new CameraPosition.Builder()
        //        .target(new LatLng(40.76793169992044, -73.98180484771729))
        //        .zoom(17)
        //        .bearing(90)
        //        .tilt(30)
        //        .build();

        //map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private void setMapStyle() {
        int mapType;

        switch (model.getMapType()) {
            case "Normal":
                mapType = GoogleMap.MAP_TYPE_NORMAL;
                break;
            case "Hybrid":
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                break;
            case "Satellite":
                mapType = GoogleMap.MAP_TYPE_SATELLITE;
                break;
            case "Terrain":
                mapType = GoogleMap.MAP_TYPE_TERRAIN;
                break;
            default:
                mapType = GoogleMap.MAP_TYPE_NORMAL;
                break;
        }

        map.setMapType(mapType);
    }

    private void setBuildings() {
        map.setBuildingsEnabled(model.getBuildingsEnabled());
    }

    public boolean getLocationError() {
        return validLocation;
    }

    public MapModel getMapModel() {
        return model;
    }

}
