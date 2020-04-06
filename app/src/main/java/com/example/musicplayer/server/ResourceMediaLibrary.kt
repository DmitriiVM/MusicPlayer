package com.example.musicplayer.server

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.example.musicplayer.R

object ResourceMediaLibrary {

    private var playlist = arrayListOf<Int>()

    init {
        playlist = arrayListOf(
            R.raw.gorillaz_on_melancholy_hill,
            R.raw.thekillers,
            R.raw.bullet,
            R.raw.fratellis_baby_dont_you_lie_to_me
//            R.raw.hiding_in_my_headphones
//            R.raw.jet
        )
    }

    fun getPlayListAsMediaMetadata(context: Context) : List<MediaMetadataCompat> {
        val list = arrayListOf<MediaMetadataCompat>()
        playlist.forEach {
            list.add(
                buildMediaMetadata(
                    context,
                    it
                )
            )
        }
        return list
    }

    fun getPlayListAsMediaItem(context: Context) : MutableList<MediaBrowserCompat.MediaItem>{
        val playListAsMediaItem = arrayListOf<MediaBrowserCompat.MediaItem>()
        getPlayListAsMediaMetadata(context).forEach {
            val mediaItem = MediaBrowserCompat.MediaItem(it.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
            playListAsMediaItem.add(mediaItem)
        }
        return playListAsMediaItem
    }

    private fun buildMediaMetadata(context: Context, track: Int): MediaMetadataCompat {
        val retriever = MediaMetadataRetriever()
        val uri = Uri.parse("android.resource://${context.packageName}/$track")
        retriever.setDataSource(context, uri)

        val iconByteArray: ByteArray
        iconByteArray = retriever.embeddedPicture
        val icon = BitmapFactory.decodeByteArray(iconByteArray, 0, iconByteArray.size)
        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, uri.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration.toLong())
            .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)
            .build()
    }
}