package com.voizy.android.audio

import android.media.MediaPlayer
import timber.log.Timber
import java.io.IOException

class VoizyPlayer {

    private val player = MediaPlayer()

    public fun prepare(filepath: String) {
        try {
            player.setDataSource(filepath)
            player.prepare()
        } catch (e: IOException) {
            Timber.e(e, "prepare() failed")
        }
    }

    public fun play() {
        player.start()
    }

    public fun release() {
        player.release()
    }
}