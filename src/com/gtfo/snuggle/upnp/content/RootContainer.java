package com.gtfo.snuggle.upnp.content;

import com.gtfo.snuggle.upnp.content.music.AudioContainer;
import com.gtfo.snuggle.upnp.content.photos.PhotosContainer;
import com.gtfo.snuggle.upnp.content.video.VideoContainer;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;

/**
 * The main container that will contain everything.
 * This then counts for the following containers:
 * 
 * a) Photos
 * b) Audio
 * c) Videos
 *
 * @author vegaasen
 * @since 0.1.a
 */

public class RootContainer extends Container {
    private static final int AMOUNT_OF_CURRENT_CONTAINERS = 3;

    protected final MediaDBContent content;

    public RootContainer(MediaDBContent content) {
        this.content = content;

        setId("0");
        setParentID("-1");
        setTitle("Root");

        setCreator(MediaDBContent.CREATOR);

        setClazz(MediaDBContent.CLASS_CONTAINER);

        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);
 
        setChildCount(AMOUNT_OF_CURRENT_CONTAINERS);

        addContainer(new PhotosContainer(this)); // get(0)
        addContainer(new AudioContainer(this)); // get(1)
        addContainer(new VideoContainer(this)); // get(2)
    }

    public MediaDBContent getContent() {
        return content;
    }

    public PhotosContainer getPhotosContainer() {
        return (PhotosContainer) getContainers().get(0);
    }

    public AudioContainer getAudioContainer() {
        return (AudioContainer) getContainers().get(1);
    }

    public VideoContainer getVideoContainer() {
        return (VideoContainer) getContainers().get(2);
    }
}
