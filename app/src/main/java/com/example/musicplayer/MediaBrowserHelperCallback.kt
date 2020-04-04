package com.example.musicplayer

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat

interface MediaBrowserHelperCallback {

    fun onMetadataChanged(metadata: MediaMetadataCompat)

    fun onPlaybackStateChanged(state: PlaybackStateCompat)

    fun onQueueTitleChanged(title: CharSequence?)

    fun onProgressChanged(progress : Int)
}