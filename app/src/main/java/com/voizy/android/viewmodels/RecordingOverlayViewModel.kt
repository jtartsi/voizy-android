package com.voizy.android.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.voizy.android.audio.VoizyRecorder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.Date

class RecordingOverlayViewModel(
    private val context: Context,
    private val voizyRecorder: VoizyRecorder
) : ViewModel() {

    enum class RecordingEvents {
        START_FAILED, STARTED, FINISHED, CLOSE_FAILED
    }

    private val startRecordingQueue: PublishSubject<String> = PublishSubject.create()
    private val stopRecordingQueue: PublishSubject<Boolean> = PublishSubject.create()

    private val startRecordingEvents = startRecordingQueue.share()
        .observeOn(Schedulers.io())
        .map { buildString { context.filesDir }.plus(it) }
        .map {
            Timber.d("starting recording file to store $it")
            voizyRecorder.startRecording(it)
            RecordingEvents.STARTED
        }
        .onErrorReturn { RecordingEvents.START_FAILED }

    private val stopRecordingEvents = stopRecordingQueue.share()
        .observeOn(Schedulers.io())
        .map {
            voizyRecorder.stopRecording()
            RecordingEvents.FINISHED
        }
        .onErrorReturn { RecordingEvents.CLOSE_FAILED }

    private val recordingEvents = startRecordingEvents.mergeWith(stopRecordingEvents)

    public fun recordingEvents(): Observable<RecordingEvents> {
        return recordingEvents
    }

    public fun startRecording() {
        val fileName = "/voizy_${Date().time}"
        startRecordingQueue.onNext(fileName)
    }

    public fun stopRecording() {
        stopRecordingQueue.onNext(true)
    }
}
