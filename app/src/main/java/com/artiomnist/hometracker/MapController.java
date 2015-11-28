package com.artiomnist.hometracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.InetAddress;
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
    private Marker meMarker;
    private Marker homeMaker;

    public static final int MY_LOCATION_PERMISSION_REQUEST = 1;

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
     * most likely paused or stopped.  The Method Should be called
     * again to guarantee that a map is created. This is best to call it in the OnResume Method in
     * the corresponding activity.
     *
     * @param fragmentManager requires a FragmentManager to set up the map.
     */
    public void setUpMapIfNull(FragmentManager fragmentManager, Context context) {

        if (map == null) {
            // Attempt to get the map.
            map = ((SupportMapFragment) fragmentManager.findFragmentById(R.id.MainMapID)).getMap();

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                MainActivity.isLocationAvailable = true;
                map.setMyLocationEnabled(true);
            } else {
                Activity activity = (Activity) context;
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_PERMISSION_REQUEST);
            }

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

        setMapStyle(); // Sets the Map Type / Style
        setBuildings(); // Sets the 3D Buildings on Map

        // These Require an active connection
        if (MainActivity.mobileConnected || MainActivity.wifiConnected) {
            if (isInternetAvailable()) {
                setHomeLocation(); // Adds the Marker
                setZoomLevel(); // Sets the Camera!
            }
        }

    }


    public void refreshMap() {
        map.clear();
        setMapStyle(); // Sets the Map Type / Style
        setBuildings(); // Sets the 3D Buildings on Map

        if (MainActivity.mobileConnected || MainActivity.wifiConnected) {
            if (isInternetAvailable()) {
                setHomeLocation(); // Adds the Marker
            }
        }
    }

    public void setCurrentLocation(Location currentLocation) {

        if (meMarker != null) {meMarker.remove();}

        LatLng me = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        meMarker = map.addMarker(new MarkerOptions().position(me).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title("me"));

    }

    // TODO MOVE TO MODEL - DEPRECIATED!
    private Location getCurrentLocation() {
        Location meLocation = map.getMyLocation();
        return meLocation;
    }

    // TODO MOVE TO MODEL
    private float getDistanceFromHome(Location current, Location home) {
        return current.distanceTo(home);
    }

    public void setUpDistanceCounter(TextView distanceText, Location currentLocation) {
        if (MainActivity.mobileConnected || MainActivity.wifiConnected) {
            if (isInternetAvailable()) {
                // TODO SET CORRECT THING
                String suffix;
                float distance = getDistanceFromHome(currentLocation, getHomeLocationLocation());

                if (distance >= 1000) {
                    distance = distance / 1000;
                    suffix = "KM";
                } else {
                    suffix = "M";
                }

                double distanceD = Math.round(distance);

                distanceText.setText(String.valueOf(distanceD) + suffix);
            }
        }

    }



//    TODO CLEAN THIS THE FUCK UP
    private void setHomeLocation() {
        LatLng coords = getCoOrdinates(getHomeAddress(model.getHome()));
        homeMaker = map.addMarker(new MarkerOptions().position(coords).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("Home"));
    }

    public Location getHomeLocationLocation() {
        Location homeLocation = new Location("Home!");

        LatLng coords = getCoOrdinates(getHomeAddress(model.getHome()));

        homeLocation.setLatitude(coords.latitude);
        homeLocation.setLongitude(coords.longitude);

        return homeLocation;
    }

    private LatLng getCoOrdinates(Address address) {

        LatLng coordinates;

        try {
            coordinates = new LatLng(address.getLatitude(), address.getLongitude());
            return coordinates;
        } catch (Exception e) {
            System.out.println("Something Went Wrong... Trying Again");
            return getCoOrdinates(address);
        }

    }

    private Address getHomeAddress(String home) {
        Address homeLocation = null;
        //if (MainActivity.mobileConnected || MainActivity.wifiConnected) {

            //if (isInternetAvailable()) {

                if (home.isEmpty() || home.equals("")) {
                    home = "University of Exeter";
                }

                List<Address> addressBook = null;
                try {
                    // get the List<Addresses>
                    addressBook = geocoder.getFromLocationName(home, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Check to see if any address where found.
                if (addressBook == null) {
                    validLocation = false;
                    // Tell user that they have entered an invalid location
                } else {
                    // Get the First and Only Address
                    validLocation = true;

                    homeLocation = addressBook.get(0);

                }
            //}
        //}
        return homeLocation;
    }

    private boolean isInternetAvailable() {

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

    public void setZoomLevel() {
        //if (MainActivity.mobileConnected || MainActivity.wifiConnected) {
            //if (isInternetAvailable()) {
                int zoom = model.getZoomLevel();

//                if (zoom == 12) {
//                    // DYNAMIC ZOOM
//
//
//
//
//                } else {
//
//
//
//                    // Set camera
//                }


        switch (zoom) {
            case 12:
                if (meMarker != null) {
                    LatLngBounds.Builder dynamicZoomBuilder = new LatLngBounds.Builder();
                    dynamicZoomBuilder.include(homeMaker.getPosition());
                    dynamicZoomBuilder.include(meMarker.getPosition());

                    LatLngBounds bounds = dynamicZoomBuilder.build();

                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 500, 500, 5));
                    break;
                } else {
                    // TODO ANIMATE NORMALLY, like it could be wait until you do have a location... but come on...
                }

            default:
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(getCoOrdinates(getHomeAddress(model.getHome())))
                        .zoom(model.getZoomLevel())
                        .tilt(30)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                break;
        }

            //}
        //}

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

    public GoogleMap getMap() {
        return map;
    }

    public boolean getLocationError() {
        return validLocation;
    }

    public MapModel getMapModel() {
        return model;
    }

}
