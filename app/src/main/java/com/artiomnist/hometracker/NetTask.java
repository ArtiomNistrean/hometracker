package com.artiomnist.hometracker;

import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by artiomNistrean on 26/11/2015.
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
