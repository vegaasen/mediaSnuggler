package com.gtfo.snuggle.upnp.content;

import android.content.Context;
import android.util.Log;
import org.apache.http.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;
import org.teleal.common.util.MimeType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Every content will be handled here. Audio, Video and Photos. (that's the idea at least......)
 *
 * @author vegaasen
 * @since 0.1.a
 */

public class MediaDBContent extends DIDLContent implements Content {
    private static final String TAG = MediaDBContent.class.getName();
    private static final String GET = "GET";
    
    public static final String CREATOR = "System";
    public static final DIDLObject.Class CLASS_CONTAINER = new DIDLObject.Class("object.container");

    private final Context context;
    private final URLBuilder urlBuilder;
    private final MediaStoreObservers observers;

    public MediaDBContent(Context context, URLBuilder urlBuilder) {
        this.context = context;
        this.urlBuilder = urlBuilder;

        RootContainer rootContainer = new RootContainer(this);
        addContainer(rootContainer);

        this.observers = new MediaStoreObservers(context, rootContainer);
    }

    public void registerObservers() {
        observers.register();
    }

    public void unRegisterObservers() {
        observers.unRegister();
    }

    public void updateAll() {
        observers.updateAll();
    }

    public DIDLObject findObjectWithId(String id) {
        for (Container currentContainer : getContainers()) {
            if (currentContainer.getId().equals(id)){
                return currentContainer;
            }
            DIDLObject didlObject = findObjectWithId(id, currentContainer);
            if (didlObject != null){
                return didlObject;
            }
        }
        return null;
    }

    protected DIDLObject findObjectWithId(String id, Container current) {
        for (Container container : current.getContainers()) {
            if (container.getId().equals(id)) return container;
            DIDLObject obj = findObjectWithId(id, container);
            if (obj != null) return obj;
        }
        for (Item item : current.getItems()) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }

    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
           throws HttpException, IOException {

        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);

        if (!method.equals(GET)) {
            throw new MethodNotSupportedException(method + " method not supported");
        }

        String objectId = getUrlBuilder().getObjectId(request.getRequestLine().getUri());
        Log.d(TAG, "GET request for object with identifier: " + objectId);

        DIDLObject obj = findObjectWithId(objectId);
        if (obj == null) {
            Log.d(TAG, "Object not found, returning 404");
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            return;
        }

        InputStream is = openDataInputStream(obj);
        if (is == null) {
            Log.d(TAG, "Data not readable, returning 404");
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            return;
        }

        long sizeInBytes = getSizeInBytes(obj);
        MimeType mimeType = getMimeType(obj);

        InputStreamEntity entity = new InputStreamEntity(is, sizeInBytes);
        entity.setContentType(mimeType.toString());
        response.setEntity(entity);
        response.setStatusCode(HttpStatus.SC_OK);
        Log.d(TAG, "Streaming data bytes: " + sizeInBytes);
    }

    protected InputStream openDataInputStream(DIDLObject didlObject) {
        try {
            if (didlObject instanceof MediaStoreItem) {
                MediaStoreItem item = (MediaStoreItem) didlObject;
                return getContext().getContentResolver().openInputStream(item.getMediaStoreUri());
            }
        } catch (FileNotFoundException ex) {
            Log.d(TAG, "Data not found, can't open input stream: " + didlObject);
        }
        return null;
    }

    protected long getSizeInBytes(DIDLObject obj) {
        if (obj instanceof MediaStoreItem) {
            return ((MediaStoreItem)obj).getSizeInBytes();
        }
        return 0;
    }

    protected MimeType getMimeType(DIDLObject didlObject) {
        if (didlObject instanceof MediaStoreItem) {
            return ((MediaStoreItem)didlObject).getMimeType();
        }
        return null;
    }

    public Context getContext() {
        return context;
    }

    public URLBuilder getUrlBuilder() {
        return urlBuilder;
    }

    @SuppressWarnings("unused")
    public RootContainer getRootContainer() {
        return (RootContainer) getContainers().get(0);
    }

    public static class ID {
        public static final String SEPARATOR = "-";

        private static Random random = new Random(new Date().getTime());

        public static String random() {
            return Long.toString(random.nextInt(99999));
        }

        public static String appendRandom(DIDLObject object) {
            return appendRandom(object.getId());
        }

        public static String appendRandom(String id) {
            return id + SEPARATOR + random();
        }
    }
    
}
