package com.gtfo.snuggle.upnp.content.video;

import com.gtfo.snuggle.common.CommonConstantsOfMediaSnuggler;
import com.gtfo.snuggle.model.VideoAlbum;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import java.util.Iterator;
import java.util.List;

/**
 * Container for all of the video-elements
 * Todo: introduce generics..
 * 
 * @author vegaasen
 * @since 0.1.c
 */

public class AllVideoContainer extends VideoAlbum {

    public AllVideoContainer(Container videoContainer) {
        setId(MediaDBContent.ID.appendRandom(videoContainer));
        setParentID(videoContainer.getId());
        setDescription(String.format(CommonConstantsOfMediaSnuggler.ALL_MEDIA_CONATINER_DESC, "video"));
        setTitle(CommonConstantsOfMediaSnuggler.ALL_MEDIA_CONTAINER_TITLE);

        setCreator(MediaDBContent.CREATOR);

        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);
    }

    public MediaStoreVideo getItem(long mediaStoreId) {
        for (Item item : getItems()) {
            if (item instanceof MediaStoreVideo) {
                MediaStoreVideo video = (MediaStoreVideo) item;
                if (video.getMediaStoreId() == mediaStoreId)
                    return video;
            }
        }
        return null;
    }

    public void removeItemsNotIdentified (List<Long> mediaStoreIds) {
        Iterator<Item> it = getItems().iterator();
        while (it.hasNext()) {
            MediaStoreVideo video = (MediaStoreVideo) it.next();
            boolean found = false;
            for (long mediaStoreId : mediaStoreIds) {
                if (video.getMediaStoreId() == mediaStoreId){
                    found = true;
                }
            }
            if (!found) {
                it.remove();
            }
        }
    }
}
