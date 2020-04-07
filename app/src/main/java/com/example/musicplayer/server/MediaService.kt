package com.example.musicplayer.server

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat

class MediaService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var musicPlayer: MusicPlayer

    override fun onCreate() {
        super.onCreate()

        musicPlayer = MusicPlayer(this)
        mediaSession = MediaSessionCompat(this,
            TAG
        ).apply {
            setCallback(mediaSessionCallback)
            setSessionToken(sessionToken)
            isActive = true
        }

        val playList =
            ResourceMediaLibrary.getPlayListAsMediaMetadata(this)
        musicPlayer.initializePlayer(playList, playbackInfoListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.releasePlayer()
        mediaSession?.apply {
            isActive = false
            release()
        }
    }

    override fun onGetRoot(
        clientPackageName: String, clientUid: Int, rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(ResourceMediaLibrary.getPlayListAsMediaItem(this))
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

        override fun onSeekTo(pos: Long) {
            musicPlayer.seekTo(pos)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            mediaId?.let {
                musicPlayer.playSelectedTrack(mediaId.toInt())
            }
        }
    }

    private val playbackInfoListener = object :
        PlaybackInfoListener {

        override fun onPlaybackStateChange(state: PlaybackStateCompat) {
            mediaSession?.setPlaybackState(state)
        }

        override fun onProgressChanged(progress: Long) {
            mediaSession?.sendSessionEvent(progress.toString(), null)
        }

        override fun updateMediaMetadata(
            mediaMetadata: MediaMetadataCompat,
            currentPosition: CharSequence?
        ) {
            mediaSession?.setMetadata(mediaMetadata)
            mediaSession?.setQueueTitle(currentPosition)
        }
    }

    companion object {
        private const val TAG = "media_session"
        private const val MEDIA_ROOT_ID = "empty_media_root_id"
    }
}