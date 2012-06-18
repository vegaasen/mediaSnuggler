package com.gtfo.snuggle.upnp.content;

import org.apache.http.protocol.HttpRequestHandler;
import org.fourthline.cling.support.model.DIDLObject;

public interface Content extends HttpRequestHandler {

    DIDLObject findObjectWithId(String id);

}
