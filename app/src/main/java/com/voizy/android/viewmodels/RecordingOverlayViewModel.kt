package com.voizy.android.viewmodels

import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.ui.model.Voizy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class RecordingOverlayViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyRecorder: VoizyRecorder,
    private val voizyPlayer: VoizyPlayer
) : ViewModel() {

    fun recordingEvents(): Observable<VoizyRecorder.RecordingEvents> {
        return voizyRecorder.recordingEvents()
    }

    fun saveVoizyEvents(): Observable<Voizy> {
        return voizyRepository.getSaveVoizyEvents()
    }

    fun playVoizy() {
        val completable = Completable.fromAction {
            voizyPlayer.play(voizyRepository.getTempFilePath())
        }.subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }

    fun oldSaveVoizy(voizy: Voizy) {
        voizyRepository.saveVoizy(voizy)
    }

    private val subject = PublishSubject.create<Boolean>()

    fun saveVoizy(voizy: Voizy): Observable<Boolean> {
        Observable.just(true)
            .subscribe(subject)
        return subject
    }
}
