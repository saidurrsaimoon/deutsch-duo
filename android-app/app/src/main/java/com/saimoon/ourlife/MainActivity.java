package com.saimoon.ourlife;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 4102;
    private static final int NOTIFICATION_REQUEST = 7001;
    private static final String PROJECT = "deutsch-duo-8f489";
    private static final String HOME_URL = "https://saidurrsaimoon.github.io/deutsch-duo/";
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private String pendingUid, pendingIdToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(27, 15, 31));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(27, 15, 31));
        initFirebase();
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);
        webView.addJavascriptInterface(new PushBridge(), "OurLifeAndroid");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if ("saidurrsaimoon.github.io".equalsIgnoreCase(host)) return false;
                if ("https".equalsIgnoreCase(uri.getScheme())) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (ActivityNotFoundException ignored) {}
                    return true;
                }
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try { startActivityForResult(params.createIntent(), FILE_CHOOSER_REQUEST); return true; }
                catch (ActivityNotFoundException e) { fileCallback = null; return false; }
            }
        });
        if (savedInstanceState == null) webView.loadUrl(HOME_URL);
        else webView.restoreState(savedInstanceState);
    }

    private void initFirebase() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyBwsCFD4YzcoCD_vbEHm5jfxStzcdamLHU")
                .setApplicationId("1:660528509397:android:03d9d4897ccd5f88818d5c")
                .setProjectId(PROJECT).setGcmSenderId("660528509397").build();
            FirebaseApp.initializeApp(this, options);
        }
    }

    private class PushBridge {
        @JavascriptInterface public void enablePush(String uid, String idToken) {
            pendingUid = uid; pendingIdToken = idToken;
            if (android.os.Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST);
            } else registerNativeToken();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) registerNativeToken();
            else notifyWeb(false, "Phone notification permission is denied.");
        }
    }

    private void registerNativeToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> new Thread(() -> {
            try {
                JSONObject value = new JSONObject().put("stringValue", token);
                JSONObject transform = new JSONObject().put("document", "projects/" + PROJECT + "/databases/(default)/documents/users/" + pendingUid)
                    .put("fieldTransforms", new JSONArray().put(new JSONObject().put("fieldPath", "fcms")
                    .put("appendMissingElements", new JSONObject().put("values", new JSONArray().put(value)))));
                JSONObject body = new JSONObject().put("writes", new JSONArray().put(new JSONObject().put("transform", transform)));
                HttpURLConnection c = (HttpURLConnection)new URL("https://firestore.googleapis.com/v1/projects/" + PROJECT + "/databases/(default)/documents:commit").openConnection();
                c.setRequestMethod("POST"); c.setConnectTimeout(10000); c.setReadTimeout(15000); c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json");
                c.setRequestProperty("Authorization", "Bearer " + pendingIdToken);
                c.getOutputStream().write(body.toString().getBytes("UTF-8"));
                int status = c.getResponseCode(); c.disconnect();
                notifyWeb(status >= 200 && status < 300, status >= 200 && status < 300 ? "Android push notifications are enabled." : "Could not save this phone's notification token (HTTP " + status + ").");
            } catch (Exception e) { notifyWeb(false, "Push setup failed: " + e.getMessage()); }
        }).start()).addOnFailureListener(e -> notifyWeb(false, "Could not get notification token."));
    }

    private void notifyWeb(boolean ok, String message) {
        if (webView != null) runOnUiThread(() -> webView.evaluateJavascript(
            "window.onNativePushReady && window.onNativePushReady(" + ok + "," + JSONObject.quote(message) + ")", null));
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data)); fileCallback = null;
        }
    }
    @Override protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); if (webView != null) webView.saveState(outState); }

    @Override public void onBackPressed() {
        if (webView == null) { super.onBackPressed(); return; }
        webView.evaluateJavascript("(function(){if(document.body&&document.body.classList.contains('chatmode')){var b=document.querySelector('.chead [data-tab=\\\"hub\\\"]');if(b){b.click();return 'chat';}}return 'other';})()", value -> {
            if (!"\"chat\"".equals(value)) {
                if (webView != null && webView.canGoBack()) webView.goBack(); else finish();
            }
        });
    }
    @Override protected void onDestroy() { if (fileCallback != null) { fileCallback.onReceiveValue(null); fileCallback = null; } if (webView != null) webView.destroy(); super.onDestroy(); }
}