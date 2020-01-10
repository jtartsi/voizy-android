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
    private var onStopTimer: Timer = Timer()

    val playbackEventStream = playbackEvents as Observable<PlaybackInfo>

    fun togglePlay(
        path: String,
        startPos: Int = 0,
        endPos: Int = 0
    ): Observable<PlaybackInfo> {
        Timber.d("replay-iss togglePlay()")
        return Observable.defer {
            Timber.d("replay-iss togglePlay() defer")
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
        Timber.d("replay-iss stop()")
        return Observable.defer {
            Timber.d("replay-iss stop() defer")
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
        Timber.d("replay-iss startPlayback()")
        currentTrackPath = filePath

        var durationInMillis: Int = -1

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        mediaPlayer = MediaPlayer()

        mediaPlayer?.apply {
            Timber.d("replay-iss startPlayback() apply()")
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
            Timber.d("replay-iss startPlayback() apply() -done")
        }
        return durationInMillis
    }

    fun stopPlayback(): Int {
        Timber.d("replay-iss stopPlayback()")
        mediaPlayer?.apply {
            onStopTimer.cancel()
            playbackEvents.onNext(PlaybackInfo(PlaybackEvent.STOP, duration))
            stop()
            release()
            mediaPlayer = null
            currentTrackPath = ""
            Timber.d("replay-iss stopPlayback() apply done")
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