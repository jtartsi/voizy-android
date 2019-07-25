package com.voizy.android.viewmodels

import androidx.lifecycle.ViewModel
import com.voizy.android.audio.VoizyRecorder
import io.reactivex.Observable

class RecordingOverlayViewModel(
    private val voizyRecorder: VoizyRecorder
) : ViewModel() {

    public fun recordingEvents(): Observable<VoizyRecorder.RecordingEvents> {
        return voizyRecorder.recordingEvents()
    }
}
