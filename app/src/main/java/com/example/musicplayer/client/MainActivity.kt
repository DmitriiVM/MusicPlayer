package com.example.musicplayer.client

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.playback_info.*

class MainActivity : AppCompatActivity(), RecyclerViewAdapter.OnTrackClickListener {

    private lateinit var mediaBrowserViewModel: MediaBrowserViewModel
    private lateinit var adapter : RecyclerViewAdapter
    private var isFirstIcon = true
    private var showAnimation = true
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            showAnimation = false
            isFirstLoad = savedInstanceState.getBoolean(KEY_FIRST_LOAD)
        }

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
            val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            seekBar.max = duration.toInt()

            Log.d("mmm", "MainActivity :  showAnimation --  $showAnimation")

            val icon = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON)
            if (isFirstIcon) {
                imageViewIcon1.setImageBitmap(icon)
                imageViewIcon1.visibility = View.VISIBLE

                if (showAnimation) {

                    Log.d("mmm", "MainActivity :  subscribeObservers --  1")
                    imageViewIcon1.scaleFromZero()
                    imageViewIcon2.translateToLeft()




                }
                else {
                    showAnimation = true
//                    imageViewIcon2.visibility = View.GONE
                }

                isFirstIcon = false


            } else {
                imageViewIcon2.setImageBitmap(icon)
                imageViewIcon2.visibility = View.VISIBLE


                if (showAnimation)
                {

                    Log.d("mmm", "MainActivity :  subscribeObservers --  2")
                    imageViewIcon2.scaleFromZero()
                    imageViewIcon1.translateToLeft()



                }
                else {
                    showAnimation = true
//                    imageViewIcon1.visibility = View.GONE
                }

                isFirstIcon = true

            }

        })
        mediaBrowserViewModel.currentPositionLiveData.observe(this, Observer {
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

                    imageViewPlayPause.rotate()
                    imageViewSkipToNext.translateArrowButton(20f)
                    imageViewSkipToPrevious.translateArrowButton(-20f)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    imageViewPlayPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
                    imageViewPlayPause.setOnClickListener {
                        mediaBrowserViewModel.getTransportControls().play()
                    }

                    imageViewPlayPause.rotate()
                    imageViewSkipToNext.translateArrowButton(20f)
                    imageViewSkipToPrevious.translateArrowButton(-20f)
                }
            }
        })
        mediaBrowserViewModel.playListLiveData.observe(this, Observer {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            adapter.setPlayList(it)
        })
        mediaBrowserViewModel.onMediaControllerConnected.observe(this, Observer {
            if (isFirstIcon){
                mediaBrowserViewModel.getTransportControls().prepare()
                isFirstLoad = false
            }
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


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_FIRST_LOAD, isFirstLoad)
    }

    companion object {
        private const val KEY_FIRST_LOAD = "first_load"
    }
}



