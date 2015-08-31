package com.spotxchange.spotxvpaid;

import com.spotxchange.spotxvpaid.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

public class VpaidActivity extends Activity implements Handler.Callback {
    private static final String TAG = VpaidActivity.class.getSimpleName();

    public static final String EXTRA_CHANNEL_ID = "channel_id";
    public static final String EXTRA_APP_DOMAIN = "app_domain";
    public static final String EXTRA_SECURE = "secure";

    private Handler _handler;
    private WebView _webview;
    private String _channelId;
    private String _domain;
    private boolean _secure;
    private boolean _loaded;
    private boolean _started;
    private boolean _visible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpaid);
        _handler = new Handler(this);
        _loaded = false;
        _started = false;
        _visible = false;

        Intent intent = getIntent();
        if (intent != null) {
            _channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
            _domain = intent.getStringExtra(EXTRA_APP_DOMAIN);
            _secure = intent.getBooleanExtra(EXTRA_SECURE, false);
        }

        if (TextUtils.isEmpty(_channelId) || TextUtils.isEmpty(_domain)) {
            finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        _webview = (WebView) findViewById(R.id.webview);
        _webview.getSettings().setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            _webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        _webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                final Uri uri = Uri.parse(url);
                if (uri.getScheme().equals("vpaid")) {
                    final String event = uri.getHost();
                    final Message msg = _handler.obtainMessage(0, event);
                    _handler.sendMessageDelayed(msg, 100);
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

        startLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _visible = true;
        startAd();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _visible = false;
        stopAd();
    }

    private void startLoading() {
        new LoadAdTask().execute();
    }

    private void startAd() {
        if (_loaded && _visible) {
            if (_started) {
                _webview.loadUrl("javascript:vpaid.resumeAd();");
            }
            else {
                _webview.loadUrl("javascript:vpaid.startAd();");
                _started = true;
            }
        }
    }

    private void stopAd() {
        if (_loaded && _started) {
            _webview.loadUrl("javascript:vpaid.pauseAd();");
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        String event = (String) msg.obj;
        this.onVpaidEvent(event);
        return true;
    }

    private void onVpaidEvent(String event) {
        Log.i(VpaidActivity.class.getSimpleName(), "VPAID Event: " + event);
        if (event.equals("AdLoaded")) {
            _loaded = true;
            startAd();
        }
        else if (event.equals("AdStopped")) {
            finish();
        }
        else if (event.equals("AdError")) {
            finish();
        }
        else if (event.equals("AdClosed")) {
            finish();
        }
    }

    private class LoadAdTask extends AsyncTask<Void, Void, Uri> {

        @Override
        protected Uri doInBackground(Void... params) {
            final String bundleId = getApplicationContext().getPackageName();
            final String advertiserId = getAdvertisingId();

            Uri.Builder url = Uri.parse("http://nameless-tundra-9674.herokuapp.com/vpaid/").buildUpon()
                    .scheme(_secure ? "https" : "http")
                    .appendPath(_channelId)
                    .appendQueryParameter("app.bundle", bundleId)
                    .appendQueryParameter("app.domain", _domain)
                    .appendQueryParameter("autoplay", "0")
                    .appendQueryParameter("events", "1");

            if (advertiserId != null) {
                url.appendQueryParameter("device.idfa", advertiserId);
            }

            return url.build();
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            _webview.loadUrl(uri.toString());
        }

        private String getAdvertisingId() {
            String advertiserId = null;
            try {
                Class clazz = Class.forName("com.google.android.gma.ads.identifier.AdvertisingIdClient");
                Object advertisingIdClientInfo = clazz.getMethod("getAdvertisingIdInfo").invoke(clazz);

                clazz = Class.forName("com.google.android.gma.ads.identifier.AdvertisingIdClient.Info");
                advertiserId = (String) clazz.getMethod("getId").invoke(advertisingIdClientInfo);
            }
            catch (Throwable t) {
                Log.i(TAG, "Unable to obtain advertising id", t);
            }
            return advertiserId;
        }
    }
}
