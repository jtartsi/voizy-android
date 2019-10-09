package com.voizy.android.viewmodels

import android.content.Context
import androidx.paging.PagedList
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.ui.models.Voizy
import com.voizy.android.utils.NetworkState
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

class MainFragmentViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyPlayer: VoizyPlayer,
    private val firebaseAnalytics: VoizyFirebaseAnalytics,
    private val compositeDisposable: CompositeDisposable
) : DisposingViewModel() {

    companion object {
        private val TAG = MainFragmentViewModel::class.java.simpleName
    }

    private val saveVoizyEventsBehaviorSubject = BehaviorSubject.create<Pair<Boolean, Voizy?>>()

    private val searchKeyword = PublishSubject.create<String>()

    private val voizyResults = searchKeyword
        .doOnNext { Timber.d("search before debounce") }
        .debounce(500, TimeUnit.MILLISECONDS)
        .doOnNext { Timber.d("search before debounce") }
        .doOnNext { firebaseAnalytics.logSearch(it) }
        .doOnNext { Timber.d("search after analytics") }
        .map { voizyRepository.voizys(it) }
        .share()

    val voizys: Observable<PagedList<Voizy>> = voizyResults
        .flatMap { it.pagedListObservable }
    val networkState: Observable<NetworkState> = voizyResults
        .flatMap { it.networkSate }
    val initialLoading: Observable<NetworkState> = voizyResults
        .flatMap { it.initialLoading }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    init {
        voizyRepository.getSaveVoizyEvents()
            .withErrorHandling(TAG, "save voizy events error")
            .subscribe { saveVoizyEventsBehaviorSubject.onNext(it) }
            .autoDispose()
    }

    fun loadVoizys(searchParam: String = "") {
        searchKeyword.onNext(searchParam)
    }

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        return saveVoizyEventsBehaviorSubject
            .withErrorHandling(TAG, "save voizy events error")
    }

    fun playVoizy(voizy: Voizy): Observable<Int> {
        firebaseAnalytics.logPlayVoizy(voizy.id, voizy.name)
        return voizyRepository.getFileUrl(voizy.filePath)
            .map { voizyPlayer.playRemote(it) }
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to play Voizy ${voizy.name}")
    }

    fun downloadVoizy(context: Context, voizy: Voizy): Observable<Pair<Voizy, File>> {
        val destinationFile = File(LocalFileManager(context).getTempFilePath())
        return voizyRepository
            .downloadVoizy(voizy.filePath, destinationFile)
            .map { Pair(voizy, it) }
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to download Voizy")
    }
}