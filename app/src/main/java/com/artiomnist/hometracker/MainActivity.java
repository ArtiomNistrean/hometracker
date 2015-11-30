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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.LocationListener;

// TODO REFINE COMMENTS

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

    // Whether there is Location Services available.
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
     * is not connected to any network, and this displays an error page {@see showConnectionError}.
     * However, If there is an internet connection, but the location of home is not valid then an
     * appropriate error message is displayed. {@see showLocationalert}
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
            mapC.setUpMapIfNull(this.getSupportFragmentManager(), this); // Creates the Map + Updates map variable
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        ProgressBar spinner = (ProgressBar)findViewById(R.id.map_progressBar);

        switch (id) {
            case R.id.settings:
                Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(settingsActivity);
                return true;

            case R.id.about:
                Intent i = new Intent(this, AboutActivity.class).putExtra(AboutActivity.EXTRA_FILE,
                        "file:///android_asset/Misc/help.html");
                startActivity(i);
                return true;

            case R.id.refresh:
                updateConnectedFlags();

                if (wifiConnected || mobileConnected) {
                    spinner.setVisibility(View.VISIBLE);
                    getFragmentManager().popBackStack();
                    SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.MainMapID);
                    mf.getMapAsync(this); // calls onMapReady when Loaded
                } else {
                    spinner.setVisibility(View.VISIBLE);
                    showConnectionError();
                    spinner.setVisibility(View.INVISIBLE);
                }
                mapC.refreshMap();
                if (isLocationAvailable && (wifiConnected || mobileConnected)) {
                    Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    if (location != null) {
                        mapC.setCurrentLocation(location);
                        mapC.setUpDistanceCounter(distanceText, location);
                    }
                }

                return true;

            case R.id.find_home:
                if (wifiConnected || mobileConnected) {
                    mapC.setZoomLevel();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method specifies what functionality to occur when the Map is layout ready and done. Cannot
     * happen before. The ProgressBar is set to disappear.
     *
     */
    @Override
    public void onMapLoaded() {
        ProgressBar spinner = (ProgressBar)findViewById(R.id.map_progressBar);

        TextView distanceText_hint = (TextView)findViewById(R.id.distance_view_text);

        spinner.setVisibility(View.INVISIBLE);

        if (isLocationAvailable) {
            //map.setMyLocationEnabled(true);
            //mapC.setCurrentLocation(); // Sets current Location
            //mapC.setUpDistanceCounter(distanceText);
        } else {
            // TOAST TODO
            System.out.println("So this Happened?!");
        }



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

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    private void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }


    // TODO
    public void showLocationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please").setTitle("ERROR TITIES");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // TODO OR SECOND DIALOG?>
                Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(settingsActivity);
            }
        });
        builder.setNegativeButton("FUCK OFF", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.create().show();
    }

    public void showConnectionError() {

        getFragmentManager().popBackStack();
        TextView distanceText = (TextView)findViewById(R.id.distance_view);
        distanceText.setVisibility(View.INVISIBLE);
        TextView distanceText_hint = (TextView)findViewById(R.id.distance_view_text);
        distanceText_hint.setVisibility(View.INVISIBLE);
        WebViewFragment wvf = ConnectionErrorFragment.newInstance("file:///android_asset/Misc/error-connection.html");
        getFragmentManager().beginTransaction().replace(R.id.MainMapID, wvf).addToBackStack("null").commit();

    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("CONNECTED!");
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // 5 Seconds
        if (isLocationAvailable) {
            System.out.println("CONNECTED! AND SETTING");
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            System.out.println("CONNECTED! ADN SET");
        } else {
            // NEED PERMISSIONS.
            System.out.println("FAILED TO CONNECT PERMISSIONS!");
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Connection to API has been Suspended
        System.out.println("FAILED TO CONNECT SUSPEDED!");
    }

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
        // Connection has failed!
        // Toast TODO
        System.out.println("FAILED TO CONNECT");
    }
}
