package com.voizy.android.audio

import android.media.AudioAttributes
import android.media.MediaPlayer

class AudioPlayer {

    fun playLocal(filepath: String): Int {
        var durationInMillis: Int = -1

        MediaPlayer().apply {
            setDataSource(filepath)
            prepare()
            start()
            setOnCompletionListener {
                it.release()
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

        MediaPlayer().apply {
            setDataSource(url)
            setAudioAttributes(audioAttributes)
            prepare()
            start()
            setOnCompletionListener {
                it.release()
            }
            durationInMillis = duration
        }
        return durationInMillis
    }
}