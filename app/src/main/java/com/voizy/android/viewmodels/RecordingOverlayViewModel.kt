package com.voizy.android.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.repositories.FileRepository
import com.voizy.android.utils.FileUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class RecordingOverlayViewModel(
    private val context: Context,
    private val fileRepository: FileRepository,
    private val voizyRecorder: VoizyRecorder,
    private val voizyPlayer: VoizyPlayer
) : ViewModel() {

    private val renameSubject = PublishSubject.create<String>()
    private val saveNameEvents = renameSubject
        .observeOn(Schedulers.io())
        .map { fileRepository.renameFile(it) }

    public fun recordingEvents(): Observable<VoizyRecorder.RecordingEvents> {
        return voizyRecorder.recordingEvents()
    }

    public fun saveNameEvents(): Observable<Boolean> {
        return saveNameEvents
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

    public fun renameVoizy(newFileName: String) {
        renameSubject.onNext(newFileName)
    }

    // /**
    //  * @newFileName only the name, no path
    //  */
    // public fun renameCurrentVoizy(context: Context, newFileName: String): Observable<Boolean> {
    //     return Observable.just(newFileName)
    //         .map { FileUtil.renameFile(FileUtil.getDefaultFileName(context), it) }
    // }
}
