package com.example.musicplayer

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat

interface PlaybackInfoListener {

    fun onPlaybackStateChange(state : PlaybackStateCompat)

    fun onProgressChanged(progress : Long)

    fun onPlaybackComplete()

    fun updateMediaMetadata(mediaMetadata: MediaMetadataCompat, nextSongInfo: CharSequence?)
}