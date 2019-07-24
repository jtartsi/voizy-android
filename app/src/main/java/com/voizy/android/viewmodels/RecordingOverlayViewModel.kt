package com.voizy.android.viewmodels

import androidx.lifecycle.ViewModel
import com.voizy.android.audio.VoizyRecorder
import io.reactivex.Observable

class RecordingOverlayViewModel(voizyRecorder: VoizyRecorder) : ViewModel() {

    public fun errorStream(): Observable<String> {
        return Observable.just("error")
    }

    public fun recordingFinished(): Observable<String> {
        return Observable.just("filepath")
    }
}
