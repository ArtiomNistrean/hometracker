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
 * This class extends a WebViewFragment and is used to display a simple about page in HTML
 * format. This is used in the {@link AboutActivity} which is called when the user selects the
 * 'about' menu option item. The about file is located in the android assets folder and contains
 * information about the application as well as how to use the application. The About file is
 * unlikely to change therefore is set and defined as a ptivate final String.
 *
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
