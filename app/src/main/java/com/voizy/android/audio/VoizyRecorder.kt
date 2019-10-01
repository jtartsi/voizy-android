package com.voizy.android.audio

import android.media.MediaRecorder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class VoizyRecorder {

    private var mediaRecorder: MediaRecorder? = null

    enum class RecordingEvent {
        START_FAILED, STARTED, STOP, STOP_FAILED
    }

    private val startRecordingQueue: BehaviorSubject<String> = BehaviorSubject.create()
    private val stopRecordingQueue: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private val startRecordingEvents = startRecordingQueue.share()
        .observeOn(Schedulers.io())
        .map {
            record(it)
            RecordingEvent.STARTED
        }
        .doOnNext { Timber.d("record-button-state startRecordingEvents after record()") }
        .onErrorReturn {
            Timber.e(it, "Starting recording failed")
            RecordingEvent.START_FAILED
        }

    private val stopRecordingEvents = stopRecordingQueue.share()
        .observeOn(Schedulers.io())
        .map {
            finish()
            RecordingEvent.STOP
        }
        .doOnNext { Timber.d("record-button-state startRecordingEvents after finish()") }
        .onErrorReturn { RecordingEvent.STOP_FAILED }

    fun recordingEvents(): Observable<RecordingEvent> {
        return startRecordingEvents.mergeWith(stopRecordingEvents)
    }

    fun startRecording(fileName: String) {
        Timber.d("record-button-state startRecording()")
        startRecordingQueue.onNext(fileName)
    }

    fun stopRecording() {
        Timber.d("record-button-state stopRecording()")
        stopRecordingQueue.onNext(true)
    }

    private fun record(fileName: String) {

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            prepare()
            start()
        }
    }

    private fun finish() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }
}