package com.gtfo.snuggle.httpservlet.utils;

import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPAddressUtil {
    
    public static final boolean ANDROID_EMULATOR;
    
    static {
        boolean foundEmulator = false;
        try {
            Class androidBuild = Thread.currentThread().getContextClassLoader().loadClass("android.os.Build");
            String product = (String)androidBuild.getField("PRODUCT").get(null);
            if ("google_sdk".equals(product) || ("sdk".equals(product)))
                foundEmulator = true;
        } catch (Exception ex) {
            //empty
        }
        ANDROID_EMULATOR = foundEmulator;
    }

    public static InetAddress getWifiIPAddress(WifiManager wifiManager) {

        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }

        int wifiIP = wifiManager.getConnectionInfo().getIpAddress();
        int reverseWifiIP = Integer.reverseBytes(wifiIP);

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> availableLocalAddresses = networkInterface.getInetAddresses();
            while (availableLocalAddresses.hasMoreElements()) {
                InetAddress inetAddress = availableLocalAddresses.nextElement();
                int currentAddressByteArray = byteArrayToInt(inetAddress.getAddress(), 0);
                if ((currentAddressByteArray == wifiIP) || (currentAddressByteArray == reverseWifiIP)) {
                    return inetAddress;
                }
            }
        }
        return null;
    }

    public static int byteArrayToInt(byte[] arr, int offset) {
        if (arr == null || arr.length - offset < 4)
            return -1;

        int r0 = (arr[offset] & 0xFF) << 24;
        int r1 = (arr[offset + 1] & 0xFF) << 16;
        int r2 = (arr[offset + 2] & 0xFF) << 8;
        int r3 = arr[offset + 3] & 0xFF;
        return r0 + r1 + r2 + r3;
    }

}
