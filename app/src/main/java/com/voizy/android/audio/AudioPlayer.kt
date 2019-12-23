package com.voizy.android.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class AudioPlayer {

    private val isPlaying: Boolean
        get() = mediaPlayer != null && mediaPlayer!!.isPlaying

    private val playbackEvents: PublishSubject<PlaybackInfo> = PublishSubject.create()
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackPath: String = ""

    val playbackEventStream = playbackEvents as Observable<PlaybackInfo>

    fun togglePlay(
        path: String,
        startPos: Int,
        endPos: Int,
        looping: Boolean = false
    ): Observable<PlaybackInfo> {
        return Observable.defer {
            val playbackInfo: PlaybackInfo = if (isPlaying && currentTrackPath == path) {
                val audioLength = stopPlayback()
                PlaybackInfo(PlaybackEvent.STOP, audioLength)
            } else {
                val audioLength = startPlayback(path, startPos, endPos, looping)
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

    // TODO audio-editor maybe put the startPlayback methdod's together
    private fun startPlayback(
        filePath: String,
        startPos: Int,
        endPos: Int,
        looping: Boolean = false
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
            prepare()

            // TODO audio-editor jump back to startPos in case looping

            setOnPreparedListener {
                seekTo(startPos)
                start()

                setOnInfoListener { mp, what, extra ->
                    Timber.d("player info $what, ${mp.currentPosition}")
                    if (mp.currentPosition > endPos) {
                        Timber.d("player info endPos: $endPos, currentPos: ${mp.currentPosition}")
                        Timber.d("player info stopping playback and releasing player")

                        // TODO audio-editor remove duplicates
                        playbackEvents.onNext(PlaybackInfo(PlaybackEvent.STOP, durationInMillis))
                        release()
                        mediaPlayer = null
                        currentTrackPath = ""
                    }
                    true
                }
            }

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