package com.artiomnist.hometracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created on 25/11/2015.
 * @author www.artiomnist.com
 *
 * This Class extends the BroadcastReceiver. The class checks if there is a connected Wi-Fi or
 * Mobile connection and sets the respective flags accordingly. The class achieves this by
 * intercepting the {@link android.net.ConnectivityManager#CONNECTIVITY_ACTION}. This indicates
 * the connection change. Checking the TYPE_WIFI indicaes if it is a Wi-Fi connection or not.
 *
 * This class was inspired by the workshop on 'NetworkUsage' in ECM3424 Mobile Computing Mobile and
 * Ubiquitous Computing at the University of Exeter.
 *
 */
public class NetworkReceiver extends BroadcastReceiver {

    /**
     * Method handles functionality when a network connection is received. The method initially
     * sets up the Connectivity Manager. Then by checking the Active network information we can
     * determine if we need to refresh the display or not.
     *
     * @param context - from the Main activity
     * @param intent - from the Main activity
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null) {
            // The Application is connected to a network.
            Toast.makeText(context, R.string.reconnected, Toast.LENGTH_SHORT).show();
            MainActivity.refreshDisplay = true;
        } else {
            // The Application is not connected to a network.
            MainActivity.refreshDisplay = false;
            Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_SHORT).show();
        }
    }
}
