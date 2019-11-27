package com.voizy.android.viewmodels

import android.content.Context
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.AudioPlayer
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class RecordPlayButtonViewModel(
    private val context: Context,
    private val voizyRecorder: AudioRecorder,
    private val voizyPlayer: AudioPlayer,
    private val voizyRepository: VoizyRepository
) : DisposingViewModel() {

    companion object {
        private val TAG = RecordPlayButtonViewModel::class.java.simpleName
    }

    fun startRecording() {
        val completable = Completable.fromAction {
            val filename = "${context.filesDir}/".plus(LocalFileManager.TMP_VOIZY_FILE_NAME)
            voizyRecorder.startRecording(filename)
        }.subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }

    fun stopRecording() {
        voizyRecorder.stopRecording()
    }

    fun getRecordingEvents(): Observable<AudioRecorder.RecordingEvent> {
        return voizyRecorder.getRecordingEvents()
    }

    fun startPreviewVoizyPlayback() {
        Observable.fromCallable { voizyPlayer.playLocal(voizyRepository.getTempFilePath()) }
            .withErrorHandling(TAG, "Failed to playVoizy local voizy")
            .subscribeOn(Schedulers.io())
            .subscribe()
            .autoDispose()
    }
}