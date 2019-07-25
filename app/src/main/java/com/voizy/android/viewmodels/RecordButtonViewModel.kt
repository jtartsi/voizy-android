package com.voizy.android.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.voizy.android.audio.VoizyRecorder
import java.util.Date

class RecordButtonViewModel(
    private val context: Context,
    private val voizyRecorder: VoizyRecorder
) : ViewModel() {

    public fun startRecording() {
        val filename = "${context.filesDir}/voizy_${Date().time}"
        voizyRecorder.startRecording(filename)
    }

    public fun stopRecording() {
        voizyRecorder.stopRecording()
    }
}