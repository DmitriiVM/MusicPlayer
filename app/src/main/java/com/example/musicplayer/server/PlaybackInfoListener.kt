package com.example.musicplayer.server

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat

interface PlaybackInfoListener {

    fun onPlaybackStateChange(state : PlaybackStateCompat)

    fun onProgressChanged(progress : Long)

    fun updateMediaMetadata(mediaMetadata: MediaMetadataCompat, currentPosition: CharSequence?)
}