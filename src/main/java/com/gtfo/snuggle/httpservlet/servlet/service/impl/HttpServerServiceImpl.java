package com.gtfo.snuggle.httpservlet.servlet.service.impl;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import com.gtfo.snuggle.httpservlet.service.impl.AndroidLocalDeviceInetAddrResolverService;
import com.gtfo.snuggle.httpservlet.servlet.HttpServerContainer;
import com.gtfo.snuggle.httpservlet.servlet.service.HttpServerService;
import com.gtfo.snuggle.httpservlet.utils.IPAddressUtil;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * Service that is used for the implementation of the actual HTTPServer
 *
 * @since v.0.1
 */

public class HttpServerServiceImpl extends Service {

    private static final String ANDROID_NET_CONN_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    
    protected HttpServerContainer httpServerContainer;
    private Binder binder = new Binder();

    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        synchronized public void onReceive(Context c, Intent intent) {
            if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) return;
            if (httpServerContainer == null) return;
            final ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!wifiInfo.isConnected()) {
                httpServerContainer.stopServer();
            } else {
                httpServerContainer.startServer();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        httpServerContainer = new HttpServerContainer(new AndroidLocalDeviceInetAddrResolverService(wifiManager));

        if (!IPAddressUtil.ANDROID_EMULATOR) {
            registerReceiver(connectivityReceiver, new IntentFilter(ANDROID_NET_CONN_CONNECTIVITY_CHANGE));
        }
    }

    @Override
    public void onDestroy() {
        if (!IPAddressUtil.ANDROID_EMULATOR) {
            unregisterReceiver(connectivityReceiver);
        }
        httpServerContainer.stopServer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected class Binder extends android.os.Binder implements HttpServerService {
        public int getLocalPort() {
            return httpServerContainer.getLocalPort();
        }

        public void addHandler(String pattern, HttpRequestHandler handler) {
            httpServerContainer.getHandlerRegistry().register(pattern, handler);
        }

        public void removeHandler(String pattern) {
            httpServerContainer.getHandlerRegistry().unregister(pattern);
        }

    }

}
