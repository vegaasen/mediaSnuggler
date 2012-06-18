package com.gtfo.snuggle.upnp.content.music;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.gtfo.snuggle.upnp.common.CommonPartsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import com.gtfo.snuggle.upnp.content.MediaStoreItem;
import com.gtfo.snuggle.upnp.content.URLBuilder;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

import java.util.Date;
import java.util.logging.Logger;

/**
 * ########EXAMPLE DATA FROM MediaStore.Audio#########
 * 
 *
 * @author vegaasen
 * @see android.provider.MediaStore
 * @see android.provider.MediaStore.Audio.Albums
 * @since 0.1.b
 */

public class MediaStoreAudio extends MusicTrack implements MediaStoreItem {

    private static final Logger LOGGER = Logger.getLogger(MediaStoreAudio.class.getName());
    private static final int
                        ID = 0, ALBUM = 1, ARTIST = 2, TRACK = 3, YEAR = 4, DATE_ADDED = 5,
                        DURATION = 6, TITLE_KEY = 7, IS_ALARM = 8, IS_MUSIC = 9, IS_PODCAST = 10,
                        IS_NOTIFICATION = 11, IS_RINGTONE = 12, TITLE = 13, SIZE = 14, MIME_TYPE = 15;

    private long mediaStoreId;
    private Uri mediaStoreUri;
    private long sizeInBytes;
    private MimeType mimeType;
    private long duration;

    public static final String[] AUDIO_PROJECTION_QUERY = {
            //Audio info
            MediaStore.Audio.Media._ID, // 0 <!DOES NOT EXISTS WITHIN THE DEFAULT COLUMNS FOR AUDIO!>
            MediaStore.Audio.Media.ALBUM, // 1
            MediaStore.Audio.Media.ARTIST, // 2
            MediaStore.Audio.Media.TRACK, // 3
            MediaStore.Audio.Media.YEAR, // 4
            MediaStore.Audio.Media.DATE_ADDED, // 5
            MediaStore.Audio.Media.DURATION, // 6
            MediaStore.Audio.Media.TITLE_KEY, // 7
            //Element info
            MediaStore.Audio.Media.IS_ALARM, // 8
            MediaStore.Audio.Media.IS_MUSIC, // 9
            MediaStore.Audio.Media.IS_PODCAST, // 10
            MediaStore.Audio.Media.IS_NOTIFICATION, // 11
            MediaStore.Audio.Media.IS_RINGTONE, // 12
            MediaStore.Audio.Media.TITLE, // 13 <!DOES NOT EXISTS WITHIN THE DEFAULT COLUMNS FOR AUDIO!>
            MediaStore.Audio.Media.SIZE, // 14 <!DOES NOT EXISTS WITHIN THE DEFAULT COLUMNS FOR AUDIO!>
            MediaStore.Audio.Media.MIME_TYPE // 15 <!DOES NOT EXISTS WITHIN THE DEFAULT COLUMNS FOR AUDIO!>
    };

    public MediaStoreAudio(Cursor cursor, Uri mediaStoreUri, String parentId, String transientId, final URLBuilder urlBuilder) {
        PersonWithRole artist;
        PersonWithRole[] artists;
        this.mediaStoreId = cursor.getLong(ID); // Used as an persistent identifier
        this.mediaStoreUri = mediaStoreUri;
        setId(transientId);
        setParentID(parentId);
        setCreator(MediaDBContent.CREATOR);
        if (!cursor.isNull(ALBUM)) {
            setAlbum(cursor.getString(ALBUM));
        }
        if (!cursor.isNull(TITLE)) {
            setTitle(cursor.getString(TITLE));
        }
        if (!cursor.isNull(DATE_ADDED)) {
            setDate(CommonPartsOfMediaSnuggler.DEFAULT_MEDIA_DATE_FORMAT.format(new Date(cursor.getLong(1) * 1000)));
        }
        if(!cursor.isNull(ARTIST)) {
            artist = new PersonWithRole(
                    cursor.getString(ARTIST),
                    CommonPartsOfMediaSnuggler.DEFAULT_ARTIST_ROLE_IF_UNKNOWN
            );
            artists = new PersonWithRole[1];
            artists[0] = artist;
            setArtists(artists);
        }else{
            artist = new PersonWithRole(
                    CommonPartsOfMediaSnuggler.DEFAULT_ARTIST_NAME_IF_UNKNOWN,
                    CommonPartsOfMediaSnuggler.DEFAULT_ARTIST_ROLE_IF_UNKNOWN
            );
            artists = new PersonWithRole[1];
            artists[0] = artist;
            setArtists(artists);
        }
        if(!cursor.isNull(DURATION)) {
            this.duration = cursor.getLong(DURATION);
        }
        if(!cursor.isNull(SIZE)){
            this.sizeInBytes = cursor.getLong(SIZE);
        }
        if(!cursor.isNull(MIME_TYPE)) {
            this.mimeType = MimeType.valueOf(cursor.getString(MIME_TYPE));
        } else {
            this.mimeType = MimeType.valueOf(CommonPartsOfMediaSnuggler.MEDIA_AUDIO_DEFAULT_MIME_TYPE_IF_NOT_FOUND);
        }
        Res resource = new Res() {
            @Override
            public String getValue() {
                return urlBuilder.getURL(MediaStoreAudio.this);
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
