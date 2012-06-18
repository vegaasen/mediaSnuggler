package com.gtfo.snuggle.upnp.content.music;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.gtfo.snuggle.upnp.common.CommonPartsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.MediaDBContent;
import com.gtfo.snuggle.upnp.content.RootContainer;
import com.gtfo.snuggle.upnp.content.URLBuilder;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Album;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.MusicTrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Contains all audio-elements. Can be considered as the "mother"-container/root-container.
 * TODO: Not a big issue, but we should also add a folder named "Categories" that contains all of the registered cat's.
 *
 * @author vegaasen
 * @since 0.1.a
 */

public class AudioContainer extends Container {

    private static final Logger LOGGER = Logger.getLogger(AudioContainer.class.getName());

    private final MediaDBContent audioContent;
    private AllAudioContainer allAudioContainer;

    public AudioContainer(RootContainer rootContainer) {
        audioContent = rootContainer.getContent();

        setId(MediaDBContent.ID.appendRandom(rootContainer));
        setParentID(rootContainer.getId());

        setTitle("Audio");
        setCreator(MediaDBContent.CREATOR);

        setClazz(MediaDBContent.CLASS_CONTAINER);

        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);

        setChildCount(2);

        addContainer(new AudioAlbumsContainer(this));
        addContainer(new AllAudioContainer(this));
        LOGGER.fine("Added new AudioContainer that will contain all elements of audio.");
    }

    public void update(ContentResolver androidContentResolver, Uri contentUri) {
        List<Long> identifiers = new ArrayList<Long>();
        Long id;
        Uri androidMediaStoreUri;
        MediaStoreAudio existingAudioElement;

        LOGGER.fine("Starting to update the repository of elements. Currently on " + contentUri);

        Cursor cursor = androidContentResolver.query(
                contentUri,
                MediaStoreAudio.AUDIO_PROJECTION_QUERY,
                null,
                null,
                MediaStore.Audio.Media.ALBUM
        );
        //TODO: What is the best sort-order? By album, album_key/id or by artist?

        if(!cursor.moveToFirst()) {
            return;
        }

        allAudioContainer = getAllAudioContainer();

        while(cursor.moveToNext()) { // same as hasNext();
            id = cursor.getLong(CommonPartsOfMediaSnuggler.ZERO);
            androidMediaStoreUri = ContentUris.withAppendedId(contentUri, id);
            identifiers.add(cursor.getLong(CommonPartsOfMediaSnuggler.ZERO));
            LOGGER.fine("Result item with identifier " + id + " added.");

            if (!CommonPartsOfMediaSnuggler.elementExistsWithinContainer(allAudioContainer.getItem(id))) {
                addElementsToContainer(cursor, id, androidMediaStoreUri);
            } else {
                existingAudioElement = allAudioContainer.getItem(id);
                updateFoundElementWithData(cursor, id, androidMediaStoreUri, existingAudioElement);
            }
        }

        if(identifiers.size()>0) {
            purgeAudioElementsContainerForDuplicates(identifiers);
            updateAudioAlbums();
            LOGGER.fine("Total items after crud: " + allAudioContainer.getChildCount());
        }
        
    }

    protected void updateAudioAlbums() {
        AudioAlbumsContainer audioAlbums = getAlbumsContainer();
        boolean addedToExistingAlbum;
        Iterator<Container> audioAlbumsContainerIterator;
        Album newAlbum;

        for (Container album : audioAlbums.getContainers()) {
            album.getItems().clear();
        }

        for (MusicTrack musicTrack : getAllAudioContainer().getMusicTracks()) {
            if (musicTrack.getAlbum() == null) {
                continue;
            }
            addedToExistingAlbum = false;
            for (Container album : audioAlbums.getContainers()) {
                if (album.getTitle().equals(musicTrack.getAlbum())) {
                    album.addItem(musicTrack);
                    addedToExistingAlbum = true;
                    break;
                }
            }
            if (!addedToExistingAlbum) {
                newAlbum = new Album(
                        MediaDBContent.ID.appendRandom(this),
                        this,
                        musicTrack.getAlbum(),
                        MediaDBContent.CREATOR,
                        CommonPartsOfMediaSnuggler.ZERO);
                audioAlbums.addContainer(newAlbum);
                newAlbum.addItem(musicTrack);
            }
        }

        audioAlbumsContainerIterator = audioAlbums.getContainers().iterator();
        while (audioAlbumsContainerIterator.hasNext()) {
            Container album = audioAlbumsContainerIterator.next();
            album.setChildCount(album.getItems().size());
            if (album.getItems().size() == CommonPartsOfMediaSnuggler.ZERO) {
                audioAlbumsContainerIterator.remove();
            }
        }

        audioAlbums.setChildCount(audioAlbums.getContainers().size());
    }

    private void addElementsToContainer(Cursor cursor, Long id, Uri mediaStoreUri) {
        allAudioContainer.addItem(
                new MediaStoreAudio(
                        cursor,
                        mediaStoreUri,
                        allAudioContainer.getId(),
                        MediaDBContent.ID.appendRandom(allAudioContainer),
                        getUrlBuilder()
                )
        );
        LOGGER.info("Created new item for persistent id: " + id);
    }

    private void updateFoundElementWithData(Cursor cursor,
                                            Long id,
                                            Uri mediaStoreUri,
                                            MediaStoreAudio theExistingAudioElement) {
        allAudioContainer.getItems().set(
                allAudioContainer.getItems().indexOf(theExistingAudioElement),
                new MediaStoreAudio(
                        cursor,
                        mediaStoreUri,
                        theExistingAudioElement.getParentID(),
                        theExistingAudioElement.getId(),
                        getUrlBuilder()
                )
        );
        LOGGER.info("Updated item for persistent id: " + id);
    }

    private void purgeAudioElementsContainerForDuplicates(List<Long> identifiers) {
        allAudioContainer.removeItemsNotIdentified(identifiers);
        allAudioContainer.setChildCount(allAudioContainer.getItems().size());
    }

    public AllAudioContainer getAllAudioContainer() {
        return (AllAudioContainer) getContainers().get(1);
    }

    public AudioAlbumsContainer getAlbumsContainer() {
        return (AudioAlbumsContainer) getContainers().get(CommonPartsOfMediaSnuggler.ZERO);
    }

    private URLBuilder getUrlBuilder() {
        return audioContent.getUrlBuilder();
    }

}
