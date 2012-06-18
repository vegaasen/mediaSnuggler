package com.gtfo.snuggle.service.impl;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.gtfo.snuggle.upnp.MediaServer;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.LocalDevice;

/**
 * The one and only uPnP Service-instance..
 * This keeps (at all times), and overview of all discovered units on the network
 *
 * Remember to shut down the service when unplugging app!
 *
 * @author vegaasen
 * @since 0.1b
 */

public class UpnpServiceConnection implements ServiceConnection {

    private static final String TAG = UpnpServiceConnection.class.getName();

    protected final MediaServer mediaServer;
    protected AndroidUpnpService upnpService;

    public UpnpServiceConnection(MediaServer mediaServer) {
        this.mediaServer = mediaServer;
    }

    public void onServiceConnected(ComponentName className, IBinder service) {
        upnpService = (AndroidUpnpService) service;

        LocalDevice mediaServerDevice =
                upnpService.getRegistry().getLocalDevice(mediaServer.getUniqueDeviceName(), true);
        if (mediaServerDevice == null) {
            try {
                Log.d(TAG, "Creating MediaServer device and registering with UPnP service");
                mediaServerDevice = mediaServer.createDevice();
                upnpService.getRegistry().addDevice(mediaServerDevice);
            } catch (Exception e) {
                Log.d(TAG, "" + e);
            }
        }
    }

    public void onServiceDisconnected(ComponentName className) {
        upnpService = null;
    }

    public void prepareUnbind() {
        if (upnpService != null) {
            upnpService.getRegistry().removeDevice(mediaServer.getUniqueDeviceName());
        }
    }
}