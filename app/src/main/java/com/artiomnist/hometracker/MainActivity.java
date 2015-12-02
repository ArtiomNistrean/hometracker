package com.artiomnist.hometracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebViewFragment;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.LocationListener;

/**
 * Created on 23/11/2015
 * @author www.artiomnist.com
 *
 * The Main Activity for this Application. This Activity is Responsible for presenting the Map,
 * Checking network status, and handling menu clicks and displaying relavent Information.
 * The Activity extends an AppCompatActivity and Implements Google Map Interfaces as well as
 * Location Listening Interfaces.
 *
 * This Activity implements Googles Newly Released Google API Client since the android Location
 * Client class and map location services are deprecated. Additionally, considering Android 6.0
 * Users grant permissions to apps while the app is running and not when installing them. API levels
 * lower than 6.0 the Permission will be asked upon installation. This Activity is reponsible with
 * handling the permission request result on run-time.
 *
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private MapController mapC;
    private GoogleMap map;
    private TextView distanceText;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    // Whether there is a Wi-Fi connection.
    public static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    public static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = false;

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();

    // Whether there is Location Services available. It should be noted that this represents
    // Location PERMISSIONS and not if location is turned on in the users Settings!
    public static boolean isLocationAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the Network Receiver.
        IntentFilter filter =  new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);
        updateConnectedFlags();

        // Get the Distance Text.
        distanceText = (TextView)findViewById(R.id.distance_view);

        // Create an Instance of the Controller
        mapC = new MapController(this);
        map = mapC.getMap();

        // Set up the Map.
        mapC.setUpMapIfNull(this.getSupportFragmentManager(), this); // Creates the Map + Updates map variable
        map = mapC.getMap();

        // Google API CLient for Finding Current Location.
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Create the Map.
        SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.MainMapID);
        mf.getMapAsync(this); // calls onMapReady when Loaded
    }


    /**
     * Method Listens for the input from the user for the Permission Request. There is only one
     * permission request, that is, for location services. This method is used in synergy with
     * setUpIfNull method in the {@link MapController} class. If the requestCode is the same as the
     * Location RequestCode, The result can be investigated. If the permission for
     * ACCESS_FINE_LOCATION matches the permissions argument and the grantResult is the same as
     * PERMISSION_GRANTED for this package then it means th user has given access for their Location
     *
     * Therefore, the isLocationAvailable is set to True to resemble this and the Maps location is
     * enabled. An attempt to connect to the Google API Client is also made. Otherwise, If
     * permission is denied, then isLocationAvailable and the maps Location is set to False. Thus,
     * disabling the user location functionality.
     *
     * @param requestCode Request Code given.
     * @param permissions The Permissions to be requested.
     * @param grantResults The results that are given on granted access.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        // Check Result of the Permission Request on Runtime
        if (requestCode == MapController.MY_LOCATION_PERMISSION_REQUEST) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Access has been Granted!
                isLocationAvailable = true;
                mapC.getMap().setMyLocationEnabled(true);
                googleApiClient.connect();

            } else {
                // Access has been denied, Disable Location functionality.
                isLocationAvailable = false;
                mapC.getMap().setMyLocationEnabled(false);
            }
        }
    }

    /**
     * Method checks the connection status, updating the connection flags as necessary and attempts
     * a connection to the Google API Client.
     */
    @Override
    public void onStart() {
        super.onStart();
        updateConnectedFlags();
        googleApiClient.connect();
    }

    /**
     * Method disconnects the Google API client before Stopping.
     */
    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Method unregisters the Network Status Receiver if one has been created when the Application
     * is destroyed and exited.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    /**
     * Method indicates the application is paused.
     */
    @Override
    public void onPause(){
        super.onPause();
    }

    /**
     * Method to deal with the application being resumed. Firstly, the connection status is updated,
     * and the a connection to the Google API Client is created. If there is a need to refresh the
     * display, the the map is refreshed through the controller method {@see refreshMap}. Otherwise,
     * The Map is created if needed. This is done through the controller method {@see setUpMapIfNull}
     *
     * This is the case when the application starts with no Internet Connection but then gets an
     * internet connection. However, the application must also consider the opposite, if it started
     * with an internet connection, but has then lost the connection. The method then checks if it
     * is not connected to any network, and this displays an error page {@link #showConnectionError()}.
     * However, If there is an internet connection, but the location of home is not valid then an
     * appropriate error message is displayed. {@link #showLocationAlert()}
     *
     */
    @Override
    public void onResume() {
        super.onResume();
        updateConnectedFlags();
        googleApiClient.connect();

        if (refreshDisplay) {
            // Refresh the Map
            mapC.refreshMap();
            map = mapC.getMap();
            refreshDisplay = false;
        } else {
            // Set up the map if needed.
            // Creates the Map + Updates map variable
            mapC.setUpMapIfNull(this.getSupportFragmentManager(), this);
            map = mapC.getMap();
        }

        if (!(wifiConnected || mobileConnected)) {
            // No network connection.
            showConnectionError();
        } else if (!mapC.getLocationError()) {
            // Connected to network, but the location is invalid
            showLocationAlert();
        }
    }

    /**
     * Method Inflates the menu; this adds items to the action bar if it is present.
     *
     * @param menu, the Menu.
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Method handles the action bar item clicks. The action bar will automatically handle the
     * clicks on the Home / Up button as long as it is specified in the parent activity.
     *
     * Settings starts a new Settings Activity {@link SettingsActivity}. Takes the user to the
     * settings/ preferences screen
     *
     * About starts a new About Activity with the about file that can be found in the Assets folder.
     *
     * Refresh essentially refreshes the screen. If it is connected to a network, it will remove the
     * connection error screen if it is present, and rebuild the map if need be. If there is no
     * connection, then the ConnectionError will be shown. Upon clicking refresh, the application
     * will also try and get an known location of the user and display it on the map.
     *
     * Find Home will zoom in on the map current home location depending on the zoom level set by
     * the user. Zoom is handled in {@link MapController} in the setZoomLevel() a Map and a home
     * location must be present for this. Thus, this cannot be done in offline mode.
     *
     * @param item the menu item.
     * @return true, if item click, else, The super of onOptionsItemSelected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        ProgressBar spinner = (ProgressBar)findViewById(R.id.map_progressBar);

        switch (id) {
            case R.id.settings:
                // Start settings activity
                Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(settingsActivity);
                return true;

            case R.id.about:
                // Start About page Activity
                Intent i = new Intent(this, AboutActivity.class).putExtra(AboutActivity.EXTRA_FILE,
                        "file:///android_asset/Misc/help.html");
                startActivity(i);
                return true;

            case R.id.refresh:
                // Refresh the Screen.

                updateConnectedFlags(); // Check Network Status

                if (wifiConnected || mobileConnected) {
                    spinner.setVisibility(View.VISIBLE);
                    getFragmentManager().popBackStack(); // Remove Any error that are shown
                    // Set up the Map
                    SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.MainMapID);
                    mf.getMapAsync(this);
                } else {
                    spinner.setVisibility(View.VISIBLE);
                    showConnectionError();
                    spinner.setVisibility(View.INVISIBLE);
                }

                mapC.refreshMap(); // Refresh

                // Can only be done if Location Permissions are available and Connected to a network
                if (isLocationAvailable && (wifiConnected || mobileConnected)) {
                    Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    // Make sure location is not null before we display it.
                    if (location != null) {
                        mapC.setCurrentLocation(location);
                        mapC.setUpDistanceCounter(distanceText, location);
                    }
                }
                return true;

            case R.id.find_home:
                // Zoom in on where home is.
                if (wifiConnected || mobileConnected) {
                    if (mapC.getHomeLocationLocation() != null) {
                        mapC.setZoomLevel();
                    } else {
                        // Home has not been added to the Map
                        Toast.makeText(this, R.string.home_not_set, Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method specifies what functionality to occur when the Map is layout ready and done. Cannot
     * happen before. The ProgressBar is set to disappear. Only Once the map is loaded should we
     * display the distance.
     *
     */
    @Override
    public void onMapLoaded() {
        ProgressBar spinner = (ProgressBar)findViewById(R.id.map_progressBar);

        TextView distanceText_hint = (TextView)findViewById(R.id.distance_view_text);

        spinner.setVisibility(View.INVISIBLE);

        distanceText.setVisibility(View.VISIBLE);
        distanceText_hint.setVisibility(View.VISIBLE);

    }

    /**
     * This Method represents the map being loaded but not laid out yet.
     * Once the layout is done, {@link #onMapLoaded()} is called to handle loaded functionality.
     *
     * @param map the map instance.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        map.setOnMapLoadedCallback(this);
    }

    /**
     * Method checks the network connection. Sets the wifiConnected and mobile Connected variables
     * accordingly. Used to check if there is an network connection.
     */
    private void updateConnectedFlags() {
        ConnectivityManager cManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = cManager.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            // Not connected to any network.
            wifiConnected = false;
            mobileConnected = false;
        }
    }


    /**
     * Method that indicates to the user that the location they have set is invalid. It asks them to
     * change the location to a valid location. The validity of a location is determined by Googles
     * {@link android.location.Geocoder}. An alert dialog is shown to the user explaining that their
     * location is invalid. The Later button dismisses the dialog. The Ok button sends the user to
     * the settings screen, where they may enter a different home location.
     */
    public void showLocationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We couldn't find where home is. Please enter a valid location for Home.")
                .setTitle("Invalid Location");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User Accepted the dialog => Starts the Settings Activity.
                Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(settingsActivity);
            }
        });
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.create().show();
    }

    /**
     * Method shows the connection error. This means the Device is not connected to either a mobile
     * or Wi-Fi network. The Distance text is hidden. Before the Error is shown, any previous error
     * is then removed. A web view fragment is used to represent the Error information.
     */
    public void showConnectionError() {
        TextView distanceText = (TextView)findViewById(R.id.distance_view);
        TextView distanceText_hint = (TextView)findViewById(R.id.distance_view_text);

        // Remove previous Instances of the Error.
        getFragmentManager().popBackStack();

        // Hide the Distance Text.
        distanceText.setVisibility(View.INVISIBLE);
        distanceText_hint.setVisibility(View.INVISIBLE);

        // Show the Error message.
        WebViewFragment wvf = ConnectionErrorFragment.newInstance("file:///android_asset/Misc/error-connection.html");
        getFragmentManager().beginTransaction().replace(R.id.MainMapID, wvf).addToBackStack("null").commit();

    }

    /**
     * Method handles functionality when a connection to the Google API Client has been Established.
     * Creates a Location Request with the priority of High Accuracy and a refresh rate of 5 seconds
     * This is The same value as the refresh for Google Maps. Therefore, the location should be
     * found every 5 seconds and match the area shown by Google Maps.
     *
     * If there are permissions to use location then location updates are requested. Otherwise,
     * disconnect from the Google API Client to allow for a connection with location permissions.
     *
     * @param bundle This Activity's Bundle.
     */
    @Override
    public void onConnected(Bundle bundle) {

        // Set up the Location Request
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // 5 Seconds

        if (isLocationAvailable) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } else {
            // NEED PERMISSIONS to get location, Therefore must disconnect.
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Connection to API has been Suspended
    }

    /**
     * Method handles functionality when location is obtained. If the Location is null, we shouldn't
     * display the current location Marker not the Distance. Otherwise we display both which is done
     * in the {@link MapController}.
     *
     * @param location, The current location of the device.
     */
    @Override
    public void onLocationChanged(Location location) {
        // Send location.
        if (location != null) {
            mapC.setCurrentLocation(location);
            mapC.setUpDistanceCounter(distanceText, location);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Connection has failed! -> Refresh will try again.
    }
}
