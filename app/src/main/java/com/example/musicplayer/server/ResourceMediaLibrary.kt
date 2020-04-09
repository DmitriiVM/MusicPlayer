package com.example.musicplayer.server

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.example.musicplayer.R

object ResourceMediaLibrary {

    private var playlist = arrayListOf(
        R.raw.thekillers,
        R.raw.gorillaz_on_melancholy_hill,
        R.raw.bullet,
        R.raw.vanilla_sky_just_dance,
        R.raw.weezer_island,
        R.raw.fratellis_baby_dont_you_lie_to_me
    )

    fun getPlayListAsMediaMetadata(context: Context) =
        arrayListOf<MediaMetadataCompat>().apply {
            playlist.forEach {
                this.add(buildMediaMetadataWithBitmap(context, it))
            }
        }

    private fun buildMediaMetadataWithBitmap(context: Context, track: Int): MediaMetadataCompat {
        val retriever = MediaMetadataRetriever()
        val uri =
            Uri.parse(context.getString(R.string.track_uri, context.packageName, track.toString()))
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


    fun getPlayListAsMediaItem(context: Context): MutableList<MediaBrowserCompat.MediaItem> =
        arrayListOf<MediaBrowserCompat.MediaItem>().apply {
            getPlayListAsMediaMetadataWithoutBitmap(context).forEach { metadata ->
                val description = MediaDescriptionCompat.Builder()
                    .setMediaId(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))
                    .setTitle(
                        context.getString(
                            R.string.song_title,
                            metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST),
                            metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                        )
                    )
                    .setSubtitle(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toString())
                    .build()
                val mediaItem = MediaBrowserCompat.MediaItem(
                    description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                )
                this.add(mediaItem)
            }
        }

    private fun getPlayListAsMediaMetadataWithoutBitmap(context: Context) =
        arrayListOf<MediaMetadataCompat>().apply {
            playlist.forEach {
                this.add(buildMediaMetadataWithoutBitmap(context, it))
            }
        }

    private fun buildMediaMetadataWithoutBitmap(context: Context, track: Int): MediaMetadataCompat {
        val retriever = MediaMetadataRetriever()
        val uri =
            Uri.parse(context.getString(R.string.track_uri, context.packageName, track.toString()))
        retriever.setDataSource(context, uri)

        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, uri.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration.toLong())
            .build()
    }
}