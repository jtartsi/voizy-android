package com.voizy.android.viewmodels

import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.model.Voizy
import com.voizy.android.repositories.FileRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject

class MainFragmentViewModel(
    private val fileRepository: FileRepository,
    private val voizyPlayer: VoizyPlayer
) : ViewModel() {

    private enum class VoizyLocation { PRIVATE, PUBLIC }

    private val startPlaybackQueue = PublishSubject.create<String>()
    private val startPlaybackEvents = startPlaybackQueue
        .observeOn(Schedulers.io())
        .map { voizyPlayer.play(it) }

    private val voizySearchRequest = ReplaySubject.create<VoizyLocation>()
    private val voizysStream = voizySearchRequest
        .observeOn(Schedulers.io())
        .map {
            when (it) {
                VoizyLocation.PUBLIC -> fileRepository.getReceivedVoizys()
                VoizyLocation.PRIVATE -> fileRepository.getAllOwnVoizys()
            }
        }

    fun getSaveVoizyEvents(): Observable<Voizy> {
        return fileRepository.getSaveVoizyEvents()
    }

    fun getVoizyStream(): Observable<List<Voizy>> {
        return voizysStream
    }

    fun getVoizyPlaybackEvents(): Observable<Int> {
        return startPlaybackEvents
    }

    fun getReceivedVoizys() {
        voizySearchRequest.onNext(VoizyLocation.PUBLIC)
    }

    fun getOwnVoizys() {
        voizySearchRequest.onNext(VoizyLocation.PRIVATE)
    }

    fun deleteVoizy(voizy: Voizy) {
        val completable = Completable.fromAction {
            fileRepository.deleteFile(voizy.filePath)
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

    // fun playVoizy(filePath: String) {
    //     val completable = Completable.fromAction {
    //         voizyPlayer.play(filePath)
    //     }.subscribeOn(Schedulers.io())
    //
    //     completable.apply {
    //         autoDisposable(this)
    //         subscribe()
    //     }
    // }
}