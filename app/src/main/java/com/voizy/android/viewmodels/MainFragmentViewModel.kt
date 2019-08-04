package com.voizy.android.viewmodels

import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.ui.model.Voizy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject

class MainFragmentViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyPlayer: VoizyPlayer
) : ViewModel() {

    private val startPlaybackQueue = PublishSubject.create<String>()
    private val startPlaybackEvents = startPlaybackQueue
        .observeOn(Schedulers.io())
        .map { voizyPlayer.play(it) }

    private val voizySearchRequest = ReplaySubject.create<Boolean>()
    private val voizysStream = voizySearchRequest
        .observeOn(Schedulers.io())
        .map { voizyRepository.getAllOwnVoizys() }

    fun getSaveVoizyEvents(): Observable<Voizy> {
        return voizyRepository.getSaveVoizyEvents()
    }

    fun getVoizyStream(): Observable<List<Voizy>> {
        return voizysStream
    }

    fun getVoizyPlaybackEvents(): Observable<Int> {
        return startPlaybackEvents
    }

    fun getLocalVoizys() {
        voizySearchRequest.onNext(true)
    }

    fun deleteVoizy(voizy: Voizy) {
        val completable = Completable.fromAction {
            voizyRepository.deleteLocalVoizy(voizy.localPath)
        }.subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }

    fun playVoizy(filePath: String): Observable<Int> {
        return Observable.just(filePath)
            .map { voizyPlayer.play(it) }
    }

    fun fetchRemoteVoizys() {
        voizyRepository.getVoizys()
    }
}