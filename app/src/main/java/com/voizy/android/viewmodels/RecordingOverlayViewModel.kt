package com.voizy.android.viewmodels

import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.model.Voizy
import com.voizy.android.repositories.FileRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class RecordingOverlayViewModel(
    private val fileRepository: FileRepository,
    private val voizyRecorder: VoizyRecorder,
    private val voizyPlayer: VoizyPlayer
) : ViewModel() {

    public fun recordingEvents(): Observable<VoizyRecorder.RecordingEvents> {
        return voizyRecorder.recordingEvents()
    }

    public fun saveVoizyEvents(): Observable<Voizy> {
        return fileRepository.getSaveVoizyEvents()
    }

    public fun playAudio() {
        val completable = Completable.fromAction {
            voizyPlayer.play(fileRepository.getTempFilePath())
        }.subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }

    public fun saveVoizy(newFileName: String) {
        Timber.d("save-voizy-iss saveVoizy() $newFileName")
        fileRepository.renameVoizy(newFileName)
    }

    // /**
    //  * @newFileName only the name, no path
    //  */
    // public fun renameCurrentVoizy(context: Context, newFileName: String): Observable<Boolean> {
    //     return Observable.just(newFileName)
    //         .map { FileUtil.renameFile(FileUtil.getDefaultFileName(context), it) }
    // }
}
