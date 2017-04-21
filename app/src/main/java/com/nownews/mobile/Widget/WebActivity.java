package com.nownews.mobile.Widget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nownews.R;
import com.nownews.mobile.Common.UserDataInfo;
import com.nownews.mobile.Common.Utility;
import com.nownews.mobile.NewHome;

public class WebActivity extends Activity {

    public final static String KEY_URL = "url";
    private final String TAG = getClass().getSimpleName();
    private String mUrl;
    private WebView vWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        startActivity();

    }

    private void startActivity() {

        processBundle();
        processView();
        setWeb();

    }

    private void processBundle() {
        mUrl = getIntent().getStringExtra(KEY_URL);
    }

    private void processView() {
        vWebView = (WebView) findViewById(R.id.webview);
    }

    private CustomTabsClient mCustomTabsClient;
    private CustomTabsIntent mCustomTabsIntent;
    private void setWeb() {

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        String packageName = "com.android.chrome";
        if(currentApiVersion >= Build.VERSION_CODES.HONEYCOMB_MR1
                && Utility.isPackageExisted(this, packageName)){

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.enableUrlBarHiding();
            builder.setShowTitle(true);
            builder.addDefaultShareMenuItem();
            builder.setToolbarColor(getResources().getColor(R.color.toolbar_color));
            builder.setStartAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            mCustomTabsIntent = builder.build();
            mCustomTabsIntent.intent.setPackage(packageName);
            mCustomTabsIntent.launchUrl(WebActivity.this, Uri.parse(mUrl));

            finish();
        }else{
            final WebSettings webSettings = vWebView.getSettings();
            webSettings.setSupportZoom(true);
            webSettings.setBuiltInZoomControls(false);
            webSettings.setJavaScriptEnabled(true);
            vWebView.setWebViewClient(new WebViewClient());
            vWebView.loadUrl(mUrl);
        }

    }

    @Override
    public void onBackPressed() {
        if (vWebView.canGoBack()) {
            vWebView.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        UserDataInfo.activityResumed(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        UserDataInfo.activityPaused();
        super.onPause();
    }

    public void reload() {
        startActivity();
    }

    @Override
    protected void onDestroy() {
        UserDataInfo.activityDestroy(this);
        super.onDestroy();
    }

}
