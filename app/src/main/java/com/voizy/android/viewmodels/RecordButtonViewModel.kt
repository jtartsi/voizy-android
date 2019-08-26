package com.voizy.android.viewmodels

import android.content.Context
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.middleware.local.LocalFileManager
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class RecordButtonViewModel(
    private val context: Context,
    private val voizyRecorder: VoizyRecorder
) : DisposingViewModel() {

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
}