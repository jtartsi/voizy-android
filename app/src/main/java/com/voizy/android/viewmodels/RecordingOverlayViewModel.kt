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

    private val startRecordingQueue: PublishSubject<Long> = PublishSubject.create()
    private val stopRecordingQueue: PublishSubject<Boolean> = PublishSubject.create()

    private val startRecordingEvents = startRecordingQueue.share()
        .observeOn(Schedulers.io())
        .map { "${context.filesDir}/voizy_$it" }
        .map {
            Timber.d("recording voizy $it")
            voizyRecorder.startRecording(it)
            RecordingEvents.STARTED
        }
        .onErrorReturn {
            Timber.e(it, "Starting recording failed")
            RecordingEvents.START_FAILED
        }

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
        startRecordingQueue.onNext(Date().time)
    }

    public fun stopRecording() {
        stopRecordingQueue.onNext(true)
    }
}
