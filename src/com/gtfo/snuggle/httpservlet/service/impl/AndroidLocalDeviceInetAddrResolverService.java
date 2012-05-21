package com.gtfo.snuggle.httpservlet.service.impl;

import android.net.wifi.WifiManager;
import com.gtfo.snuggle.httpservlet.service.LocalDeviceInetAddrResolverService;
import com.gtfo.snuggle.httpservlet.utils.IPAddressUtil;
import java.net.InetAddress;

public class AndroidLocalDeviceInetAddrResolverService implements LocalDeviceInetAddrResolverService {

    final protected WifiManager wifiManager;

    public AndroidLocalDeviceInetAddrResolverService(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }

    public InetAddress getLocalInetAddress() {
        return IPAddressUtil.getWifiIPAddress(getWifiManager());

    }
}
