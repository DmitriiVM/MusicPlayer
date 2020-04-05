package com.example.musicplayer

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat

class MediaService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var musicPlayer: MusicPlayer

    override fun onCreate() {
        super.onCreate()

        musicPlayer = MusicPlayer(this)
        mediaSession = MediaSessionCompat(this, TAG).apply {
            setCallback(mediaSessionCallback)
            setSessionToken(sessionToken)
        }

        val playList = ResourceMediaLibrary.getPlayListAsMediaMetadata(this)
        musicPlayer.initializePlayer(playList, playbackInfoListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
//        MusicPlayer.stop()
//        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
//        mediaSession?.release()
    }

    override fun onGetRoot(
        clientPackageName: String, clientUid: Int, rootHints: Bundle?
    ): BrowserRoot? {
        if (clientPackageName == applicationContext.packageName) {
            return BrowserRoot(MEDIA_ROOT_ID, null)
        }
        return BrowserRoot(EMPTY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == EMPTY_MEDIA_ROOT_ID) {
            result.sendResult(null)
            return
        }

        if (MEDIA_ROOT_ID == parentId) {
//            result.sendResult(ResourceMediaLibrary.getMediaItems())
        }
        result.sendResult(ResourceMediaLibrary.getMediaItemsList(this))
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            musicPlayer.play()
        }

        override fun onPause() {
            musicPlayer.pause()
        }

        override fun onSkipToNext() {
            musicPlayer.skipToNext()
        }

        override fun onSkipToPrevious() {
            musicPlayer.skipToPrevious()
        }

        override fun onPrepare() {
            musicPlayer.onPrepare()
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

    companion object {
        private const val TAG = "media_session"
        private const val MEDIA_ROOT_ID = "media_root_id"
        private const val EMPTY_MEDIA_ROOT_ID = "media_root_id"
    }
}