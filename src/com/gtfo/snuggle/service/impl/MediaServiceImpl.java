package com.gtfo.snuggle.service.impl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import com.gtfo.snuggle.activity.MainActivity;
import com.gtfo.snuggle.httpservlet.service.impl.AndroidLocalDeviceInetAddrResolverService;
import com.gtfo.snuggle.httpservlet.servlet.service.impl.HttpServerServiceImpl;
import com.gtfo.snuggle.upnp.MediaServer;
import org.teleal.cling.android.AndroidUpnpServiceImpl;

/**
 * Service for the MediaServer.
 * Creates an service that keeps track of all content and units on the current network.
 *
 * @author vegaasen
 * @since 0.1.a
 */

public class MediaServiceImpl extends Service {
    
    private static final String TAG = MediaServiceImpl.class.getName();
    private static final int NOTIFICATION_ID = 1; //the notification ID for the service

    protected Binder binder = new Binder();
    protected ContentHttpServerConnection contentHttpServerConnection;
    protected UpnpServiceConnection upnpServiceConnection;

    @Override
    public void onCreate() {
        super.onCreate();

        getNotificationManager().notify(NOTIFICATION_ID, createNotification());

        contentHttpServerConnection = new ContentHttpServerConnection(
                this,
                new AndroidLocalDeviceInetAddrResolverService((WifiManager) getSystemService(Context.WIFI_SERVICE))
        );

        //Registering the observers of photos/videos/audio files
        contentHttpServerConnection.getContent().registerObservers();

        //Refreshing all the content within the observers (stupid..?)
        contentHttpServerConnection.getContent().updateAll();

        Log.d(TAG, "Binding to the content HTTP server service");
        getApplicationContext().bindService(
                new Intent(this, HttpServerServiceImpl.class),
                contentHttpServerConnection,
                Context.BIND_AUTO_CREATE
        );

        upnpServiceConnection = new UpnpServiceConnection(new MediaServer(contentHttpServerConnection.getContent()));

        Log.d(TAG, "Binding to the UPnP service");
        getApplicationContext().bindService(
                new Intent(MediaServiceImpl.this, AndroidUpnpServiceImpl.class),
                upnpServiceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (contentHttpServerConnection != null) {

            contentHttpServerConnection.getContent().unRegisterObservers();

            Log.d(TAG, "Unbinding from HTTP server service");
            contentHttpServerConnection.prepareUnbind();
            getApplicationContext().unbindService(contentHttpServerConnection);
        }

        if (upnpServiceConnection != null) {
            Log.d(TAG, "Unbinding from UPnP service");
            upnpServiceConnection.prepareUnbind();
            getApplicationContext().unbindService(upnpServiceConnection);
        }

        getNotificationManager().cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private Notification createNotification() {
        Notification notification = new Notification();

        notification.icon = android.R.drawable.star_on;
        notification.tickerText = "MediaSnuggler is Active.";

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        notification.setLatestEventInfo(
                getApplicationContext(),
                notification.tickerText,
                null,
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));

        return notification;
    }
}
