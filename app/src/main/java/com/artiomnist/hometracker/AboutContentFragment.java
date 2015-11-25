package com.artiomnist.hometracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewFragment;

/**
 * Created on 25/11/2015.
 */
public class AboutContentFragment extends WebViewFragment {
    private static final String ABOUT_FILE = "file:///android_asset/Misc/about.html";

    protected static AboutContentFragment newInstance(String file) {
        AboutContentFragment acf = new AboutContentFragment();

        Bundle args = new Bundle();
        args.putString(ABOUT_FILE, file);
        acf.setArguments(args);

        return(acf);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);


        getWebView().loadUrl(ABOUT_FILE);

        return(result);
    }

}
