package com.example.musicplayer.server

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.media.session.PlaybackState
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.musicplayer.R
import com.example.musicplayer.client.MainActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicPlayer(private val service: MediaBrowserServiceCompat) {

    private lateinit var exoPlayer: SimpleExoPlayer
    private var playList = listOf<MediaMetadataCompat>()
    private var playbackInfoListener: PlaybackInfoListener? = null
    private var playbackState: PlaybackStateCompat? = null
    private val context = service.applicationContext
    private var isNotificationManagerSet = false

    fun initializePlayer(
        playList: List<MediaMetadataCompat>, playbackInfoListener: PlaybackInfoListener
    ) {
        this.playbackInfoListener = playbackInfoListener
        this.playList = playList

        exoPlayer = ExoPlayerFactory.newSimpleInstance(context)
        exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)
        exoPlayer.addListener(exoPlayerEventListener)
        val concatenatingMediaSource = buildMediaSource(playList)
        exoPlayer.prepare(concatenatingMediaSource, false, false)
    }

    private fun buildMediaSource(playList: List<MediaMetadataCompat>): ConcatenatingMediaSource {

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

    private fun setNotificationManager() {
        if (!isNotificationManagerSet) {
            val notificationManager = buildNotificationManager()
            notificationManager.setPlayer(exoPlayer)
            isNotificationManagerSet = true
        }
    }

    private fun buildNotificationManager(): PlayerNotificationManager {
        return PlayerNotificationManager.createWithNotificationChannel(
            context,
            PLAYBACK_CHANNEL_ID,
            R.string.playback_channel_name,
            R.string.channel_description,
            NOTIFICATION_ID,
            descriptionAdapter,
            notificationListener
        )
    }

    fun play() {
        exoPlayer.playWhenReady = true
        setNotificationManager()
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun skipToNext() {
        exoPlayer.next()
    }

    fun skipToPrevious() {
        if (exoPlayer.currentWindowIndex == 0) {
            exoPlayer.seekTo(0)
        } else {
            exoPlayer.previous()
        }
    }

    fun seekTo(pos: Long) {
        exoPlayer.seekTo(pos)
    }

    fun playSelectedTrack(position: Int) {
        exoPlayer.playWhenReady = true
        exoPlayer.seekTo(position, 0)
        setNotificationManager()
    }

    fun onPrepare() {
        updateMediaMetadata()
        playbackState?.let {
            playbackInfoListener?.onPlaybackStateChange(it)
        }
        playbackInfoListener?.onProgressChanged(exoPlayer.contentPosition)
    }

    fun releasePlayer() {
        exoPlayer.release()
    }

    @SuppressLint("WrongConstant")
    private fun setState(state: Int) {
        playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY_PAUSE
                        or PlaybackState.ACTION_SKIP_TO_NEXT
                        or PlaybackState.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(state, 0, 1.0f, SystemClock.elapsedRealtime())
            .build()
        playbackState?.let {
            playbackInfoListener?.onPlaybackStateChange(it)
        }
    }

    var isClickHandled = false

    private val exoPlayerEventListener = object : Player.EventListener {

        override fun onTracksChanged(
            trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray
        ) {
            if (isClickHandled) {
                updateMediaMetadata()
                playbackInfoListener?.onProgressChanged(0)
                isClickHandled = false
            } else {
                isClickHandled = true
            }

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

        private fun startTrackingPlayback() {
            MainScope().launch {
                while (true) {
                    if (!exoPlayer.isPlaying) cancel()
                    playbackInfoListener?.onProgressChanged(exoPlayer.contentPosition)
                    delay(200)
                }
            }
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            if (exoPlayer.playWhenReady) return
            setState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    private fun updateMediaMetadata() {
        val currentPosition = exoPlayer.currentWindowIndex
        val currentTrackMediaMetadata = playList[currentPosition]

        playbackInfoListener?.updateMediaMetadata(
            currentTrackMediaMetadata,
            currentPosition.toString()
        )
    }


    private val descriptionAdapter = object :
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

    private val notificationListener = object : PlayerNotificationManager.NotificationListener {

        override fun onNotificationCancelled(notificationId: Int) {
            service.stopSelf()
        }

        override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
            ContextCompat.startForegroundService(
                context, Intent(context, MediaService::class.java)
            )
            service.startForeground(notificationId, notification)
        }

        override fun onNotificationPosted(
            notificationId: Int, notification: Notification?, ongoing: Boolean
        ) {
            if (ongoing) {
                ContextCompat.startForegroundService(
                    context, Intent(context, MediaService::class.java)
                )
                service.startForeground(notificationId, notification)
            } else {
                service.stopForeground(false)
            }
        }
    }

    companion object {
        private const val PLAYBACK_CHANNEL_ID = "playback_channel"
        private const val NOTIFICATION_ID = 555
    }
}


