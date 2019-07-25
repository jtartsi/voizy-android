package com.voizy.android.audio

import android.media.MediaPlayer
import timber.log.Timber
import java.io.IOException

class VoizyPlayer {

    private val player = MediaPlayer()

    public fun play(filepath: String) {
        try {
            player.setDataSource(filepath)
            player.prepare()
            player.start()
        } catch (e: IOException) {
            Timber.e(e, "prepare() failed")
        }
        player.setOnCompletionListener {
            player.release()
        }
    }
}