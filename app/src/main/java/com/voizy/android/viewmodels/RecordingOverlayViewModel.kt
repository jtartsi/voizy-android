package com.voizy.android.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.voizy.android.audio.VoizyRecorder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.Date

class RecordingOverlayViewModel(
    private val voizyRecorder: VoizyRecorder
) : ViewModel() {

    enum class RecordingEvents {
        START_FAILED, STARTED, FINISHED, CLOSE_FAILED
    }

    private val startRecordingQueue: PublishSubject<String> = PublishSubject.create()
    private val stopRecordingQueue: PublishSubject<Boolean> = PublishSubject.create()

    private val startRecordingEvents = startRecordingQueue.share()
        .map {
            voizyRecorder.startRecording(it)
            RecordingEvents.STARTED
        }
        .onErrorReturn { RecordingEvents.START_FAILED }

    private val stopRecordingEvents = stopRecordingQueue.share()
        .map {
            voizyRecorder.stopRecording()
            RecordingEvents.FINISHED
        }
        .onErrorReturn { RecordingEvents.CLOSE_FAILED }

    private val recordingEvents = startRecordingEvents.mergeWith(stopRecordingEvents)

    public fun recordingEvents(): Observable<RecordingEvents> {
        return recordingEvents
    }

    public fun startRecording(context: Context) {
        val fileName = "${context.filesDir}/voizy_${Date().time}"
        startRecordingQueue.onNext(fileName)
    }

    public fun stopRecording() {
        stopRecordingQueue.onNext(true)
    }
}
