package com.voizy.android.audio

import android.media.MediaRecorder

class VoizyRecorder {

    private var mediaRecorder: MediaRecorder? = null

    public fun startRecording(fileName: String) {

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            prepare()
            // try {
            //     prepare()
            // } catch (e: IOException) {
            //     Timber.e(e, "prepare() failed")
            // }
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