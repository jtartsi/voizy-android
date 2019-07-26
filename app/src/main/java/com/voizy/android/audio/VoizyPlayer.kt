package com.voizy.android.audio

import android.media.MediaPlayer
import timber.log.Timber

class VoizyPlayer {

    public fun play(filepath: String) {
        Timber.d("play-iss $filepath")
        MediaPlayer().apply {
            setDataSource(filepath)
            prepare()
            start()
            setOnCompletionListener {
                it.release()
            }
        }
    }
}