package com.voizy.android.viewmodels

import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.ui.models.Voizy
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable

class RecordingViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyRecorder: VoizyRecorder
) : DisposingViewModel() {

    companion object {
        private val TAG = RecordingViewModel::class.java.simpleName
    }

    fun getRecordingEvents(): Observable<VoizyRecorder.RecordingEvent> {
        return voizyRecorder.getRecordingEvents()
            .withErrorHandling(TAG, "recordingEvents error")
    }

    fun saveVoizy(voizy: Voizy) {
        voizyRepository.saveVoizy(voizy)
    }
}
