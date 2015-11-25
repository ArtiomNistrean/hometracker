package com.artiomnist.hometracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by artiomNistrean on 25/11/2015.
 */
/**
 *
 * This BroadcastReceiver intercepts the android.net.ConnectivityManager.CONNECTIVITY_ACTION,
 * which indicates a connection change. It checks whether the type is TYPE_WIFI.
 * If it is, it checks whether Wi-Fi is connected and sets the wifiConnected flag in the
 * main activity accordingly.
 *
 */
public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Checks the user prefs and the network connection. Based on the result, decides
        // whether
        // to refresh the display or keep the current display.
        // If the setting is ANY network and there is a network connection
        // (which by process of elimination would be mobile), sets refreshDisplay to true.
        if (networkInfo != null) {
            Toast.makeText(context, R.string.reconnected, Toast.LENGTH_SHORT).show();
            // Otherwise, the app can't download content--either because there is no network
            // connection (mobile or Wi-Fi), or because the pref setting is WIFI, and there
            MainActivity.refreshDisplay = true;


        } else {
            MainActivity.refreshDisplay = false;
            Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_SHORT).show();
        }
    }
}
