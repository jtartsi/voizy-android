package com.voizy.android.audio

import android.media.MediaRecorder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.Date

class AudioRecorder {

    private var recordStartTime: Long = 0L
    private var mediaRecorder: MediaRecorder? = null

    enum class RecordingEvent {
        START_FAILED, STARTED, STOP, STOP_FAILED, STOP_UNDER_MINIMUM_TIME, FILE_RECEIVED
    }

    private val fileReceivedEvents: PublishSubject<RecordingEvent> = PublishSubject.create()
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

    fun audioFileReceived() {
        fileReceivedEvents.onNext(RecordingEvent.FILE_RECEIVED)
    }

    fun startRecording(filename: String) {
        recordingActionRequests.onNext(filename)
    }

    fun stopRecording() {
        recordingActionRequests.onNext("")
    }

    fun getRecordingEvents(): Observable<RecordingEvent> {
        return recordingEventsStream
            .mergeWith(fileReceivedEvents)
    }

    private fun record(fileName: String): Observable<RecordingEvent> {
        return Observable.defer {
            Observable.fromCallable {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                    setOutputFile(fileName)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(96000)
                    setAudioEncodingBitRate(128000)
                    setAudioChannels(2)

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

                try {
                    mediaRecorder?.apply {
                        stop()
                        release()
                    }
                    mediaRecorder = null

                    val event = if (isOverMinimumTime()) {
                        RecordingEvent.STOP
                    } else {
                        RecordingEvent.STOP_UNDER_MINIMUM_TIME
                    }
                    event
                } catch (e: Exception) {
                    Timber.e(e, "Recording stop() failed")
                    val event = if (isOverMinimumTime()) {
                        RecordingEvent.STOP_FAILED
                    } else {
                        RecordingEvent.STOP_UNDER_MINIMUM_TIME
                    }
                    event
                }
            }
        }
    }

    private fun isOverMinimumTime(): Boolean {
        val recordingLength = Date().time - recordStartTime
        return recordingLength > 1000
    }
}