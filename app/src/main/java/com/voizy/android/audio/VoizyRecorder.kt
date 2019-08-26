package com.voizy.android.audio

import android.media.MediaRecorder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class VoizyRecorder {

    private var mediaRecorder: MediaRecorder? = null

    enum class RecordingEvent {
        START_FAILED, STARTED, FINISHED, CLOSE_FAILED
    }

    private val startRecordingQueue: BehaviorSubject<String> = BehaviorSubject.create()
    private val stopRecordingQueue: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private val startRecordingEvents = startRecordingQueue.share()
        .observeOn(Schedulers.io())
        .map {
            record(it)
            RecordingEvent.STARTED
        }
        .onErrorReturn {
            Timber.e(it, "Starting recording failed")
            RecordingEvent.START_FAILED
        }

    private val stopRecordingEvents = stopRecordingQueue.share()
        .observeOn(Schedulers.io())
        .map {
            finish()
            RecordingEvent.FINISHED
        }
        .onErrorReturn { RecordingEvent.CLOSE_FAILED }

    fun recordingEvents(): Observable<RecordingEvent> {
        return startRecordingEvents.mergeWith(stopRecordingEvents)
    }

    fun startRecording(fileName: String) {
        startRecordingQueue.onNext(fileName)
    }

    fun stopRecording() {
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