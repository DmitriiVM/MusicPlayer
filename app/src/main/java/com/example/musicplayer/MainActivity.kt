package com.example.musicplayer

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import java.io.File


class MainActivity : AppCompatActivity(),
MediaBrowserHelperCallback{


    private lateinit var mediaBrowserHelper: MediaBrowserHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaBrowserHelper = MediaBrowserHelper(this, this)

        imageViewSkipToNext.setOnClickListener {
            mediaBrowserHelper.getTransportControls().skipToNext()
        }
        imageViewSkipToPrevious.setOnClickListener {
            mediaBrowserHelper.getTransportControls().skipToPrevious()
        }
    }
    
    override fun onStart() {
        super.onStart()
        mediaBrowserHelper.onStart()
    }

    override fun onStop() {
        super.onStop()
        mediaBrowserHelper.onStop()
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat) {
        textViewArtist.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        textViewSong.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        imageViewIcon.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON))
        val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
        progressBar.max = duration.toInt()
    }

    override fun onQueueTitleChanged(title: CharSequence?) {
        textViewNextTrack.text = title ?: ""
    }

    override fun onProgressChanged(progress: Int) {
        progressBar.progress = progress
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
        when (state.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                Log.d("mmm", "MainActivity :  onPlaybackStateChanged --  STATE_PLAYING")
                imageViewPlayPause.setImageResource(R.drawable.ic_pause_black_24dp)
                imageViewPlayPause.setOnClickListener {
                    mediaBrowserHelper.getTransportControls().pause()
                }
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                Log.d("mmm", "MainActivity :  onPlaybackStateChanged -- STATE_PAUSED ")
                imageViewPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                imageViewPlayPause.setOnClickListener {
                    mediaBrowserHelper.getTransportControls().play()
                }
            }
        }
    }

}
