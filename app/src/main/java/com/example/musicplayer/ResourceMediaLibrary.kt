package com.example.musicplayer

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log

object ResourceMediaLibrary {

    private var playlist = arrayListOf<Int>()

    init {
        playlist = arrayListOf(
            R.raw.thekillers,
            R.raw.bullet,
            R.raw.fever_for_the_flava,
            R.raw.jet)
    }


    fun getPlayListAsMediaMetadata(context: Context) : List<MediaMetadataCompat> {
        val list = arrayListOf<MediaMetadataCompat>()
        playlist.forEach {
            list.add(buildMediaMetadataWithBitmap(context, it))
        }
        return list
    }

    private fun buildMediaMetadataWithBitmap(context: Context, track: Int): MediaMetadataCompat {
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

    //---------------------------------------------------------

    private fun buildMediaMetadata(context: Context, uri: Uri): MediaMetadataCompat {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)

        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)


        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "$artist $title")
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, uri.toString())
            .build()
    }


    fun getMediaItemsList(context: Context): MutableList<MediaBrowserCompat.MediaItem> {

        val mediaItemList = arrayListOf<MediaBrowserCompat.MediaItem>()
        val uriString1 = "android.resource://" + context.packageName + "/" + R.raw.thekillers
        mediaItemList.add(buildMediaItem(context, Uri.parse(uriString1)))

        val uriString2 = "android.resource://" + context.packageName + "/" + R.raw.bullet
        mediaItemList.add(buildMediaItem(context, Uri.parse(uriString2)))

        val uriString3 =
            "android.resource://" + context.packageName + "/" + R.raw.fever_for_the_flava
        mediaItemList.add(buildMediaItem(context, Uri.parse(uriString3)))

        val uriString4 = "android.resource://" + context.packageName + "/" + R.raw.jet
        mediaItemList.add(buildMediaItem(context, Uri.parse(uriString4)))

        return mediaItemList
    }




    private fun buildMediaItem(context: Context, uri: Uri) =
        MediaBrowserCompat.MediaItem(
            buildMediaMetadata(context, uri).description,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
}