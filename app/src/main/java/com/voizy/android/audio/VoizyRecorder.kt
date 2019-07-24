package com.voizy.android.audio

import android.content.Context
import android.media.MediaRecorder
import timber.log.Timber
import java.io.IOException
import java.util.Date

class VoizyRecorder(val context: Context) {

    private var mediaRecorder: MediaRecorder? = null

    public fun startRecording() {

        val filename = "${context.filesDir}/voizy_${Date().time}"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(filename)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Timber.e(e, "prepare() failed")
            }
            start()
        }
    }

    public fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }
}