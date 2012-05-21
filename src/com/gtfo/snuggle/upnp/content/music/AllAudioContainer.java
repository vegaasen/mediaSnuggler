package com.gtfo.snuggle.upnp.content.music;

import com.gtfo.snuggle.common.CommonConstantsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.container.MusicAlbum;
import org.teleal.cling.support.model.item.Item;

import java.util.Iterator;
import java.util.List;

public class AllAudioContainer extends MusicAlbum {

    public AllAudioContainer(Container audioContainer) {
        setId(MediaDBContent.ID.appendRandom(audioContainer));
        setParentID(audioContainer.getId());
        setDescription(String.format(CommonConstantsOfMediaSnuggler.ALL_MEDIA_CONATINER_DESC, "audio"));
        setTitle(CommonConstantsOfMediaSnuggler.ALL_MEDIA_CONTAINER_TITLE);

        setCreator(MediaDBContent.CREATOR);

        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);
    }

    public MediaStoreAudio getItem(long mediaStoreId) {
        for (Item item : getItems()) {
            if (item instanceof MediaStoreAudio) {
                MediaStoreAudio audio = (MediaStoreAudio) item;
                if (audio.getMediaStoreId() == mediaStoreId)
                    return audio;
            }
        }
        return null;
    }

    public void removeItemsNotIdentified (List<Long> mediaStoreIds) {
        Iterator<Item> it = getItems().iterator();
        while (it.hasNext()) {
            MediaStoreAudio audio = (MediaStoreAudio) it.next();
            boolean found = false;
            for (long mediaStoreId : mediaStoreIds) {
                if (audio.getMediaStoreId() == mediaStoreId){
                    found = true;
                }
            }
            if (!found) {
                it.remove();
            }
        }
    }

}
