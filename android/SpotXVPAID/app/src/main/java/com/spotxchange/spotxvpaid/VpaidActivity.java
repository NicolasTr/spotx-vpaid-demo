package com.spotxchange.spotxvpaid;

import com.spotxchange.spotxvpaid.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

public class VpaidActivity extends Activity {


    private WebView _webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpaid);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        _webview = (WebView) findViewById(R.id.webview);
        _webview.getSettings().setJavaScriptEnabled(true);
        _webview.getSettings().setMediaPlaybackRequiresUserGesture(false);

        _webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                final Uri uri = Uri.parse(url);
                if (uri.getScheme().equals("vpaid")) {
                    final String event = uri.getHost();
                    onVpaidEvent(event);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        _webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = String.format(Locale.US, "%d: %s", consoleMessage.lineNumber(), consoleMessage.message());
                Log.i(VpaidActivity.class.getSimpleName(), message);
                return true;
            }
        });

        _webview.loadUrl("http://nameless-tundra-9674.herokuapp.com/vpaid/85394?app.domain=com.spotxchange.vpaid&autoplay=1");
    }

    private void onVpaidEvent(String event) {
        Log.i(VpaidActivity.class.getSimpleName(), "VPAID Event: " + event);
        if (event.equals("AdStopped")) {
            finish();
        }
        else if (event.equals("AdError")) {
            finish();
        }
        else if (event.equals("AdClosed")) {
            finish();
        }

    }

}
