package com.gtfo.snuggle.upnp.content.photos;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.gtfo.snuggle.upnp.common.CommonPartsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import com.gtfo.snuggle.upnp.content.MediaStoreItem;
import com.gtfo.snuggle.upnp.content.URLBuilder;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.Photo;
import org.seamless.util.MimeType;

import java.util.Date;

/**
 * ################EXAMPLE DATA FROM MediaStore - "ImageColumns"####################
 * 12-03 07:12:53.152: INFO/System.out(4444): ### '_id' => 79
 * 12-03 07:12:53.152: INFO/System.out(4444): ### '_data' => /mnt/sdcard/DCIM/Camera/IMG_20101203_071252.jpg
 * 12-03 07:12:53.152: INFO/System.out(4444): ### '_size' => 737666
 * 12-03 07:12:53.152: INFO/System.out(4444): ### '_display_name' => IMG_20101203_071252.jpg
 * 12-03 07:12:53.152: INFO/System.out(4444): ### 'mime_type' => image/jpeg
 * 12-03 07:12:53.152: INFO/System.out(4444): ### 'title' => IMG_20101203_071252
 * 12-03 07:12:53.152: INFO/System.out(4444): ### 'date_added' => 1291356772
 * 12-03 07:12:53.152: INFO/System.out(4444): ### 'date_modified' => null
 * 12-03 07:12:53.152: INFO/System.out(4444): ### 'description' => null
 * 12-03 07:12:53.152: INFO/System.out(4444): ### 'picasa_id' => null
 * 12-03 07:12:53.152: INFO/System.out(4444): ### 'isprivate' => null
 * 12-03 07:12:53.162: INFO/System.out(4444): ### 'latitude' => null
 * 12-03 07:12:53.162: INFO/System.out(4444): ### 'longitude' => null
 * 12-03 07:12:53.162: INFO/System.out(4444): ### 'datetaken' => 1291356772964
 * 12-03 07:12:53.162: INFO/System.out(4444): ### 'orientation' => 0
 * 12-03 07:12:53.162: INFO/System.out(4444): ### 'mini_thumb_magic' => null
 * 12-03 07:12:53.162: INFO/System.out(4444): ### 'bucket_id' => 1506676782
 * 12-03 07:12:53.162: INFO/System.out(4444): ### 'bucket_display_name' => Camera
 *
 * @author vegaasen
 * @since 0.1.a
 * @see android.provider.MediaStore
 */


public class MediaStorePhoto extends Photo implements MediaStoreItem {

    private static final int
                            ID = 0, DATE_ADDED = 1, BUCKET_DISP_NAME = 2, TITLE = 3,
                            DISPLAY_NAME = 4, SIZE = 5, MIME_TYPE = 6;
    
    private long mediaStoreId;
    private Uri mediaStoreUri;
    private long sizeInBytes;
    private MimeType mimeType;
    @SuppressWarnings("unused")
    private long duration;

    public static final String[] PROJECTION_QUERY = {
            MediaStore.Images.Media._ID, // 0
            MediaStore.Images.Media.DATE_ADDED, // 1
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 2
            MediaStore.Images.Media.TITLE, // 3
            MediaStore.Images.Media.DISPLAY_NAME, // 4
            MediaStore.Images.Media.SIZE, // 5
            MediaStore.Images.Media.MIME_TYPE // 6
    };

    public MediaStorePhoto(Cursor cursor, Uri mediaStoreUri, String parentId, String transientId, final URLBuilder urlBuilder) {
        this.mediaStoreId = cursor.getLong(ID); // Used as an persistent identifier
        this.mediaStoreUri = mediaStoreUri;
        
        setId(transientId);
        setParentID(parentId);
        setCreator(MediaDBContent.CREATOR);

        if (!cursor.isNull(DATE_ADDED)) {
            setDate(
                    CommonPartsOfMediaSnuggler.DEFAULT_MEDIA_DATE_FORMAT.format(
                    new Date(cursor.getLong(DATE_ADDED) * 1000))
            );
        }
        if (!cursor.isNull(BUCKET_DISP_NAME)) {
            setAlbum(cursor.getString(BUCKET_DISP_NAME));
        }
        if (!cursor.isNull(TITLE)) {
            setTitle(cursor.getString(TITLE));
        } else {
            setTitle(cursor.getString(DISPLAY_NAME));
        }
        if(!cursor.isNull(SIZE)) {
            this.sizeInBytes = cursor.getLong(SIZE);
        }
        if(!cursor.isNull(MIME_TYPE)) {
            this.mimeType = MimeType.valueOf(cursor.getString(MIME_TYPE));
        }
        Res resource = new Res() {
            @Override
            public String getValue() {
                return urlBuilder.getURL(MediaStorePhoto.this);
            }
        };
        resource.setProtocolInfo(new ProtocolInfo(mimeType));
        resource.setSize(sizeInBytes);
        addResource(resource);
    }


    public long getMediaStoreId() {
        return mediaStoreId;
    }

    public Uri getMediaStoreUri() {
        return mediaStoreUri;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ") " + getMediaStoreUri();
    }
}