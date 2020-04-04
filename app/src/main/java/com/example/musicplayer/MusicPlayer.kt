package com.example.musicplayer

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ServiceCompat.stopForeground
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.offline.DownloadService.startForeground
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PLAYBACK_CHANNEL_ID = "playback_channel"
private const val NOTIFICATION_ID = 555

class MusicPlayer(private val service: Service) {

    private lateinit var exoPlayer: SimpleExoPlayer
    private var playList = listOf<MediaMetadataCompat>()
    private var playbackInfoListener: PlaybackInfoListener? = null


    fun initializePlayer(
        context: Context,
        playList: List<MediaMetadataCompat>,
        playbackInfoListener: PlaybackInfoListener
    ) {
        this.playbackInfoListener = playbackInfoListener
        this.playList = playList

        exoPlayer = ExoPlayerFactory.newSimpleInstance(
            context,
            DefaultTrackSelector(),
            DefaultLoadControl()
        )  // ???
        exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)

        val notificationManager = buildNotificationManager(context)
        notificationManager.setPlayer(exoPlayer)
        exoPlayer.addListener(exoPlayerEventListener)

        val concatenatingMediaSource = buildMediaSource(context, playList)
        exoPlayer.prepare(concatenatingMediaSource, false, false)
    }

    private fun buildMediaSource(
        context: Context, playList: List<MediaMetadataCompat>
    ): ConcatenatingMediaSource {

        val concatenatingMediaSource = ConcatenatingMediaSource()
        val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
        val dataSourceFactory = DefaultDataSourceFactory(context, userAgent)
        playList.forEach {
            val trackResource = it.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).toInt()
            val uri = RawResourceDataSource.buildRawResourceUri(trackResource)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    private fun buildNotificationManager(context: Context): PlayerNotificationManager {
        return PlayerNotificationManager.createWithNotificationChannel(
            context,
            PLAYBACK_CHANNEL_ID,
            R.string.playback_channel_name,
            R.string.channel_description,
            NOTIFICATION_ID,
            DescriptionAdapter(context),
            NotificationListener()
        )
    }


    fun play() {
        exoPlayer.playWhenReady = true
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun skipToNext() {
        exoPlayer.next()
    }

    fun skipToPrevious() {
        exoPlayer.previous()
    }


    fun releasePlayer() {
//        playerNotificationManager.setPlayer(null)
        exoPlayer.release()
    }

    private fun startTrackingPlayback() {

        CoroutineScope(Dispatchers.IO).launch {// отписаться

            while (exoPlayer.isPlaying) {
                playbackInfoListener?.onProgressChanged(
                    exoPlayer.contentPosition
                )
                delay(200)
            }
        }
    }


    @SuppressLint("WrongConstant")
    private fun setState(state: Int) {
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, 0, 1.0f, SystemClock.elapsedRealtime())
            .build()
        playbackInfoListener?.onPlaybackStateChange(playbackState)
    }


    private val exoPlayerEventListener = object : Player.EventListener {

        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
            updateMediaMetadata()
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (playWhenReady) {
                        setState(PlaybackStateCompat.STATE_PLAYING)
                        startTrackingPlayback()
                    } else {
                        setState(PlaybackStateCompat.STATE_PAUSED)
                    }
                }
            }
        }


        override fun onLoadingChanged(isLoading: Boolean) {
            updateMediaMetadata()
            setState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    private fun updateMediaMetadata() {
        val currentTrackMediaMetadata = playList[exoPlayer.currentWindowIndex]

        var nextSongInfo = ""
        if (exoPlayer.nextWindowIndex != -1) {
            val nextSongArtist =
                playList[exoPlayer.nextWindowIndex].getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            val nextSongTitle =
                playList[exoPlayer.nextWindowIndex].getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            nextSongInfo = "$nextSongArtist - $nextSongTitle"
        }
        playbackInfoListener?.updateMediaMetadata(currentTrackMediaMetadata, nextSongInfo)
    }


    inner class DescriptionAdapter(private val context: Context) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): String =
            playList[exoPlayer.currentWindowIndex].getString(MediaMetadataCompat.METADATA_KEY_TITLE)

        override fun getCurrentContentText(player: Player): String? =
            playList[exoPlayer.currentWindowIndex].getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

        override fun getCurrentLargeIcon(
            player: Player, callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? =
            playList[exoPlayer.currentWindowIndex].getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON)

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
    }

    inner class NotificationListener : PlayerNotificationManager.NotificationListener {

        override fun onNotificationCancelled(notificationId: Int) {
            service.stopSelf()
        }

        override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
            ContextCompat.startForegroundService(
                service.applicationContext,
                Intent(service.applicationContext, MediaService::class.java)
            )
            service.startForeground(notificationId, notification)
        }

        override fun onNotificationPosted(
            notificationId: Int, notification: Notification?, ongoing: Boolean
        ) {
            if (ongoing) {
                ContextCompat.startForegroundService(
                    service.applicationContext,
                    Intent(service.applicationContext, MediaService::class.java)
                )
                service.startForeground(notificationId, notification)
            } else {
                service.stopForeground(false)
            }
        }
    }
}


