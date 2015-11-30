package com.artiomnist.hometracker;

import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by artiomNistrean on 26/11/2015.
 *
 * This NetTask class uses an AsyncTask to find an IP address. Running
 * network IO on main thread is not a best practice Therefore this avoids using network
 * operations on the main thread. As a consequence, this implementation is more efficient than
 * using a {@link android.os.StrictMode.ThreadPolicy.Builder().permitAll()} solution, which can take
 * a much heavier toll on the main thread and is only better in very rare circumstances.
 */
public class NetTask extends AsyncTask<String, Integer, InetAddress> {
    @Override
    protected InetAddress doInBackground(String... params) {

        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(params[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return addr;

    }
}
