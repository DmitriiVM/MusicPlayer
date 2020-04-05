package com.example.musicplayer

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.IMediaControllerCallback
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media.MediaBrowserServiceCompat
import java.lang.IllegalStateException

class MediaBrowserViewModel(
    private val app: Application
) : AndroidViewModel(app) {

    private val TAG = "mmm"

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null

    val _metadataLiveData = MutableLiveData<MediaMetadataCompat>()
    val metadataLiveData: LiveData<MediaMetadataCompat>
        get() = _metadataLiveData

    val _nextSongLiveData = MutableLiveData<CharSequence>()
    val nextSongLiveData: LiveData<CharSequence>
        get() = _nextSongLiveData

    val _playbackStateLiveData = MutableLiveData<PlaybackStateCompat>()
    val playbackStateLiveData: LiveData<PlaybackStateCompat>
        get() = _playbackStateLiveData

    val _progressLiveData = MutableLiveData<Int>()
    val progressLiveData: LiveData<Int>
        get() = _progressLiveData


    fun onStart() {   // init
        mediaBrowser = MediaBrowserCompat(
            app.applicationContext,
            ComponentName(app.applicationContext, MediaService::class.java),
            connectionCallback, // let you know when the client successfully connected to the service
            null
        )
        mediaBrowser.connect() // to the service
//        Log.d(
//            "mmm",
//            "MediaBrowserViewModel  -  onStart: CALLED: Creating MediaBrowser, and connecting"
//        )
    }

    fun onStop() {
        if (mediaController != null) {
            mediaController!!.unregisterCallback(mediaControllerCallback)
            mediaController = null
        }
        if (mediaBrowser.isConnected) {
            mediaBrowser.disconnect()
        }
//        mediaBrowser = null
//        Log.d(
//            "mmm",
//            "MediaBrowserViewModel  -  onStop: CALLED: Releasing MediaController, Disconnecting from MediaBrowser"
//        )
        //-----
//        MediaControllerCompat.getMediaController(context).unregisterCallback()
//        mediaBrowser.disconnect()
    }


    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {

        //  to retrieve the media session token from the MediaBrowserService and use the token to create a MediaControllerCompat.
        override fun onConnected() {
//            Log.d(TAG, "MediaBrowserViewModel  -  onConnected: CALLED")

            mediaController =
                MediaControllerCompat(app.applicationContext, mediaBrowser.sessionToken)
            mediaController!!.registerCallback(mediaControllerCallback)

            // subscribe for playlist
            mediaBrowser.subscribe(mediaBrowser.root, MediaBrowserSubscriptionCallback())
            Log.d("mmm", "MediaBrowserViewModel :  onConnected --  ${mediaBrowser.sessionToken.token.hashCode()}")

            mediaController!!.transportControls.prepare()

//            MediaControllerCompat.setMediaController(context, mediaController)
//            Log.d(
//                TAG,
//                "MediaBrowserViewModel  -  onConnected: CALLED: subscribing to: " + mediaBrowser.getRoot()
//            )

        }
    }

    inner class MediaBrowserSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
//            Log.d(
//                TAG,
//                "MediaBrowserViewModel  -  onChildrenLoaded: CALLED: " + parentId + ", " + children.toString()
//            )
            children.forEach {
                //                Log.d(
//                    TAG,
//                    "MediaBrowserViewModel  -  onChildrenLoaded: CALLED: queue item: " + it.getMediaId()!!
//                )
                mediaController?.addQueueItem(it.description)
            }
        }
    }


    fun getTransportControls(): MediaControllerCompat.TransportControls {
        return mediaController!!.transportControls
    }

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let {
                Log.d("mmm", "MediaBrowserViewModel :  onPlaybackStateChanged --  ")
                //                mediaBrowserHelperCallback.onPlaybackStateChanged(it)
                _playbackStateLiveData.value = it
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let {
                Log.d("mmm", "MediaBrowserViewModel :  onMetadataChanged --  ")
                //                mediaBrowserHelperCallback.onMetadataChanged(it)
                _metadataLiveData.value = it
            }
        }

        override fun onQueueTitleChanged(title: CharSequence?) {
//            mediaBrowserHelperCallback.onQueueTitleChanged(title)
            Log.d("mmm", "MediaBrowserViewModel :  onQueueTitleChanged --  ")
            _nextSongLiveData.value = title
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
//            Log.d("mmm", "MediaBrowserViewModel :  onSessionEvent --  ")
            event?.let {
                //                mediaBrowserHelperCallback.onProgressChanged(it.toInt())
                _progressLiveData.value = it.toInt()
            }
        }
    }
}