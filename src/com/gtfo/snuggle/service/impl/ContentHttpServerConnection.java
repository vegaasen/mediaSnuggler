package com.gtfo.snuggle.service.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.gtfo.snuggle.httpservlet.service.LocalDeviceInetAddrResolverService;
import com.gtfo.snuggle.httpservlet.servlet.service.HttpServerService;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import com.gtfo.snuggle.upnp.content.URLBuilder;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.common.util.URIUtil;

import java.net.URI;

/**
 * Should keep track of all content on the server.. _should_
 *
 * @since 0.1b
 * @author vegaasen
 */

public class ContentHttpServerConnection implements ServiceConnection {

    private static final String TAG = ContentHttpServerConnection.class.getName();

    private final MediaDBContent content;
    private HttpServerService httpServerService;

    public ContentHttpServerConnection(Context context, final LocalDeviceInetAddrResolverService localDeviceResolverService) {

        Log.d(TAG, "Creating MediaDBContent [MediaStore]");
        content = new MediaDBContent(
                context,
                new URLBuilder() {
                    public String getURL(DIDLObject object) {
                        return URIUtil.createAbsoluteURL(
                                localDeviceResolverService.getLocalInetAddress(),
                                getHttpServerService().getLocalPort(),
                                URI.create("/" + object.getId())
                        ).toString();
                    }

                    public String getObjectId(String urlPath) {
                        return urlPath.substring(1);
                    }
                }
        );
    }

    public MediaDBContent getContent() {
        return content;
    }

    public HttpServerService getHttpServerService() {
        return httpServerService;
    }

    public void onServiceConnected(ComponentName componentName, IBinder service) {
        httpServerService = (HttpServerService) service;
        httpServerService.addHandler("*", content);
    }

    public void onServiceDisconnected(ComponentName componentName) {
        httpServerService = null;
    }

    public void prepareUnbind() {
        if (httpServerService != null) {
            httpServerService.removeHandler("*");
        }
    }
}
