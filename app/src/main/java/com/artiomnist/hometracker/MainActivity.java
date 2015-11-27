package com.artiomnist.hometracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebViewFragment;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

// TODO REFINE COMMENTS

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private MapController mapC;
    private GoogleMap map;

    // Whether there is a Wi-Fi connection.
    public static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    public static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = false;

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter =  new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);

        updateConnectedFlags();


        mapC = new MapController(this);
        map = mapC.getMap();

        mapC.setUpMapIfNull(this.getSupportFragmentManager(), this); // Creates the Map + Updates map variable
        mapC.getMap().setMyLocationEnabled(true);

        SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.MainMapID);
        mf.getMapAsync(this); // calls onMapReady when Loaded
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        System.out.println("REQUEST CODE " + requestCode);
        System.out.println("permissions " + permissions[0]);
        System.out.println("grantResults " + grantResults[0]);
        System.out.println("pkgmger " + PackageManager.PERMISSION_GRANTED);
        if (requestCode == MapController.MY_LOCATION_PERMISSION_REQUEST) {
            if (permissions.length == 1 &&
                    permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //
                System.out.println("Set Location True ACTIVITY");
            } else {
                mapC.getMap().setMyLocationEnabled(false);
                System.out.println("Set Location FALSE ACTIVITY");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateConnectedFlags();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateConnectedFlags();
        if (refreshDisplay) {
            mapC.refreshMap();
            refreshDisplay = false;
        } else {
            mapC.setUpMapIfNull(this.getSupportFragmentManager(), this); // Creates the Map + Updates map variable
            map = mapC.getMap();
        }
        if (!(wifiConnected || mobileConnected)) {
            showConnectionError();
        } else if (!mapC.getLocationError()) {
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
        spinner.setVisibility(View.INVISIBLE);

        // TODO
        LatLng me = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
        Marker meMarker = map.addMarker(new MarkerOptions().position(me).title("me"));
        meMarker.setDraggable(true);
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                System.out.println("LAT " + marker.getPosition().latitude);
                System.out.println("LONG " + marker.getPosition().longitude);
            }
        });

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
        builder.setMessage("ERROR MESAGE").setTitle("ERROR TITIES");
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
        WebViewFragment wvf = ConnectionErrorFragment.newInstance("file:///android_asset/Misc/error-connection.html");
        getFragmentManager().beginTransaction().replace(R.id.MainMapID, wvf).addToBackStack("null").commit();

    }
}
