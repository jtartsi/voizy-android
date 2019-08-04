package com.voizy.android.middleware.viewmodels

import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.middleware.model.Voizy
import com.voizy.android.middleware.repositories.LocalFileManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class RecordingOverlayViewModel(
    private val fileRepository: LocalFileManager,
    private val voizyRecorder: VoizyRecorder,
    private val voizyPlayer: VoizyPlayer
) : ViewModel() {

    fun recordingEvents(): Observable<VoizyRecorder.RecordingEvents> {
        return voizyRecorder.recordingEvents()
    }

    fun saveVoizyEvents(): Observable<Voizy> {
        return fileRepository.getSaveVoizyEvents()
    }

    fun playVoizy() {
        val completable = Completable.fromAction {
            voizyPlayer.play(fileRepository.getTempFilePath())
        }.subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }

    fun saveVoizy(newFileName: String) {
        fileRepository.renameVoizy(newFileName)
    }
}
