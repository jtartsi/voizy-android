package com.voizy.android.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyRecorder
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class RecordButtonViewModel(
    private val context: Context,
    private val voizyRecorder: VoizyRecorder
) : ViewModel() {

    public fun startRecording() {

        val completable = Completable.fromAction {
            val filename = "${context.filesDir}/voizy_tmp.mp3"
            voizyRecorder.startRecording(filename)
        }.subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }

    public fun stopRecording() {
        voizyRecorder.stopRecording()
    }
}