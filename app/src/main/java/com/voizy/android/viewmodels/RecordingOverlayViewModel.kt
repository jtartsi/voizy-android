package com.voizy.android.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.audio.VoizyRecorder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class RecordingOverlayViewModel(
    private val voizyRecorder: VoizyRecorder,
    private val voizyPlayer: VoizyPlayer
) : ViewModel() {

    public fun recordingEvents(): Observable<VoizyRecorder.RecordingEvents> {
        return voizyRecorder.recordingEvents()
    }

    public fun playAudio(context: Context) {
        val completable = Completable.fromAction {
            // TODO recorder move this _tmp filename to some constant
            val filename = "${context.filesDir}/voizy_tmp"
            voizyPlayer.play(filename)
        }.subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }
}
