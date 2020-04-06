package com.example.musicplayer.client

import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicplayer.server.MediaService

class MediaBrowserViewModel(private val app: Application) : AndroidViewModel(app) {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null

    private val _metadataLiveData = MutableLiveData<MediaMetadataCompat>()
    val metadataLiveData: LiveData<MediaMetadataCompat>
        get() = _metadataLiveData

    private val _currentPositionLiveData = MutableLiveData<CharSequence>()
    val currentPositionLiveData: LiveData<CharSequence>
        get() = _currentPositionLiveData

    private val _playbackStateLiveData = MutableLiveData<PlaybackStateCompat>()
    val playbackStateLiveData: LiveData<PlaybackStateCompat>
        get() = _playbackStateLiveData

    private val _progressLiveData = MutableLiveData<Int>()
    val progressLiveData: LiveData<Int>
        get() = _progressLiveData

    private val _playListLiveData = MutableLiveData<List<MediaBrowserCompat.MediaItem>>()
    val playListLiveData: LiveData<List<MediaBrowserCompat.MediaItem>>
        get() = _playListLiveData

    fun onStart() {
        mediaBrowser = MediaBrowserCompat(
            app.applicationContext,
            ComponentName(app.applicationContext, MediaService::class.java),
            connectionCallback,
            null
        )
        mediaBrowser.connect()
    }

    fun onStop() {
        mediaController?.unregisterCallback(mediaControllerCallback)
        if (mediaBrowser.isConnected) mediaBrowser.disconnect()
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaController =
                MediaControllerCompat(app.applicationContext, mediaBrowser.sessionToken)
            mediaController?.registerCallback(mediaControllerCallback)
            mediaBrowser.subscribe(mediaBrowser.root, mediaBrowserSubscriptionCallback)
            mediaController?.transportControls?.prepare()
        }
    }

    private val mediaBrowserSubscriptionCallback =
        object : MediaBrowserCompat.SubscriptionCallback() {

            override fun onChildrenLoaded(
                parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                _playListLiveData.value = children
            }
        }

    fun getTransportControls(): MediaControllerCompat.TransportControls =
        mediaController!!.transportControls

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let {
                _playbackStateLiveData.value = it
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let {
                _metadataLiveData.value = it
            }
        }

        override fun onQueueTitleChanged(currentPosition: CharSequence?) {
//            Log.d("mmm", "MediaBrowserViewModel :  onQueueTitleChanged --  ")
            _currentPositionLiveData.value = currentPosition
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            event?.let {
                _progressLiveData.value = it.toInt()
            }
        }



    }
}