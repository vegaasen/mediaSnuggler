package com.gtfo.snuggle.model;

import java.util.Map;

/**
 * Just a simple POJO that contains info about the server, and also about the user's phone.
 *
 * @author vegaasen
 * @since 0.1.d
 */
public class ServerInformation {

    public String currentIpActive;
    public String currentPortActive;
    public Map<String, Integer> containerAndElementsShared;

    public void updatePort() {

    }

    public void updateIp() {

    }

    public void updateElementsShared() {
        
    }

    public String getCurrentIpActive() {
        return currentIpActive;
    }

    public String getCurrentPortActive() {
        return currentPortActive;
    }

    public Map<String, Integer> getContainerAndElementsShared() {
        return containerAndElementsShared;
    }
}
