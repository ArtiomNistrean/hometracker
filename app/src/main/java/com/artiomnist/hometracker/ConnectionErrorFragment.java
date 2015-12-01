package com.artiomnist.hometracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewFragment;

/**
 * Created on 25/11/2015.
 * @author www.artiomnist.com
 *
 * This class extends a WebViewFragment and is used to display an Connection Error Message in HTML
 * format. This is used in the {@link MainActivity} when the activity doesn't detects any
 * network connection.
 *
 */
public class ConnectionErrorFragment extends WebViewFragment {

    private static final String CONNECTION_ERROR = "file";

    protected static ConnectionErrorFragment newInstance(String file) {
        ConnectionErrorFragment fragment = new ConnectionErrorFragment();

        Bundle args = new Bundle();
        args.putString(CONNECTION_ERROR, file);
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

        getWebView().loadUrl(getErrorFile());

        return(result);
    }

    private String getErrorFile() {
        return(getArguments().getString(CONNECTION_ERROR));
    }

}
