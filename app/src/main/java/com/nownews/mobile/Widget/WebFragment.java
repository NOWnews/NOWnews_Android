package com.nownews.mobile.Widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.nownews.R;

/**
 * Created by cindy on 2016/10/13.
 */

public class WebFragment extends Fragment {

    public final static String KEY_URL = "url";
    private final String TAG = getClass().getSimpleName();
    private String mUrl;
    private WebView vWebView;
    private RelativeLayout vLoadingLayout;

    public WebFragment() {
        // Do nothing...
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_web, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        processArgument();
        processView();
        setWeb();

    }

    private void processArgument() {
        mUrl = getArguments().getString(KEY_URL);
    }

    private void processView() {

        View view = getView();

        vWebView = (WebView) view.findViewById(R.id.webview);
        vLoadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);
    }

    private void setWeb() {
        final WebSettings webSettings = vWebView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        vWebView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                vLoadingLayout.setVisibility(View.GONE);
                vWebView.setVisibility(View.VISIBLE);

            }
        });
        vWebView.loadUrl(mUrl);
    }

}
