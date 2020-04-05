package com.example.musicplayer

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mediaBrowserViewModel: MediaBrowserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaBrowserViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(MediaBrowserViewModel::class.java)

        imageViewSkipToNext.setOnClickListener {
            mediaBrowserViewModel.getTransportControls().skipToNext()
        }
        imageViewSkipToPrevious.setOnClickListener {
            mediaBrowserViewModel.getTransportControls().skipToPrevious()
        }
        subscribeObservers()
    }

    private fun subscribeObservers() {

        mediaBrowserViewModel.metadataLiveData.observe(this, Observer { metadata ->
            textViewArtist.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            textViewSong.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            imageViewIcon.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON))
            val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            progressBar.max = duration.toInt()
        })
        mediaBrowserViewModel.nextSongLiveData.observe(this, Observer {
            textViewNextTrack.text = getString(R.string.next_track, it)
        })
        mediaBrowserViewModel.progressLiveData.observe(this, Observer {
            progressBar.progress = it
        })
        mediaBrowserViewModel.playbackStateLiveData.observe(this, Observer { state ->
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    imageViewPlayPause.setImageResource(R.drawable.ic_pause_black_24dp)
                    imageViewPlayPause.setOnClickListener {
                        mediaBrowserViewModel.getTransportControls().pause()
                    }
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    imageViewPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                    imageViewPlayPause.setOnClickListener {
                        mediaBrowserViewModel.getTransportControls().play()
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mediaBrowserViewModel.onStart()
    }

    override fun onStop() {
        super.onStop()
        mediaBrowserViewModel.onStop()
    }
}
