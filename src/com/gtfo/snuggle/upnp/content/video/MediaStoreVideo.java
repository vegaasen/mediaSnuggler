package com.gtfo.snuggle.upnp.content.video;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.gtfo.snuggle.upnp.common.CommonPartsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import com.gtfo.snuggle.upnp.content.MediaStoreItem;
import com.gtfo.snuggle.upnp.content.URLBuilder;
import org.teleal.cling.support.model.PersonWithRole;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.item.VideoItem;
import org.teleal.common.util.MimeType;

import java.util.logging.Logger;

/**
 * ########EXAMPLE DATA FROM MediaStore.Video#########
 *
 * The mediastore-element for videos
 *
 * @author vegaasen
 * @since 0.1.c
 */

public class MediaStoreVideo extends VideoItem implements MediaStoreItem {

    private static final Logger LOGGER = Logger.getLogger(MediaStoreVideo.class.getName());
    private static final int
                        ID = 0, ALBUM = 1, ARTIST = 2, DURATION = 3, CATEGORY = 4, BUCKET_DISPLAY_NAME = 5,
                        MINI_THUM_MAGIC = 6, LANGUAGE = 7, DESCRIPTION = 8, LATITUDE = 9, LONGITUDE = 10,
                        TITLE = 11, MIME_TYPE = 12, SIZE = 13;

    private long mediaStoreId;
    private Uri mediaStoreUri;
    private long sizeInBytes;
    private MimeType mimeType;
    private long duration;

    public static final String[] VIDEO_PROJECTION_QUERY = {
            //video info
            MediaStore.Video.Media._ID, // 0
            MediaStore.Video.Media.ALBUM, // 1
            MediaStore.Video.Media.ARTIST, // 2
            MediaStore.Video.Media.DURATION, // 3
            MediaStore.Video.Media.CATEGORY, // 4
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME, // 5
            MediaStore.Video.Media.MINI_THUMB_MAGIC, // 6
            MediaStore.Video.Media.LANGUAGE, // 7
            MediaStore.Video.Media.DESCRIPTION, // 8

            //video file info
            MediaStore.Video.Media.LATITUDE, // 9
            MediaStore.Video.Media.LONGITUDE, // 10
            MediaStore.Video.Media.TITLE, // 11
            MediaStore.Video.Media.MIME_TYPE, // 12
            MediaStore.Video.Media.SIZE // 13
    };

    public static final String[] SIMPLE_VIDEO_PROJECTION_QUERY = {
            //video info
            MediaStore.Video.Media._ID, // 0
            MediaStore.Video.Media.ALBUM, // 1
            MediaStore.Video.Media.DURATION, // 3
            MediaStore.Video.Media.TITLE, // 11
            MediaStore.Video.Media.MIME_TYPE, // 12
            MediaStore.Video.Media.SIZE // 13
    };

    public MediaStoreVideo(Cursor cursor, Uri mediaStoreUri, String parentId, String transientId, final URLBuilder urlBuilder) {
        PersonWithRole artist;
        PersonWithRole[] artists;
        this.mediaStoreId = cursor.getLong(ID); // Used as an persistent identifier
        this.mediaStoreUri = mediaStoreUri;
        setId(transientId);
        setParentID(parentId);
        setCreator(MediaDBContent.CREATOR);

        if(!cursor.isNull(ALBUM)) {
            //unused
        }
        if(!cursor.isNull(ARTIST)) {
            artist = new PersonWithRole(
                    cursor.getString(ARTIST),
                    CommonPartsOfMediaSnuggler.DEFAULT_ARTIST_ROLE_IF_UNKNOWN
            );
            artists = new PersonWithRole[1];
            artists[0] = artist;
            setActors(artists);
        }else{
            artist = new PersonWithRole(
                    CommonPartsOfMediaSnuggler.DEFAULT_ARTIST_NAME_IF_UNKNOWN,
                    CommonPartsOfMediaSnuggler.DEFAULT_ARTIST_ROLE_IF_UNKNOWN
            );
            artists = new PersonWithRole[1];
            artists[0] = artist;
            setActors(artists);
        }
        if(!cursor.isNull(LANGUAGE)) {
            setLanguage(cursor.getString(LANGUAGE));
        }
        if(!cursor.isNull(DESCRIPTION)) {
            setDescription(cursor.getString(DESCRIPTION));
        }
        if(!cursor.isNull(TITLE)) {
            setTitle(cursor.getString(TITLE));
        }
        if(!cursor.isNull(CATEGORY)) {
            String[] categories = new String[1];
            categories[0] = cursor.getString(CATEGORY);
            setGenres(categories);
        }
        if(!cursor.isNull(DURATION)) {
            this.duration = cursor.getLong(DURATION);
        }
        if(!cursor.isNull(MIME_TYPE)) {
            this.mimeType = MimeType.valueOf(cursor.getString(MIME_TYPE));
        } else {
            this.mimeType = MimeType.valueOf(CommonPartsOfMediaSnuggler.MEDIA_VIDEO_DEFAULT_MIME_TYPE_IF_NOT_FOUND);
        }
        if(!cursor.isNull(SIZE)){
            this.sizeInBytes = cursor.getInt(SIZE);
        }

        Res resource = new Res() {
            @Override
            public String getValue() {
                return urlBuilder.getURL(MediaStoreVideo.this);
            }
        };
        resource.setProtocolInfo(new ProtocolInfo(mimeType));
        resource.setDuration(Long.toString(duration));
        resource.setSize(sizeInBytes);
        addResource(resource);

        LOGGER.info("Added resource.");
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
        return "(" + getClass().getSimpleName() + ")" + getMediaStoreUri();
    }
}
