package com.voizy.android.audio

import android.media.MediaPlayer

class VoizyPlayer {

    fun play(filepath: String): Int {
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
}