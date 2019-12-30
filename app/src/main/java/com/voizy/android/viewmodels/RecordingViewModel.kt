package com.voizy.android.viewmodels

import com.voizy.android.audio.AudioRecorder
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable

class RecordingViewModel(
    private val voizyRecorder: AudioRecorder
) : DisposingViewModel() {

    companion object {
        private val TAG = RecordingViewModel::class.java.simpleName
    }

    fun getRecordingEvents(): Observable<AudioRecorder.RecordingEvent> {
        return voizyRecorder.getRecordingEvents()
            .withErrorHandling(TAG, "recordingEvents error")
    }
}
