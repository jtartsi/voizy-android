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

    fun togglePlay(url: String): Observable<PlaybackInfo> {
        return Observable.defer {
            if (isPlaying && currentTrackPath == url) {
                val audioLength = stop()
                Observable.just(PlaybackInfo(PlaybackEvent.STOP, audioLength))
            } else if (isPlaying) {
                stop()
                val audioLength = play(url)
                Observable.just(PlaybackInfo(PlaybackEvent.SWITCH, audioLength))
            } else if (!isPlaying) {
                val audioLength = play(url)
                Observable.just(PlaybackInfo(PlaybackEvent.START, audioLength))
            } else {
                Observable.empty()
            }
        }
    }

    fun release() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    private fun play(filePath: String): Int {
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
                release()
                mediaPlayer = null
                currentTrackPath = ""
            }
            durationInMillis = duration
        }
        return durationInMillis
    }

    private fun stop(): Int {
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