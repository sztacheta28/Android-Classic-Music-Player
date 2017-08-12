package pl.androiddev.musicplayer.listeners;

import pl.androiddev.musicplayer.MusicService;

/**
 * Listens for playback changes to send the the fragments bound to this activity
 */
public interface MusicStateListener {

    /**
     * Called when {@link MusicService#REFRESH} is invoked
     */
    void restartLoader();

    /**
     * Called when {@link MusicService#PLAYLIST_CHANGED} is invoked
     */
    void onPlaylistChanged();

    /**
     * Called when {@link MusicService#META_CHANGED} is invoked
     */
    void onMetaChanged();

}
