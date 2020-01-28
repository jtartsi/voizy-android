package com.voizy.android.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
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
    private var onStopTimer: Timer = Timer()

    val playbackEventStream = playbackEvents as Observable<PlaybackInfo>

    fun togglePlay(
        path: String,
        startPos: Int = 0,
        endPos: Int = 0
    ): Observable<PlaybackInfo> {
        Timber.d("playback-iss togglePlay()")
        return Observable.defer {
            val playbackInfo: PlaybackInfo = if (isPlaying && currentTrackPath == path) {
                Timber.d("playback-iss togglePlay() -> stop()")
                val audioLength = stopPlayback()
                PlaybackInfo(PlaybackEvent.STOP, audioLength)
            } else if (isPlaying && currentTrackPath != path) {
                Timber.d("playback-iss togglePlay() -> switch()")
                stopPlayback()
                val audioLength = startPlayback(path, startPos, endPos)
                PlaybackInfo(PlaybackEvent.SWITCH, audioLength)
            } else {
                Timber.d("playback-iss togglePlay() -> start()")
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        seekTo(startPos.toLong(), MediaPlayer.SEEK_CLOSEST)
                    } else {
                        seekTo(startPos)
                    }
                }
                Timber.d("playback-iss startPlayback()")
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
            Timber.d("playback-iss stopPlayback()")
        }
        return 0
    }

    private fun setOnStopListener(endTime: Int) {
        onStopTimer = Timer()
        onStopTimer.schedule(object : TimerTask() {
            override fun run() {
                if (mediaPlayer != null && mediaPlayer!!.currentPosition > endTime) {
                    stopPlayback()
                }
            }
        }, 0, 10)
    }
}