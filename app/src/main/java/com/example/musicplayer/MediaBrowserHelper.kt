package com.example.musicplayer

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
import androidx.media.MediaBrowserServiceCompat
import java.lang.IllegalStateException

class MediaBrowserHelper(
    private val context: Context,
    val mediaBrowserHelperCallback: MediaBrowserHelperCallback
//    ,
//    private val mediaBrowserServiceCompat: MediaBrowserServiceCompat
) {

    private val TAG = "mmm"

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null


    fun onStart() {
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MediaService::class.java),
            connectionCallback, // let you know when the client successfully connected to the service
            null
        )
        mediaBrowser.connect() // to the service
//        Log.d(
//            "mmm",
//            "MediaBrowserHelper  -  onStart: CALLED: Creating MediaBrowser, and connecting"
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
//            "MediaBrowserHelper  -  onStop: CALLED: Releasing MediaController, Disconnecting from MediaBrowser"
//        )
        //-----
//        MediaControllerCompat.getMediaController(context).unregisterCallback()
//        mediaBrowser.disconnect()
    }


    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {

        //  to retrieve the media session token from the MediaBrowserService and use the token to create a MediaControllerCompat.
        override fun onConnected() {
//            Log.d(TAG, "MediaBrowserHelper  -  onConnected: CALLED")

            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken)
            mediaController!!.registerCallback(mediaControllerCallback)

            // subscribe for playlist
            mediaBrowser.subscribe(mediaBrowser.root, MediaBrowserSubscriptionCallback())

//            MediaControllerCompat.setMediaController(context, mediaController)
//            Log.d(
//                TAG,
//                "MediaBrowserHelper  -  onConnected: CALLED: subscribing to: " + mediaBrowser.getRoot()
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
//                "MediaBrowserHelper  -  onChildrenLoaded: CALLED: " + parentId + ", " + children.toString()
//            )
            children.forEach {
//                Log.d(
//                    TAG,
//                    "MediaBrowserHelper  -  onChildrenLoaded: CALLED: queue item: " + it.getMediaId()!!
//                )
                mediaController?.addQueueItem(it.description)
            }
        }
    }


    fun getTransportControls(): MediaControllerCompat.TransportControls {
        if (mediaController == null) {
            throw IllegalStateException("MediaController is null!")
        }
        return mediaController!!.transportControls
    }






    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let {
                mediaBrowserHelperCallback.onPlaybackStateChanged(it)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let {
                mediaBrowserHelperCallback.onMetadataChanged(it)
            }
        }

        override fun onQueueTitleChanged(title: CharSequence?) {
            mediaBrowserHelperCallback.onQueueTitleChanged(title)
        }






        override fun onExtrasChanged(extras: Bundle?) {
            super.onExtrasChanged(extras)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            event?.let {
                mediaBrowserHelperCallback.onProgressChanged(it.toInt())
            }
        }

        override fun onAudioInfoChanged(info: MediaControllerCompat.PlaybackInfo?) {
//            info?.
        }
    }
}