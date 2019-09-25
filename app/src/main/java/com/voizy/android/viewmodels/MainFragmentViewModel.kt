package com.voizy.android.viewmodels

import android.content.Context
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.ui.model.Voizy
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.File

class MainFragmentViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyPlayer: VoizyPlayer
) : DisposingViewModel() {

    companion object {
        private val TAG = MainFragmentViewModel::class.java.simpleName
    }

    private val saveVoizyEventsBehaviorSubject = BehaviorSubject.create<Pair<Boolean, Voizy?>>()

    private val searchVoizysRequest = PublishSubject.create<String>()
    private val voizysStream = searchVoizysRequest
        .observeOn(Schedulers.io())
        .flatMap { voizyRepository.searchVoizys(it) }

    init {
        voizyRepository.getSaveVoizyEvents()
            .withErrorHandling(TAG, "save voizy events error")
            .subscribe { saveVoizyEventsBehaviorSubject.onNext(it) }
            .autoDispose()
    }

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        return saveVoizyEventsBehaviorSubject
            .doOnNext { searchVoizysRequest.onNext(true) }
            .withErrorHandling(TAG, "save voizy events error")
    }

    fun getVoizyStream(): Observable<List<Voizy>> {
        return voizysStream.map { it.voizys }
    }

    fun searchVoizys(searchKeyword: String = "") {
        searchVoizysRequest.onNext(searchKeyword)
    }

    // TODO v0.3.0 remove this
    fun deleteVoizy(voizy: Voizy) {
        val completable = Completable.fromAction {
            voizyRepository.deleteLocalVoizy(voizy.localFilePath!!)
        }
            .onErrorComplete()
            .subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }

    fun playVoizy(voizy: Voizy): Observable<Int> {
        return voizyRepository.getFileUrl(voizy.firebaseFilePath!!)
            .map { voizyPlayer.playRemote(it) }
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to play Voizy")
    }

    fun downloadVoizy(context: Context, voizy: Voizy): Observable<File> {
        val destinationFile = File(LocalFileManager(context).getTempFilePath())
        return voizyRepository.downloadVoizy(voizy.firebaseFilePath!!, destinationFile)
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to download Voizy")
    }
}