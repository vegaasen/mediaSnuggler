package com.gtfo.snuggle.upnp.content;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Observers for audio, video and photo elements on the phone. Watching the state and updating
 * each of the stores defined.
 *
 * @author vegaasen
 * @since 0.1.a
 */

public class MediaStoreObservers {

    private static final String TAG = MediaStoreObservers.class.getName();
    private static final boolean NOTIFY_FOR_DECENDANTS = false, SELF_CHANGE = false;

    private final Context context;
    private final RootContainer rootContainer;

    public MediaStoreObservers(Context context, RootContainer rootContainer) {
        this.context = context;
        this.rootContainer = rootContainer;
    }

    private final ContentObserver internalPhotosContentObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    getRootContainer().getPhotosContainer().update(
                            getContext().getContentResolver(),
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                }
            };

    private final ContentObserver externalPhotosContentObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    getRootContainer().getPhotosContainer().update(
                            getContext().getContentResolver(),
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }
            };

    private final ContentObserver internalAudioContentObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    getRootContainer().getAudioContainer().update(
                            getContext().getContentResolver(),
                            MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
                }
            };

    private final ContentObserver externalAudioContentObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    getRootContainer().getAudioContainer().update(
                            getContext().getContentResolver(),
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                }
            };

    private final ContentObserver internalVideoContentObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    getRootContainer().getVideoContainer().update(
                            getContext().getContentResolver(),
                            MediaStore.Video.Media.INTERNAL_CONTENT_URI);
                }
            };

    private final ContentObserver externalVideoContentObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    getRootContainer().getVideoContainer().update(
                            getContext().getContentResolver(),
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                }
            };

    public void register() {
        Log.d(TAG, "Registering observers - Photos | Audio | Video");
        //photos
        getContext().getContentResolver().registerContentObserver(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                NOTIFY_FOR_DECENDANTS,
                internalPhotosContentObserver
        );
        getContext().getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                NOTIFY_FOR_DECENDANTS,
                externalPhotosContentObserver
        );
        //audio
        getContext().getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                NOTIFY_FOR_DECENDANTS,
                internalAudioContentObserver
        );
        getContext().getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                NOTIFY_FOR_DECENDANTS,
                externalAudioContentObserver
        );
        //video
        getContext().getContentResolver().registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                NOTIFY_FOR_DECENDANTS,
                internalVideoContentObserver
        );
        getContext().getContentResolver().registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                NOTIFY_FOR_DECENDANTS,
                externalVideoContentObserver
        );
    }

    public void unRegister() {
        Log.d(TAG, "UnRegistering observers  - Photos | Audio | Video");
        //photos
        getContext().getContentResolver().unregisterContentObserver(internalPhotosContentObserver);
        getContext().getContentResolver().unregisterContentObserver(externalPhotosContentObserver);
        //audio
        getContext().getContentResolver().unregisterContentObserver(internalAudioContentObserver);
        getContext().getContentResolver().unregisterContentObserver(externalAudioContentObserver);
        //video
        getContext().getContentResolver().unregisterContentObserver(internalVideoContentObserver);
        getContext().getContentResolver().unregisterContentObserver(externalVideoContentObserver);
    }

    public void updateAll() {
        //photos
        internalPhotosContentObserver.onChange(SELF_CHANGE);
        externalPhotosContentObserver.onChange(SELF_CHANGE);
        //audio
        internalAudioContentObserver.onChange(SELF_CHANGE);
        externalAudioContentObserver.onChange(SELF_CHANGE);
        //video
        internalVideoContentObserver.onChange(SELF_CHANGE);
        externalVideoContentObserver.onChange(SELF_CHANGE);
    }

    public Context getContext() {
        return context;
    }

    public RootContainer getRootContainer() {
        return rootContainer;
    }
}
