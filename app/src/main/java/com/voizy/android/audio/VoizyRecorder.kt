package com.voizy.android.audio

import android.media.MediaRecorder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.Date

class VoizyRecorder {

    private var recordStartTime: Long = 0L
    private var mediaRecorder: MediaRecorder? = null

    enum class RecordingEvent {
        START_FAILED, STARTED, STOP, STOP_FAILED, STOP_UNDER_MINIMUM_TIME
    }

    private val recordingActionRequests: PublishSubject<String> = PublishSubject.create()
    private val recordingEventsStream: Observable<RecordingEvent> = recordingActionRequests
        .serialize()
        .switchMap { Observable.just(it) }
        .concatMap {
            if (!it.isEmpty()) {
                record(it)
            } else {
                stop()
            }
        }
        .share()

    fun startRecording(filename: String) {
        recordingActionRequests.onNext(filename)
    }

    fun stopRecording() {
        recordingActionRequests.onNext("")
    }

    fun getRecordingEvents(): Observable<RecordingEvent> {
        return recordingEventsStream
    }

    private fun record(fileName: String): Observable<RecordingEvent> {
        return Observable.defer {
            Observable.fromCallable {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                    setOutputFile(fileName)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                    prepare()
                    start()
                    recordStartTime = Date().time
                }
            }
        }
            .map { RecordingEvent.STARTED }
            .onErrorReturn { RecordingEvent.START_FAILED }
    }

    private fun stop(): Observable<RecordingEvent> {
        return Observable.defer {
            Observable.fromCallable {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                val recordingLength = Date().time - recordStartTime
                recordStartTime = 0
                recordingLength
            }
        }
            .map {
                if (it < 1000) {
                    RecordingEvent.STOP_UNDER_MINIMUM_TIME
                } else {
                    RecordingEvent.STOP
                }
            }
            .onErrorReturn { RecordingEvent.STOP_FAILED }
    }
}