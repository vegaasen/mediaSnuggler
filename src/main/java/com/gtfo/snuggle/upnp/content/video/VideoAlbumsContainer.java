package com.gtfo.snuggle.upnp.content.video;

import com.gtfo.snuggle.common.CommonConstantsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;

/**
 * Contains all video-elements. Can be considered as the "mother"-container/root-container.
 *
 * @author vegaasen
 * @since 0.1.c
 */
public class VideoAlbumsContainer extends Container {

    public VideoAlbumsContainer(Container videoAlbumsContainer) {
        setId(MediaDBContent.ID.appendRandom(videoAlbumsContainer));
        setParentID(videoAlbumsContainer.getId());

        setTitle(String.format(CommonConstantsOfMediaSnuggler.TOP_LEVEL_CONTAINER_TITLE, "Video"));
        setCreator(MediaDBContent.CREATOR);

        setClazz(MediaDBContent.CLASS_CONTAINER);
        
        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);
    }

}
