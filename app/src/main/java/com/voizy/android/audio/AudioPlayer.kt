package com.voizy.android.audio

import android.media.AudioAttributes
import android.media.MediaPlayer

class AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null

    val isPlaying: Boolean
        get() = mediaPlayer != null && mediaPlayer!!.isPlaying

    fun playLocal(filepath: String): Int {
        var durationInMillis: Int = -1

        mediaPlayer = MediaPlayer()

        mediaPlayer?.apply {
            setDataSource(filepath)
            prepare()
            start()
            setOnCompletionListener {
                release()
                mediaPlayer = null
            }
            durationInMillis = duration
        }
        return durationInMillis
    }

    fun playRemote(url: String): Int {
        var durationInMillis: Int = -1

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        mediaPlayer = MediaPlayer()

        mediaPlayer?.apply {
            setDataSource(url)
            setAudioAttributes(audioAttributes)
            prepare()
            start()
            setOnCompletionListener {
                release()
                mediaPlayer = null
            }
            durationInMillis = duration
        }
        return durationInMillis
    }

    fun stop(): Int {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
                release()
                mediaPlayer = null
            }
        }
        return 0
    }
}