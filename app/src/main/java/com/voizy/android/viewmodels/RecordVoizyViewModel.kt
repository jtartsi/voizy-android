package com.voizy.android.viewmodels

import android.content.Context
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.local.LocalFileManager
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class RecordVoizyViewModel(
    private val context: Context,
    private val voizyRecorder: AudioRecorder,
    private val firebaseAnalytics: VoizyFirebaseAnalytics
) : DisposingViewModel() {

    val recordingEvents = voizyRecorder.getRecordingEvents()

    companion object {
        private val TAG = RecordVoizyViewModel::class.java.simpleName
    }

    init {
        recordingEvents
            .filter { it == AudioRecorder.RecordingEvent.STOP }
            .subscribe {
                firebaseAnalytics.logRecordMicrophone()
            }
    }

    fun startRecording() {
        Observable
            .defer {
                Observable.fromCallable {
                    val filename = "${context.filesDir}/".plus(LocalFileManager.TMP_VOIZY_FILE_NAME)
                    voizyRecorder.startRecording(filename)
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun stopRecording() {
        voizyRecorder.stopRecording()
    }
}