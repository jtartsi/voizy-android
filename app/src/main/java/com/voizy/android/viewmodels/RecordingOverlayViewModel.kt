package com.voizy.android.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.utils.FileUtil
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
            voizyPlayer.play(FileUtil.getDefaultFileName(context))
        }.subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }

    /**
     * @newFileName only the name, no path
     */
    public fun renameCurrentVoizy(context: Context, newFileName: String): Observable<Boolean> {
        return Observable.just(newFileName)
            .map { FileUtil.renameFile(FileUtil.getDefaultFileName(context), it) }
    }
}
