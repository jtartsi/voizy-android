package com.voizy.android.audio

import android.media.MediaPlayer

class VoizyPlayer {

    public fun play(filepath: String) {
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