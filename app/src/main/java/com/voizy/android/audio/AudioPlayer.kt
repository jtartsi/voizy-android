package com.voizy.android.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AudioPlayer {

    private val isPlaying: Boolean
        get() = mediaPlayer != null && mediaPlayer!!.isPlaying

    private val playbackEvents: PublishSubject<PlaybackInfo> = PublishSubject.create()
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackPath: String = ""

    fun getPlaybackEvents(): Observable<PlaybackInfo> {
        return playbackEvents
    }

    fun play(path: String): Observable<Int> {
        return Observable.defer {

            val durationInMillis = startPlayback(path)
            val playbackInfo = PlaybackInfo(PlaybackEvent.START, durationInMillis)
            playbackEvents.onNext(playbackInfo)
            Observable.just(startPlayback(path))
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

    fun togglePlay(path: String): Observable<PlaybackInfo> {
        return Observable.defer {
            val playbackInfo: PlaybackInfo = if (isPlaying && currentTrackPath == path) {
                val audioLength = stopPlayback()
                PlaybackInfo(PlaybackEvent.STOP, audioLength)
            } else if (isPlaying) {
                stopPlayback()
                val audioLength = startPlayback(path)
                PlaybackInfo(PlaybackEvent.SWITCH, audioLength)
            } else {
                val audioLength = startPlayback(path)
                PlaybackInfo(PlaybackEvent.START, audioLength)
            }
            playbackEvents.onNext(playbackInfo)
            Observable.just(playbackInfo)
        }
    }

    private fun startPlayback(filePath: String): Int {
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
            prepare()
            start()
            setOnCompletionListener {
                playbackEvents.onNext(PlaybackInfo(PlaybackEvent.STOP, durationInMillis))
                release()
                mediaPlayer = null
                currentTrackPath = ""
            }
            durationInMillis = duration
        }
        return durationInMillis
    }

    fun stopPlayback(): Int {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
                release()
                mediaPlayer = null
                currentTrackPath = ""
            }
        }
        return 0
    }
}