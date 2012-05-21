package com.gtfo.snuggle.upnp.content.photos;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.gtfo.snuggle.upnp.common.CommonPartsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import com.gtfo.snuggle.upnp.content.RootContainer;
import com.gtfo.snuggle.upnp.content.URLBuilder;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Album;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Photo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * The "mother" of the photos. This is the actual top-level container.
 *
 * @author vegaasen
 * @since 0.1.a
 */

public class PhotosContainer extends Container {

    private static final Logger LOGGER = Logger.getLogger(PhotosContainer.class.getName());

    private final MediaDBContent photoContent;
    private AllPhotosContainer allPhotosContainer;

    public PhotosContainer(RootContainer rootContainer) {
        this.photoContent = rootContainer.getContent();
        setId(MediaDBContent.ID.appendRandom(rootContainer));
        setParentID(rootContainer.getId());

        setTitle("Photos");
        setCreator(MediaDBContent.CREATOR);

        setClazz(MediaDBContent.CLASS_CONTAINER);

        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);

        setChildCount(2);

        addContainer(new PhotoAlbumsContainer(this));
        addContainer(new AllPhotosContainer(this));
    }

    public void update(ContentResolver androidContentResolver, Uri contentUri) {
        List<Long> identifiers = new ArrayList<Long>();
        Long id;
        Uri androidMediaStoreUri;
        MediaStorePhoto existingPhoto;

        LOGGER.info("Querying photoContent: " + contentUri);

        Cursor cursor = androidContentResolver.query(
                contentUri,
                MediaStorePhoto.PROJECTION_QUERY,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " desc"
        );

        if (!cursor.moveToFirst()) {
            return;
        }

        allPhotosContainer = getAllPhotosContainer();

        while (cursor.moveToNext()) {
            id = cursor.getLong(CommonPartsOfMediaSnuggler.ZERO);
            androidMediaStoreUri = ContentUris.withAppendedId(contentUri, id);
            identifiers.add(cursor.getLong(CommonPartsOfMediaSnuggler.ZERO));
            LOGGER.info("Result item with identifier: " + id);

            if (!CommonPartsOfMediaSnuggler.elementExistsWithinContainer(allPhotosContainer.getItem(id))) {
                addElementToContainer(cursor, id, androidMediaStoreUri);
            } else {
                existingPhoto = allPhotosContainer.getItem(id);
                updateFoundElementWithData(cursor, id, androidMediaStoreUri, existingPhoto);
            }
        }
        purgePhotoElementsContainerForDuplicates(identifiers);
        updateAlbums();
        LOGGER.info("Total items after crud: " + allPhotosContainer.getChildCount());
    }


    protected void updateAlbums() {
        PhotoAlbumsContainer photoAlbums = getAlbumsContainer();
        boolean addedToExistingAlbum;
        Iterator<Container> photoAlbumsContainerIterator;

        for (Container album : photoAlbums.getContainers()) {
            album.getItems().clear();
        }

        for (Photo photo : getAllPhotosContainer().getPhotos()) {
            if (photo.getAlbum() == null) {
                continue;
            }
            addedToExistingAlbum = false;
            for (Container album : photoAlbums.getContainers()) {
                if (album.getTitle().equals(photo.getAlbum())) {
                    album.addItem(photo);
                    addedToExistingAlbum = true;
                    break;
                }
            }
            if (!addedToExistingAlbum) {
                Album newAlbum = new Album(
                        MediaDBContent.ID.appendRandom(this),
                        this,
                        photo.getAlbum(),
                        MediaDBContent.CREATOR,
                        CommonPartsOfMediaSnuggler.ZERO);
                photoAlbums.addContainer(newAlbum);
                newAlbum.addItem(photo);
            }
        }

        photoAlbumsContainerIterator = photoAlbums.getContainers().iterator();
        while (photoAlbumsContainerIterator.hasNext()) {
            Container album = photoAlbumsContainerIterator.next();
            album.setChildCount(album.getItems().size());
            if (album.getItems().size() == CommonPartsOfMediaSnuggler.ZERO) {
                photoAlbumsContainerIterator.remove();
            }
        }

        photoAlbums.setChildCount(photoAlbums.getContainers().size());
    }

    private void addElementToContainer(Cursor cursor, Long id, Uri mediaStoreUri) {
        allPhotosContainer.addItem(
                new MediaStorePhoto(
                        cursor,
                        mediaStoreUri,
                        allPhotosContainer.getId(),
                        MediaDBContent.ID.appendRandom(allPhotosContainer),
                        getUrlBuilder()
                )
        );
        LOGGER.info("Created new item for persistent id: " + id);
    }

    private void updateFoundElementWithData(Cursor cursor, Long id, Uri mediaStoreUri, MediaStorePhoto existingItem) {
        allPhotosContainer.getItems().set(
                allPhotosContainer.getItems().indexOf(existingItem),
                new MediaStorePhoto(
                        cursor,
                        mediaStoreUri,
                        existingItem.getParentID(),
                        existingItem.getId(),
                        getUrlBuilder()
                )
        );
        LOGGER.finer("Updated item for persistent id: " + id);
    }

    private void purgePhotoElementsContainerForDuplicates(List<Long> identifiers) {
        allPhotosContainer.removeItemsNotIdentified(identifiers);
        allPhotosContainer.setChildCount(allPhotosContainer.getItems().size());
    }

    private URLBuilder getUrlBuilder() {
        return photoContent.getUrlBuilder();
    }

    public PhotoAlbumsContainer getAlbumsContainer() {
        return (PhotoAlbumsContainer) getContainers().get(CommonPartsOfMediaSnuggler.ZERO);
    }

    public AllPhotosContainer getAllPhotosContainer() {
        return (AllPhotosContainer) getContainers().get(1);
    }

}

