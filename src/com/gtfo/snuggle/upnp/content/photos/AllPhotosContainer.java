package com.gtfo.snuggle.upnp.content.photos;

import com.gtfo.snuggle.common.CommonConstantsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.PhotoAlbum;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.Photo;

import java.util.Iterator;
import java.util.List;

public class AllPhotosContainer extends PhotoAlbum {

    public AllPhotosContainer(PhotosContainer photoContainer) {
        setId(MediaDBContent.ID.appendRandom(photoContainer));
        setParentID(photoContainer.getId());
        setDescription(String.format(CommonConstantsOfMediaSnuggler.ALL_MEDIA_CONATINER_DESC, "photo"));
        setTitle(CommonConstantsOfMediaSnuggler.ALL_MEDIA_CONTAINER_TITLE);

        setCreator(MediaDBContent.CREATOR);

        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);
    }

    public MediaStorePhoto getItem(long mediaStoreId) {
        for (Item item : getItems()) {
            if (item instanceof MediaStorePhoto) {
                MediaStorePhoto photo = (MediaStorePhoto) item;
                if (photo.getMediaStoreId() == mediaStoreId)
                    return photo;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public boolean containsItem(long mediaStoreId) {
        for (Photo p : getPhotos()) {
            if (((MediaStorePhoto)p).getMediaStoreId() == mediaStoreId) return true;
        }
        return false;
    }

    public void removeItemsNotIdentified(List<Long> mediaStoreIds) {
        Iterator<Item> it = getItems().iterator();
        while (it.hasNext()) {
            MediaStorePhoto photo = (MediaStorePhoto)it.next();
            boolean found = false;
            for (long mediaStoreId : mediaStoreIds) {
                if (photo.getMediaStoreId() == mediaStoreId) {
                    found = true;
                }
            }
            if (!found) {
                it.remove();
            }
        }
    }

}
