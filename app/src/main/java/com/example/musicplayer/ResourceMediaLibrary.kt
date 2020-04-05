package com.example.musicplayer

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat

object ResourceMediaLibrary {

    private var playlist = arrayListOf<Int>()

    init {
        playlist = arrayListOf(
            R.raw.thekillers,
            R.raw.gorillaz_on_melancholy_hill,
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
}