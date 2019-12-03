package com.voizy.android.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class AudioPlayer(private val firebaseAnalytics: VoizyFirebaseAnalytics) {

    val isPlaying: Boolean
        get() = mediaPlayer != null && mediaPlayer!!.isPlaying

    private val playbackEvents: PublishSubject<PlaybackInfo> = PublishSubject.create()
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackPath: String = ""

    fun getPlaybackEvents(): Observable<PlaybackInfo> {
        return playbackEvents
    }

    // fun togglePlay(voizy: Voizy): Observable<PlaybackInfo> {
    //     return if (isPlaying && currentTrackPath == voizy.filePath) {
    //         val audioLength = stop()
    //         Observable.just(PlaybackInfo(PlaybackEvent.STOP, audioLength))
    //     } else if (isPlaying) {
    //         stop()
    //         val audioLength = play(voizy.filePath)
    //         firebaseAnalytics.logPlayVoizy(voizy.id, voizy.name)
    //         Observable.just(PlaybackInfo(PlaybackEvent.SWITCH, audioLength))
    //     } else if (!isPlaying) {
    //         val audioLength = play(voizy.filePath)
    //         // TODO play-pause remove analytics for the preview playback
    //         // maybe still move that part to the viewmodel with .doOnNext pattern...
    //         firebaseAnalytics.logPlayVoizy(voizy.id, voizy.name)
    //         Observable.just(PlaybackInfo(PlaybackEvent.START, audioLength))
    //     } else {
    //         Observable.empty()
    //     }
    // }

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

    private fun play(filePath: String): Int {
        currentTrackPath = filePath

        var durationInMillis: Int = -1

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        mediaPlayer = MediaPlayer()

        Timber.d("play-pause play $filePath")
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

    // fun playLocal(filepath: String): Int {
    //     var durationInMillis: Int = -1
    //
    //     mediaPlayer = MediaPlayer()
    //
    //     mediaPlayer?.apply {
    //         setDataSource(filepath)
    //         prepare()
    //         start()
    //         setOnCompletionListener {
    //             release()
    //             mediaPlayer = null
    //         }
    //         durationInMillis = duration
    //     }
    //     return durationInMillis
    // }
    //
    // fun playRemote(voizy: Voizy): Int {
    //     var durationInMillis: Int = -1
    //
    //     val audioAttributes = AudioAttributes.Builder()
    //         .setUsage(AudioAttributes.USAGE_MEDIA)
    //         .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
    //         .build()
    //
    //     mediaPlayer = MediaPlayer()
    //
    //     mediaPlayer?.apply {
    //         setDataSource(voizy.filePath)
    //         setAudioAttributes(audioAttributes)
    //         prepare()
    //         start()
    //         setOnCompletionListener {
    //             release()
    //             mediaPlayer = null
    //         }
    //         durationInMillis = duration
    //     }
    //     return durationInMillis
    // }
    //
    // // TODO play-pause remove duplicates
    // fun playRemote(url: String): Int {
    //     var durationInMillis: Int = -1
    //
    //     val audioAttributes = AudioAttributes.Builder()
    //         .setUsage(AudioAttributes.USAGE_MEDIA)
    //         .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
    //         .build()
    //
    //     mediaPlayer = MediaPlayer()
    //
    //     mediaPlayer?.apply {
    //         setDataSource(url)
    //         setAudioAttributes(audioAttributes)
    //         prepare()
    //         start()
    //         setOnCompletionListener {
    //             release()
    //             mediaPlayer = null
    //         }
    //         durationInMillis = duration
    //     }
    //     return durationInMillis
    // }
}