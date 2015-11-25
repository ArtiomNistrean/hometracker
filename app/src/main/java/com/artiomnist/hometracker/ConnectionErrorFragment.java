package com.artiomnist.hometracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewFragment;

/**
 * Created by artiomNistrean on 25/11/2015.
 */
public class ConnectionErrorFragment extends WebViewFragment {

    private static final String CONNTECTION_ERROR = "file";

    protected static ConnectionErrorFragment newInstance(String file) {
        ConnectionErrorFragment fragment = new ConnectionErrorFragment();

        Bundle args = new Bundle();
        args.putString(CONNTECTION_ERROR, file);
        fragment.setArguments(args);

        return (fragment);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);

        getWebView().getSettings().setJavaScriptEnabled(true);
        getWebView().getSettings().setSupportZoom(true);
        getWebView().getSettings().setBuiltInZoomControls(false);
        getWebView().loadUrl(getErrorFile());

        return(result);
    }

    private String getErrorFile() {
        return(getArguments().getString(CONNTECTION_ERROR));
    }

}
