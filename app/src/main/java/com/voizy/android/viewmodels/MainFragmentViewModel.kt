package com.voizy.android.viewmodels

import android.content.Context
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.ui.models.Voizy
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.util.concurrent.TimeUnit

class MainFragmentViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyPlayer: VoizyPlayer,
    private val voizyFirebaseAnalytics: VoizyFirebaseAnalytics
) : DisposingViewModel() {

    companion object {
        private val TAG = MainFragmentViewModel::class.java.simpleName
    }

    private val saveVoizyEventsBehaviorSubject = BehaviorSubject.create<Pair<Boolean, Voizy?>>()

    private val searchVoizysRequest = PublishSubject.create<String>()
    private val voizysStream = searchVoizysRequest
        .debounce(500, TimeUnit.MILLISECONDS)
        .doOnNext { voizyFirebaseAnalytics.logSearch(it) }
        .observeOn(Schedulers.io())
        .flatMap { voizyRepository.searchVoizys(it) }
        .withErrorHandling(TAG, "Searching voizys failed")

    init {
        voizyRepository.getSaveVoizyEvents()
            .withErrorHandling(TAG, "save voizy events error")
            .subscribe { saveVoizyEventsBehaviorSubject.onNext(it) }
            .autoDispose()
    }

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        return saveVoizyEventsBehaviorSubject
            .withErrorHandling(TAG, "save voizy events error")
    }

    fun getVoizyStream(): Observable<List<Voizy>> {
        return voizysStream.map { it }
    }

    fun searchVoizys(searchKeyword: String = "") {
        searchVoizysRequest.onNext(searchKeyword)
    }

    fun playVoizy(voizy: Voizy): Observable<Int> {
        voizyFirebaseAnalytics.logPlayVoizy(voizy.id, voizy.name)
        return voizyRepository.getFileUrl(voizy.filePath)
            .map { voizyPlayer.playRemote(it) }
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to play Voizy ${voizy.name}")
    }

    fun downloadVoizy(context: Context, voizy: Voizy): Observable<File> {
        val destinationFile = File(LocalFileManager(context).getTempFilePath())
        return voizyRepository.downloadVoizy(voizy.filePath, destinationFile)
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to download Voizy")
    }
}