package com.artiomnist.hometracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewFragment;

/**
 * Created by artiomNistrean on 25/11/2015.
 */
public class ErrorActivity extends WebViewFragment {

    private static final String COCKTAIL="file";

    protected static ErrorActivity newInstance(String file) {
        ErrorActivity ea = new ErrorActivity();

        Bundle args = new Bundle();
        args.putString(COCKTAIL, file);
        ea.setArguments(args);

        return (ea);
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
        getWebView().loadUrl(getCocktail());

        return(result);
    }

    private String getCocktail() {
        return(getArguments().getString(COCKTAIL));
    }

}
