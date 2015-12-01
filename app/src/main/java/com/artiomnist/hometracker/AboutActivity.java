package com.artiomnist.hometracker;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

/**
 * Created on 25/11/2015.
 * @author www.artiomnist.com
 *
 * This class extends Activity and handles the {@link AboutContentFragment}. This activity is
 * started in the {@link MainActivity} and shows the AboutContentFragment.
 *
 */
public class AboutActivity extends Activity {

    public static final String EXTRA_FILE = "file:///android_asset/Misc/about.html";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {

            String file = getIntent().getStringExtra(EXTRA_FILE);

            Fragment f = AboutContentFragment.newInstance(file);
            getFragmentManager().beginTransaction().add(android.R.id.content, f).commit();
        }
    }
}

