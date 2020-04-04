package com.example.musicplayer

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat

class MediaService : MediaBrowserServiceCompat() {

    private val TAG11 = "mmm"

    private var mediaSession: MediaSessionCompat? = null   // фасад для плеера

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(this, TAG).apply {
//            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)  // to handle playlist
            setCallback(mediaSessionCallback)
            setSessionToken(sessionToken)  // identify the service
        }


        val playList = ResourceMediaLibrary.getPlayListAsMediaMetadata(this)
        MusicPlayer.initializePlayer(this, playList, playbackInfoListener)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
//        Log.d(TAG11, "MediaService   -   onTaskRemoved: stopped")
//        MusicPlayer.stop()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
//        Log.d(TAG11, "MediaService   -   onDestroy: MediaPlayerAdapter stopped, and MediaSession released")
    }

    // controls access to the service  есть ли доступ к плейлисту и другим данным
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
//        Log.d(TAG11, "MediaService   -   onGetRoot: called. ")
        if (clientPackageName == applicationContext.packageName) {
//            Log.d("mmm", "MediaService :  onGetRoot --  1")
            return BrowserRoot(MEDIA_ROOT_ID, null)
        }
//        Log.d("mmm", "MediaService :  onGetRoot --  2")
        return BrowserRoot(EMPTY_MEDIA_ROOT_ID, null) // ??
    }

    // provides the ability for a client to build and display a menu of the MediaBrowserService's content hierarchy
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
//        Log.d(TAG11, "MediaService   -   onLoadChildren: called: " + parentId + ", " + result)
        if (parentId == EMPTY_MEDIA_ROOT_ID) {
            //  Browsing not allowed
            result.sendResult(null)
            return
        }
        val mediaItems = emptyList<MediaBrowserCompat.MediaItem>()

        if (MEDIA_ROOT_ID == parentId){
//            result.sendResult(ResourceMediaLibrary.getMediaItems())
        }
        result.sendResult(ResourceMediaLibrary.getMediaItemsList(this))
    }


    companion object {
        private const val TAG = "media_session"
        private const val MEDIA_ROOT_ID = "media_root_id"
        private const val EMPTY_MEDIA_ROOT_ID = "media_root_id"
    }


    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            MusicPlayer.play()
        }

        override fun onPause() {
            MusicPlayer.pause()
        }

        override fun onSkipToNext() {
            MusicPlayer.skipToNext()
        }

        override fun onSkipToPrevious() {
            MusicPlayer.skipToPrevious()
        }
    }












    private val playbackInfoListener = object : PlaybackInfoListener {
        override fun onPlaybackStateChange(state: PlaybackStateCompat) {
            mediaSession?.setPlaybackState(state)
        }

        override fun onProgressChanged(progress: Long) {
            mediaSession?.sendSessionEvent(progress.toString(), null)
        }

        override fun updateMediaMetadata(
            mediaMetadata: MediaMetadataCompat,
            nextSongInfo: CharSequence?
        ) {
            mediaSession?.setMetadata(mediaMetadata)
            mediaSession?.setQueueTitle(nextSongInfo)
        }
    }

}