package com.gtfo.snuggle.upnp.content.music;

import com.gtfo.snuggle.common.CommonConstantsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;

/**
 * @author vegaasen
 * @since 0.1.b
 */

public class AudioAlbumsContainer extends Container {

    public AudioAlbumsContainer(Container audioAlbumContainer) {
        setId(MediaDBContent.ID.appendRandom(audioAlbumContainer));
        setParentID(audioAlbumContainer.getId());

        setTitle(String.format(CommonConstantsOfMediaSnuggler.TOP_LEVEL_CONTAINER_TITLE, "Audio"));
        setCreator(MediaDBContent.CREATOR);

        setClazz(MediaDBContent.CLASS_CONTAINER);
        
        setWriteStatus(WriteStatus.NOT_WRITABLE);
        setSearchable(false);
        setRestricted(true);
    }
}
