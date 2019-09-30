package com.voizy.android.viewmodels

import android.content.Context
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.Date

class RecordPlayButtonViewModel(
    private val context: Context,
    private val voizyRecorder: VoizyRecorder,
    private val voizyPlayer: VoizyPlayer,
    private val voizyRepository: VoizyRepository
) : DisposingViewModel() {

    private var recordStartTime: Long = 0L

    companion object {
        private val TAG = RecordPlayButtonViewModel::class.java.simpleName
    }

    fun startRecording() {
        val completable = Completable.fromAction {
            val filename = "${context.filesDir}/".plus(LocalFileManager.TMP_VOIZY_FILE_NAME)
            recordStartTime = Date().time
            voizyRecorder.startRecording(filename)
        }.subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }

    fun stopRecording(): Boolean {
        val recordingLength = recordStartTime - Date().time
        recordStartTime = 0
        voizyRecorder.stopRecording()
        return recordingLength > 1000
    }

    fun getRecordingEvents(): Observable<VoizyRecorder.RecordingEvent> {
        return voizyRecorder.recordingEvents()
    }

    fun startPreviewVoizyPlayback() {
        Observable.fromCallable { voizyPlayer.playLocal(voizyRepository.getTempFilePath()) }
            .withErrorHandling(TAG, "Failed to playVoizy local voizy")
            .subscribeOn(Schedulers.io())
            .subscribe()
            .autoDispose()
    }
}