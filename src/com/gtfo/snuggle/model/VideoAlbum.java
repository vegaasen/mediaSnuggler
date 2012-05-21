package com.gtfo.snuggle.model;

import org.teleal.cling.support.model.container.Album;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.VideoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Container-element for Video-elements
 * Does not exists within the support-framework, but thats OK, we're just adding it here instead..
 *
 * @author vegaasen
 * @since 0.1.c
 * @version 1
 */
public class VideoAlbum extends Album {

    public static final Class CLAZZ = new Class("object.container.album.videoAlbum");

    public VideoAlbum() {
        setClazz(CLAZZ);
    }

    public VideoAlbum(Container container) {
        super(container);
    }

    public VideoAlbum (String id, Container parentContainer, String title, String creator, Integer childCount) {
        this(id, parentContainer.getId(), title, creator, childCount, null);
    }

    public VideoAlbum (String id, Container parentContainer, String title, String creator, Integer childCount, List<VideoItem> videos) {
        this(id, parentContainer.getId(), title, creator, childCount, videos);
    }

    public VideoAlbum (String id, String parentId, String title, String creator, Integer childCount) {
        this(id, parentId, title, creator, childCount, null);
    }

    public VideoAlbum (String id, String parentId, String title, String creator, Integer childCount, List<VideoItem> videos) {
        super(id, parentId, title, creator, childCount);
        setClazz(CLAZZ);
        if(videos!=null&&videos.size()>0) {
            addVideos(videos);
        }
    }

    public VideoItem[] getVideos() {
        List<VideoItem> movies = new ArrayList<VideoItem>();
        for(Item item : getItems()) {
            if(item instanceof VideoItem) {
                movies.add((VideoItem) item);
            }
        }
        return (VideoItem[]) movies.toArray();
    }

    public void addVideos(List<VideoItem> videos) {
        if(videos!=null && videos.size()>0) {
            
        }
    }

    public void addVideos(VideoItem[] videos) {
        if(videos!=null&&videos.length>0) {
            for(VideoItem video : videos) {
                video.setTitle(getTitle());
                addItem(video);
            }
        }
    }

}
