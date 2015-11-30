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
import java.util.Locale;

/**
 * Created on 24/11/2015.
 * @author www.artiomnist.com
 *
 * Controller Class for the Map. This class is used in synergy with the MapModel class, see
 * {@link MapModel}. Together, they allow for the functionallity behind the MainActivity
 * {@link MainActivity}.
 */
public class MapController {

    // Variables for the MapController Class. Initialised when the Class is constructed.
    private GoogleMap map;
    private MapModel model;
    private Geocoder geocoder;

    // Represents if a location is valid. i.e. If such a location, exists.
    private boolean validLocation;

    // Marker representing users current location.
    private Marker meMarker;
    // Marker representing users chosen home location.
    private Marker homeMaker;

    // The permission location request value.
    public static final int MY_LOCATION_PERMISSION_REQUEST = 1;

    // Default values for Home.
    private static final String DEFAULT_HOME = "University of Exeter";
    private static final double DEFAULT_LONG = -3.535136;
    private static final double DEFAULT_LAT = 50.737147;


    /**
     * Constructor for the MapController Class. Creates a new Model and geocode. Then gets the Map
     * from the model, which should be null. MapController is instantiated in the Main Activity.
     *
     * @param context
     */
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

            // Check if Location Permission is available.
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Permission has been allowed. Set Variables appropriately to indicate this.
                MainActivity.isLocationAvailable = true;
                map.setMyLocationEnabled(true);
            } else {
                // Permission is not found! Prompt the user to give permission for ACCESS_FINE_LOCATION.
                // Result is handled in Main Activity, in the onRequestPermissionsResult method.
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
     * Method 'Sets Up' The map. This means setting the style of the map, setting buildings.
     * After this, If there is network connectivity, the Home marker is set up and the Zoom camera
     * is then animated. The style, 3D buildings and zoom level all depend on what the user has
     * set them to be in the preferences / Settings Activity.
     *
     * This method must only be called once! It should be called when it is sure that
     * {@link #map} is not null. Therefore this is assured in the
     * {@link #setUpMapIfNull(FragmentManager, Context)} method.
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


    /**
     * Method refreshes the map. This method is used in combination with the
     * {@link MainActivity#refreshDisplay} variable indicator. This method would be called in
     * onResume if the display needs to be refreshed, as well as when the user clicks the refresh
     * button in the action bar.
     *
     * The method initially, clears the map of all markers and then sets the map style and the maps
     * 3D buildings. If there is a network connection and there is internet, then the Home markers
     * are set up. This method is similar to {@link #setUpMap()} but doesnt include zoom, and
     * removes existing markers to handle new home locations.
     *
     */
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

    /**
     * Method determines if there is an internet connection connected to the device. This method is
     * called before getting a locaton from the {@link #geocoder}. The device can be connected to
     * a mobile network or even a Wi-Fi network and still not have access to internet. I.E. Need to
     * sign in to the network or just signal issues.
     *
     * This method attempts to get an InetAddress from google.com. This is done using the
     * {@link NetTask} Class. More information on why an Async class is used can be here.
     * If the Ip Address is not empty then there must be internet access and true is returned.
     * Otherwise, false is returned if the IP address is empty or an exception is thrown.
     *
     *
     * @return boolean true or boolean false indicating if an internet connection is available
     */
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

    /**
     * Method sets up the current location of the user. If there already exists a marker that
     * represents the user, then that marker is remove prior to setting a new one. Additionally, the
     * marker is only set, if a current location exists. i.e. if current location is not null.
     *
     * @param currentLocation the current location of the user.
     */
    public void setCurrentLocation(Location currentLocation) {

        if (meMarker != null) {meMarker.remove();}
        if (currentLocation != null) {
            LatLng me = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            meMarker = map.addMarker(new MarkerOptions()
                    .position(me)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .title("Me"));
        }

    }

    /**
     * Method returns the distance from the users current location, to the users home location.
     * Note, both locations must exist and must not be null.
     *
     * @param current the users current location
     * @param home the users home location
     * @return float number representing the distance between the two points.
     */
    private float getDistanceFromHome(Location current, Location home) {
        return current.distanceTo(home);
    }

    /**
     * Method creates the distance counter. The method uses
     * {@link #getDistanceFromHome(Location, Location)} to obtain the distance to show. Depending
     * on this value an appropriate suffix is displayed. E.g M for meters or KM for Kilometers.
     * The value is then rounded to allow for enough screen space to display the distance.
     *
     * @param distanceText the TextView that is to represent the distance.
     * @param currentLocation the current location of the user.
     */
    public void setUpDistanceCounter(TextView distanceText, Location currentLocation) {
        if (MainActivity.mobileConnected || MainActivity.wifiConnected) {
            if (isInternetAvailable()) {

                String suffix;

                float distance = getDistanceFromHome(currentLocation, getHomeLocationLocation());

                if (distance >= 1000) {
                    // Show in Kilometers.
                    distance = distance / 1000;
                    suffix = "KM";
                } else {
                    // Show in Meters.
                    suffix = "M";
                }

                int distanceD = Math.round(distance);

                // Set the Display text.
                distanceText.setText(String.valueOf(distanceD) + suffix);
            }
        }
    }


    /**
     * Method sets up the set home location on the map. Adds a Maker 'homeMarker' to represent this.
     * The Co-ordinate values are obtained by the method {@link #getCoOrdinates(Address)} which
     * in turn uses the method {@link #getHomeAddress(String)} to get a location from the Home
     * String found in the Model.
     */
    private void setHomeLocation() {
        LatLng coords = getCoOrdinates(getHomeAddress(model.getHome()));
        homeMaker = map.addMarker(new MarkerOptions()
                .position(coords)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title("Home"));
    }


    /**
     * Method gets a Location object {@link Location} that represents home. This method is used in
     * obtaining the distance from the current location to the home location. The method assigns
     * respective latitude and longitude coordinates to the location Object.
     *
     * @return Location object of 'Home' with LAT and LONG values.
     */
    public Location getHomeLocationLocation() {
        Location homeLocation = new Location("Home!");

        LatLng coords = getCoOrdinates(getHomeAddress(model.getHome()));

        homeLocation.setLatitude(coords.latitude);
        homeLocation.setLongitude(coords.longitude);

        return homeLocation;
    }


    /**
     * Method obtains Latitude and Longitude for an Address Object. This method is used in the
     * {@link #setHomeLocation()} and {@link #getHomeLocationLocation()} and in setting up the
     * zoom level. It is important that co-ordinates are returned. If The Address object is null,
     * an exception is thrown. This is the case for When a location is not valid. Therefore
     * default co-ordinates are returned instead. These co-ordinates represent The University of
     * Exeter which is the default home Location for this application.
     *
     * @param address the Address object of the address form which to get the co-ordinates.
     * @return LatLng - a set of co-ordinates with LAT and LONG Values
     */
    private LatLng getCoOrdinates(Address address) {

        LatLng coordinates;

        try {
            coordinates = new LatLng(address.getLatitude(), address.getLongitude());
            return coordinates;
        } catch (Exception e) {
            e.printStackTrace(); // For Debugging.
            return new LatLng(DEFAULT_LAT, DEFAULT_LONG); // Return the Default set of Co-ordinates
        }

    }

    /**
     * Method gets the Address object from a String using the {@link Geocoder} object provided by
     * android. This method is used to determine an address from the users home preferences and
     * acts as a base from which to get location coordinates from a string that represents 'home'
     *
     * Initially, the method checks the home String to be empty. If so, the method resorts to the
     * default home string value. (University of Exeter).
     *
     * The method then tries to obtain a List of Address objects from the Geocoder with a Limit of 1
     * This can return a null or empty address book. If this is the case The location is Invalid
     * and the {@link #validLocation} variable is set appropriately. As a consequence, the Default
     * home address is set and returned. Otherwise, the one and only element of the address list is
     * returned.
     *
     * @param home the String that represents 'Home' Should be obtained from the model.
     * @return Address - The Address object which represents the Address location of the String.
     */
    private Address getHomeAddress(String home) {
        Address homeLocation;
        List<Address> addressBook = null;

        // Check input is valid.
        if (home.isEmpty() || home.equals("")) {
            home = DEFAULT_HOME;
        }

        // Get the List of Addresses that match the String
        try {
            addressBook = geocoder.getFromLocationName(home, 1);
        } catch (IOException e) {
            e.printStackTrace(); // Used for Debugging.
        }

        // Check to see if any address where found.
        if (addressBook == null || addressBook.isEmpty()) {
            // Tell user that they have entered an invalid location
            // And set Home to the Default Home.

            validLocation = false;

            homeLocation = new Address(Locale.getDefault());
            homeLocation.setFeatureName(DEFAULT_HOME);
            homeLocation.setLongitude(DEFAULT_LONG);
            homeLocation.setLatitude(DEFAULT_LAT);

        } else {
            // Get the First and Only Address and indicate that it is a valid location.
            validLocation = true;

            homeLocation = addressBook.get(0);
        }

        return homeLocation;
    }

    /**
     * Method sets up the zoom for the map. The zoom is set up depending on what the zoom level is
     * set in the settings. This method is used in {@link #setUpMap()} and in {@link MainActivity}
     * when the "Find Home" Option is selected.
     *
     * If the zoom integer is 12 it represents the Dynamic zoom. Dynamic zoom works by zooming in on
     * map such that both the current location and home location markers are shown on the map. This
     * is done using {@link LatLngBounds} to set up a boundary on which to focus on. Since getting
     * a current location takes a bit of time, or Location services are disabled the case statement
     * we default to animating the camera at zoom level 12. (This is between a Close up and Medium
     * zoom.)
     *
     * If the zoom Integer is not 12 then the camera is animated with a camera position being home
     * coordinates and the zoom level to the value in the model. A tilt of 30 is added to give a
     * more visually appealing angle.
     *
     */
    public void setZoomLevel() {
        int zoom = model.getZoomLevel();

        switch (zoom) {
            case 12:
                if (meMarker != null) {
                    LatLngBounds.Builder dynamicZoomBuilder = new LatLngBounds.Builder();
                    dynamicZoomBuilder.include(homeMaker.getPosition()); // Home
                    dynamicZoomBuilder.include(meMarker.getPosition()); // Current Location

                    LatLngBounds bounds = dynamicZoomBuilder.build();

                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 500, 500, 5));
                    break;
                } else {
                    //
                    /**
                     * TODO: /** Room for future work; Implementing logic for waiting for a current
                     * Todo:  * location by detecting if location permission are allowed
                     * Todo:  * and if there is a network connection thereafter this logic needs to
                     * Todo:  * obtain a location before animating the camera.
                     *
                     */
                }

            default:
                // Create the camera position with Home and Zoom Level found in model
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(getCoOrdinates(getHomeAddress(model.getHome())))
                        .zoom(model.getZoomLevel())
                        .tilt(30)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                break;
        }
    }

    /**
     * Method sets the maps style. The style is set to the one found in the model.
     * (Settings / Preferences). Simple Switch statement matching String to Int. This method is used
     * in {@link #setUpMap()} and {@link #refreshMap()} when creating the map Object.
     *
     */
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

    /**
     * Method sets 3D buildings to True or False depending on the setting value. Setting value is
     * obtained from the model (Settings / Preferences). 3D buildings allow for detailed views of
     * certain building floor levels once zoomed in enough. This method is used in the
     *  {@link #setUpMap()} and {@link #refreshMap()} when creating the map Object.
     *
     */
    private void setBuildings() {
        map.setBuildingsEnabled(model.getBuildingsEnabled());
    }

    /**
     * Method for getting the map Object. Mainly used in the {@link MainActivity}.
     * @return the map - GoogleMap Object
     */
    public GoogleMap getMap() {
        return map;
    }

    /**
     * Method returns if there is a location error. Method is used in the {@link MainActivity}.
     * @return validLocation the boolean value representing if the home location is valid or not.
     */
    public boolean getLocationError() {
        return validLocation;
    }

}
