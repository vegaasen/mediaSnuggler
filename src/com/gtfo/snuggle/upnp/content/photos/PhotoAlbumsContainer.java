package com.gtfo.snuggle.upnp.content.photos;

import com.gtfo.snuggle.common.CommonConstantsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;

/**
 * @author vegasen
 * @since 0.1.a
 */

public class PhotoAlbumsContainer extends Container {

    public PhotoAlbumsContainer(PhotosContainer photoContainer) {
        setId(MediaDBContent.ID.appendRandom(photoContainer));
        setParentID(photoContainer.getId());

        setTitle(String.format(CommonConstantsOfMediaSnuggler.TOP_LEVEL_CONTAINER_TITLE, "Photo"));
        setCreator(MediaDBContent.CREATOR);

        setClazz(MediaDBContent.CLASS_CONTAINER);
        
        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);
    }
}

