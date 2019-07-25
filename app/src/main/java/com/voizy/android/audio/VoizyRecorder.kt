package com.voizy.android.audio

import android.media.MediaRecorder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class VoizyRecorder {

    private var mediaRecorder: MediaRecorder? = null

    enum class RecordingEvents {
        START_FAILED, STARTED, FINISHED, CLOSE_FAILED
    }

    private val startRecordingQueue: PublishSubject<String> = PublishSubject.create()
    private val stopRecordingQueue: PublishSubject<Boolean> = PublishSubject.create()

    private val startRecordingEvents = startRecordingQueue.share()
        .observeOn(Schedulers.io())
        .map {
            Timber.d("recording voizy $it")
            record(it)
            RecordingEvents.STARTED
        }
        .onErrorReturn {
            Timber.e(it, "Starting recording failed")
            RecordingEvents.START_FAILED
        }

    private val stopRecordingEvents = stopRecordingQueue.share()
        .observeOn(Schedulers.io())
        .map {
            finish()
            RecordingEvents.FINISHED
        }
        .onErrorReturn { RecordingEvents.CLOSE_FAILED }

    private val recordingEvents = startRecordingEvents.mergeWith(stopRecordingEvents)

    fun recordingEvents(): Observable<RecordingEvents> {
        return recordingEvents
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
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

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