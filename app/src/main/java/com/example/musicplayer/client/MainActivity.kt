package com.example.musicplayer.client

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), RecyclerViewAdapter.OnTrackClickListener {

    private lateinit var mediaBrowserViewModel: MediaBrowserViewModel
    private lateinit var adapter : RecyclerViewAdapter
    private var isFirstIcon = true

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
//            imageViewIcon1.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON))
            val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            seekBar.max = duration.toInt()


            val icon = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON)
            if (isFirstIcon) {
                imageViewIcon1.visibility = View.VISIBLE
                imageViewIcon1.scaleFromZero(icon)
                imageViewIcon2.translateToLeft()
                isFirstIcon = false
            } else {
                imageViewIcon2.visibility = View.VISIBLE
                imageViewIcon2.scaleFromZero(icon)
                imageViewIcon1.translateToLeft()
                isFirstIcon = true
            }

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
//                    val rotate = ObjectAnimator.ofFloat(imageViewPlayPause, "rotation", 0f, 360f)
//                    rotate.duration = 1000
//                    rotate.start()
                    imageViewPlayPause.rotate()

                    val animation1 = ObjectAnimator.ofFloat(imageViewSkipToNext, "translationY", 20f)
                    animation1.duration = 500
                    animation1.repeatCount = 1
                    animation1.repeatMode = ValueAnimator.REVERSE
                    animation1.start()
//                    imageViewSkipToNext.translateArrowButton(150f)

                    val animation2 = ObjectAnimator.ofFloat(imageViewSkipToPrevious, "translationY", -20f)
                    animation2.duration = 500
                    animation2.repeatCount = 1
                    animation2.repeatMode = ValueAnimator.REVERSE
                    animation2.start()
//                    imageViewSkipToPrevious.translateArrowButton(-150f)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    imageViewPlayPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
                    imageViewPlayPause.setOnClickListener {
                        mediaBrowserViewModel.getTransportControls().play()
                    }
//                    val rotate = ObjectAnimator.ofFloat(imageViewPlayPause, "rotation", 0f, 360f)
//                    rotate.duration = 1000
//                    rotate.start()
                    imageViewPlayPause.rotate()

                    val animation = ObjectAnimator.ofFloat(imageViewSkipToNext, "translationY", 20f)
                    animation.duration = 500
                    animation.repeatCount = 1
                    animation.repeatMode = ValueAnimator.REVERSE
                    animation.start()
//                    imageViewSkipToNext.translateArrowButton(150f)

                    val animation2 = ObjectAnimator.ofFloat(imageViewSkipToPrevious, "translationY", -20f)
                    animation2.duration = 500
                    animation2.repeatCount = 1
                    animation2.repeatMode = ValueAnimator.REVERSE
                    animation2.start()
//                    imageViewSkipToPrevious.translateArrowButton(-150f)
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



