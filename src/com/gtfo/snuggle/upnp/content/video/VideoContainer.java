package com.gtfo.snuggle.upnp.content.video;

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
import org.teleal.cling.support.model.item.VideoItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Contains all video-elements. Can be considered as the "mother"-container/root-container.
 * TODO: Come up with a plan to make Video-lookups work as intended. As of now, there is no results, even if we got videos on the phone..
 * 
 * @author vegaasen
 * @since 0.1.c
 */
public class VideoContainer extends Container {

    private static final Logger LOGGER = Logger.getLogger(VideoContainer.class.getName());

    private final MediaDBContent videoContent;
    private AllVideoContainer allVideoContainer;

    public VideoContainer(RootContainer rootContainer) {
        videoContent = rootContainer.getContent();

        setId(MediaDBContent.ID.appendRandom(rootContainer));
        setParentID(rootContainer.getId());

        setTitle("Video");
        setCreator(MediaDBContent.CREATOR);

        setClazz(MediaDBContent.CLASS_CONTAINER);

        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);

        setChildCount(2);

        addContainer(new VideoAlbumsContainer(this));
        addContainer(new AllVideoContainer(this));
        LOGGER.fine("Added new VideoContainer that will contain all elements of video.");
    }

    public void update(ContentResolver androidContentResolver, Uri contentUri) {
        List<Long> identifiers = new ArrayList<Long>();
        Long id;
        Uri androidMediaStoreUri;
        MediaStoreVideo existingVideoElement;

        LOGGER.fine("Starting to update the repository of elements. Currently on " + contentUri);

        //Sorting by album
        Cursor cursor = androidContentResolver.query(
                contentUri,
                MediaStoreVideo.VIDEO_PROJECTION_QUERY,
                null,
                null,
                MediaStore.Video.Media.ALBUM
        );

        if(!cursor.moveToFirst()) {
            return;
        }

        allVideoContainer = getAllVideoContainer();

        while(cursor.moveToNext()) { // same as hasNext();
            id = cursor.getLong(CommonPartsOfMediaSnuggler.ZERO);
            androidMediaStoreUri = ContentUris.withAppendedId(contentUri, id);
            identifiers.add(cursor.getLong(CommonPartsOfMediaSnuggler.ZERO));
            LOGGER.fine("Result item with identifier " + id + " added.");

            if (!CommonPartsOfMediaSnuggler.elementExistsWithinContainer(allVideoContainer.getItem(id))) {
                addElementsToContainer(cursor, id, androidMediaStoreUri);
            } else {
                existingVideoElement = allVideoContainer.getItem(id);
                updateFoundElementWithData(cursor, id, androidMediaStoreUri, existingVideoElement);
            }
        }

        if(identifiers.size()>0) {
            purgeVideoElementsContainerForDuplicates(identifiers);
            updateVideoAlbums();
            LOGGER.fine("Total items after crud: " + allVideoContainer.getChildCount());
        }

    }

    protected void updateVideoAlbums() {
        VideoAlbumsContainer videos = getAlbumsContainer();
        Iterator<Container> videoAlbumsContainerIterator;
        Album newVideoAlbum;

        for (Container album : videos.getContainers()) {
            album.getItems().clear();
        }

        for (VideoItem videoItem : getAllVideoContainer().getVideos()) {
            newVideoAlbum = new Album(
                    MediaDBContent.ID.appendRandom(this),
                    videoItem.getParentID(),
                    videoItem.getTitle(),
                    MediaDBContent.CREATOR,
                    CommonPartsOfMediaSnuggler.ZERO);
            videos.addContainer(newVideoAlbum);
            newVideoAlbum.addItem(videoItem);
        }

        videoAlbumsContainerIterator = videos.getContainers().iterator();
        while (videoAlbumsContainerIterator.hasNext()) {
            Container album = videoAlbumsContainerIterator.next();
            album.setChildCount(album.getItems().size());
            if (album.getItems().size() == CommonPartsOfMediaSnuggler.ZERO) {
                videoAlbumsContainerIterator.remove();
            }
        }

        videos.setChildCount(videos.getContainers().size());
    }

    private void addElementsToContainer(Cursor cursor, Long id, Uri mediaStoreUri) {
        allVideoContainer.addItem(
                new MediaStoreVideo(
                        cursor,
                        mediaStoreUri,
                        allVideoContainer.getId(),
                        MediaDBContent.ID.appendRandom(allVideoContainer),
                        getUrlBuilder()
                )
        );
        LOGGER.info("Created new item for persistent id: " + id);
    }

    private void updateFoundElementWithData(Cursor cursor,
                                            Long id,
                                            Uri mediaStoreUri,
                                            MediaStoreVideo theExistingVideoElement) {
        allVideoContainer.getItems().set(
                allVideoContainer.getItems().indexOf(theExistingVideoElement),
                new MediaStoreVideo(
                        cursor,
                        mediaStoreUri,
                        theExistingVideoElement.getParentID(),
                        theExistingVideoElement.getId(),
                        getUrlBuilder()
                )
        );
        LOGGER.info("Updated item for persistent id: " + id);
    }

    private void purgeVideoElementsContainerForDuplicates(List<Long> identifiers) {
        allVideoContainer.removeItemsNotIdentified(identifiers);
        allVideoContainer.setChildCount(allVideoContainer.getItems().size());
    }

    public AllVideoContainer getAllVideoContainer() {
        return (AllVideoContainer) getContainers().get(1);
    }

    public VideoAlbumsContainer getAlbumsContainer() {
        return (VideoAlbumsContainer) getContainers().get(CommonPartsOfMediaSnuggler.ZERO);
    }

    private URLBuilder getUrlBuilder() {
        return videoContent.getUrlBuilder();
    }
}
