package com.artiomnist.hometracker;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
        map = mapC.getMapModel().getMap();

        mapC.setUpMapIfNull(this.getSupportFragmentManager()); // Creates the Map + Updates map variable
        map = mapC.getMapModel().getMap();


        SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.MainMapID);
        mf.getMapAsync(this); // calls onMapReady when Loaded
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
            mapC.setUpMapIfNull(this.getSupportFragmentManager()); // Creates the Map + Updates map variable
            map = mapC.getMapModel().getMap();
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
