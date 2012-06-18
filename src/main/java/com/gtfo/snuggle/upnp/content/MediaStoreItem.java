package com.gtfo.snuggle.upnp.content;

import android.net.Uri;
import org.seamless.util.MimeType;

public interface MediaStoreItem {

    public long getMediaStoreId();

    public Uri getMediaStoreUri();

    public long getSizeInBytes();

    public MimeType getMimeType();

    public long getDuration();

}