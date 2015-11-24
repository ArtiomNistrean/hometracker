package com.artiomnist.hometracker;

import android.app.FragmentManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created on 24/11/2015.
 * Controller Class for the Map.
 */
public class MapController {

    private GoogleMap map = null;

    public void setUpMapIfNull(FragmentManager fragmentManager) {
        if (map == null) {
            map = ((MapFragment) fragmentManager.findFragmentById(R.id.MainMapID)).getMap();
        }

        if (map != null) {
            setUpMap();
        }
    }

    public void setUpMap() {
        map.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Marker"));
    }

    public GoogleMap getMap() {
        return map;
    }

}
