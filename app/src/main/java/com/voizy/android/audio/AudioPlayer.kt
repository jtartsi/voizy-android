package com.voizy.android.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.models.Voizy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AudioPlayer(private val firebaseAnalytics: VoizyFirebaseAnalytics) {

    val isPlaying: Boolean
        get() = mediaPlayer != null && mediaPlayer!!.isPlaying

    private val playbackEvents: PublishSubject<PlaybackInfo> = PublishSubject.create()
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackPath: String = ""

    fun getPlaybackEvents(): Observable<PlaybackInfo> {
        return playbackEvents
    }

    fun togglePlay(voizy: Voizy) {
        if (isPlaying) {
            val audioLength = stop()
            playbackEvents.onNext(PlaybackInfo(PlaybackEvent.START, audioLength))
        }
        if (currentTrackPath != voizy.filePath) {
            val audioLength = play(voizy.filePath)
            firebaseAnalytics.logPlayVoizy(voizy.id, voizy.name)
            playbackEvents.onNext(PlaybackInfo(PlaybackEvent.START, audioLength))
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