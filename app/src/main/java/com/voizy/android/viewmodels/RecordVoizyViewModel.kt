package com.voizy.android.viewmodels

import android.content.Context
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.local.LocalFileManager
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

class RecordVoizyViewModel(
    private val context: Context,
    private val voizyRecorder: AudioRecorder,
    private val firebaseAnalytics: VoizyFirebaseAnalytics
) : DisposingViewModel() {

    val recordingEvents: Observable<AudioRecorder.RecordingEvent> =
        voizyRecorder.getRecordingEvents().share()

    val recording = AtomicBoolean()

    companion object {
        private val TAG = RecordVoizyViewModel::class.java.simpleName
    }

    init {
        recording.set(false)

        recordingEvents
            .filter { it == AudioRecorder.RecordingEvent.STOP }
            .subscribe { firebaseAnalytics.logRecordMicrophone() }
            .autoDispose()
    }

    fun startRecording() {
        Observable
            .defer {
                Observable.fromCallable {
                    recording.set(true)
                    val filename = "${context.filesDir}/".plus(LocalFileManager.TMP_VOIZY_FILE_NAME)
                    voizyRecorder.startRecording(filename)
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun stopRecording() {
        recording.set(false)
        voizyRecorder.stopRecording()
    }
}