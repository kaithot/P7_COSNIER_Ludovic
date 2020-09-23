package com.ludovic.go4lunch;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

class WebViewRestaurant extends AppCompatActivity {

    private WebView mWebView;
    public static final String WEB = "restaurant_web";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        mWebView = findViewById(R.id.webview);
        String mURL = getIntent().getStringExtra(WEB);

        mWebView.setWebViewClient(new WebViewClient());

        mWebView.loadUrl(mURL);
    }
}
