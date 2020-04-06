package com.example.musicplayer.client

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), RecyclerViewAdapter.OnTrackClickListener {

    private lateinit var mediaBrowserViewModel: MediaBrowserViewModel
    private lateinit var adapter : RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = RecyclerViewAdapter(this)

        mediaBrowserViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(MediaBrowserViewModel::class.java)

        imageViewSkipToNext.setOnClickListener {
            mediaBrowserViewModel.getTransportControls().skipToNext()
        }
        imageViewSkipToPrevious.setOnClickListener {
            mediaBrowserViewModel.getTransportControls().skipToPrevious()
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaBrowserViewModel.getTransportControls().seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { }

        })
        subscribeObservers()
    }

    private fun subscribeObservers() {

        mediaBrowserViewModel.metadataLiveData.observe(this, Observer { metadata ->
            imageViewIcon.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON))
            val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            seekBar.max = duration.toInt()
        })
        mediaBrowserViewModel.currentPositionLiveData.observe(this, Observer {
            Log.d("mmm", "MainActivity :  subscribeObservers --  ")
            adapter.selectItem(it.toString().toInt())
        })
        mediaBrowserViewModel.progressLiveData.observe(this, Observer {
            seekBar.progress = it
        })
        mediaBrowserViewModel.playbackStateLiveData.observe(this, Observer { state ->
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    imageViewPlayPause.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp)
                    imageViewPlayPause.setOnClickListener {
                        mediaBrowserViewModel.getTransportControls().pause()
                    }
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    imageViewPlayPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
                    imageViewPlayPause.setOnClickListener {
                        mediaBrowserViewModel.getTransportControls().play()
                    }
                }
            }
        })
        mediaBrowserViewModel.playListLiveData.observe(this, Observer {
//            val list = arrayListOf<String>()
//            it.forEach { mediaItem ->
//                list.add("${mediaItem.description.subtitle} - ${mediaItem.description.title}")
//            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            adapter.setPlayList(it)
        })
    }

    override fun onTrackClick(position: Int) {
        mediaBrowserViewModel.getTransportControls().playFromMediaId(position.toString(), null)
//        adapter.selectItem(position)
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
