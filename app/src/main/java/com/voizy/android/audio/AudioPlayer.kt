package com.voizy.android.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask

class AudioPlayer {

    private val isPlaying: Boolean
        get() = mediaPlayer != null && mediaPlayer!!.isPlaying

    private val playbackEvents: PublishSubject<PlaybackInfo> = PublishSubject.create()
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackPath: String = ""
    private lateinit var onStopTimer: Timer

    val playbackEventStream = playbackEvents as Observable<PlaybackInfo>

    fun togglePlay(
        path: String,
        startPos: Int = 0,
        endPos: Int = 0
    ): Observable<PlaybackInfo> {
        return Observable.defer {
            val playbackInfo: PlaybackInfo = if (isPlaying && currentTrackPath == path) {
                val audioLength = stopPlayback()
                PlaybackInfo(PlaybackEvent.STOP, audioLength)
            } else {
                val audioLength = startPlayback(path, startPos, endPos)
                PlaybackInfo(PlaybackEvent.START, audioLength)
            }
            playbackEvents.onNext(playbackInfo)
            Observable.just(playbackInfo)
        }
    }

    fun stop(): Observable<Int> {
        return Observable.defer {
            val durationInMillis = stopPlayback()
            val playbackInfo = PlaybackInfo(PlaybackEvent.STOP, durationInMillis)
            playbackEvents.onNext(playbackInfo)
            Observable.just(durationInMillis)
        }
    }

    private fun startPlayback(
        filePath: String,
        startPos: Int = 0,
        endPos: Int = 0
    ): Int {
        currentTrackPath = filePath

        var durationInMillis: Int = -1

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        mediaPlayer = MediaPlayer()

        mediaPlayer?.apply {
            setDataSource(filePath)
            setAudioAttributes(audioAttributes)

            setOnPreparedListener {
                if (startPos > 0) {
                    seekTo(startPos)
                }
                start()
            }

            setOnCompletionListener {
                stopPlayback()
            }

            if (endPos > 0) {
                setOnStopListener(endPos)
            }

            prepare()
            durationInMillis = duration
        }
        return durationInMillis
    }

    fun stopPlayback(): Int {
        mediaPlayer?.apply {
            onStopTimer.cancel()
            playbackEvents.onNext(PlaybackInfo(PlaybackEvent.STOP, duration))
            stop()
            release()
            mediaPlayer = null
            currentTrackPath = ""
        }
        return 0
    }

    private fun setOnStopListener(endTime: Int) {
        onStopTimer = Timer()
        onStopTimer.schedule(object : TimerTask() {
            override fun run() {
                Timber.d("player-info setOnStopListener() run")
                if (mediaPlayer != null && mediaPlayer!!.currentPosition > endTime) {
                    Timber.d("player-info setOnStopListener() stopPlayback")
                    stopPlayback()
                }
            }
        }, 0, 10)
    }
}