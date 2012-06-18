package com.gtfo.snuggle.upnp.content;

import org.fourthline.cling.support.model.DIDLObject;

public interface URLBuilder {

    String getURL(DIDLObject object);

    String getObjectId(String urlPath);
    
}
