package com.voizy.android.viewmodels

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.paging.PagedList
import com.voizy.android.audio.AudioPlayer
import com.voizy.android.audio.PlaybackEvent
import com.voizy.android.audio.PlaybackInfo
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.utils.NetworkState
import com.voizy.android.utils.ShareManager
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

class LibraryViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyPlayer: AudioPlayer,
    private val firebaseAnalytics: VoizyFirebaseAnalytics,
    private val compositeDisposable: CompositeDisposable,
    private val shareManager: ShareManager
) : DisposingViewModel() {

    companion object {
        private val TAG = LibraryViewModel::class.java.simpleName
    }

    private val searchKeyword = PublishSubject.create<String>()

    private val voizyResults = searchKeyword
        .debounce(500, TimeUnit.MILLISECONDS)
        .map { it.toLowerCase() }
        .doOnNext { handleSearchAnalytics(it) }
        .map { voizyRepository.voizys(it) }
        .share()

    val voizys: Observable<PagedList<Voizy>> = voizyResults
        .flatMap { it.pagedListObservable }
        .share()
        .withErrorHandling(TAG, "failed to get voizys")

    val networkState: Observable<NetworkState> = voizyResults
        .flatMap { it.networkSate }
        .share()
        .withErrorHandling(TAG, "results failed to get networkState")

    // Indicates when loading first dataset for each search keyword
    val initialLoading: Observable<NetworkState> = voizyResults
        .flatMap { it.initialLoading }
        .share()
        .withErrorHandling(TAG, "failed to get initialLoading state")

    val playbackEvents = voizyPlayer.playbackEventStream

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun loadVoizys(searchParam: String = "") {
        searchKeyword.onNext(searchParam)
    }

    fun togglePlay(voizy: Voizy): Observable<PlaybackInfo> {
        Timber.d("cdn-url togglePlay() ${voizy.playbackUrl}")
        return voizyPlayer.togglePlay(voizy.playbackUrl)
            .doOnNext { handlePlayAnalytics(voizy, it) }
            .withErrorHandling(TAG, "Failed to toggle startPlayback Voizy ${voizy.name}")
    }

    fun releasePlayer() {
        voizyPlayer.stopPlayback()
    }

    fun downloadVoizy(context: Context, voizy: Voizy): Observable<Pair<Voizy, File>> {
        val destinationFile = File(LocalFileManager(context).getTempFilePath())
        return voizyRepository
            .downloadVoizy(voizy.remoteUrl, destinationFile)
            .map { Pair(voizy, it) }
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to downloadVideo Voizy")
    }

    fun startVoizyShare(context: Context, voizy: Voizy, file: File) {
        shareManager.startVoizyShare(context, voizy, file)
    }

    fun downloadUrlToClipboard(context: Context, voizy: Voizy): Observable<String> {
        return voizyRepository.getDownloadUrl(voizy.remoteUrl)
            .subscribeOn(Schedulers.io())
            .doOnNext {
                val clipBoard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipBoard.primaryClip = ClipData.newPlainText("voizy url", it)
            }
            .withErrorHandling(TAG, "Failed to copy downloadVideo url")
    }

    private fun handlePlayAnalytics(voizy: Voizy, playbackInfo: PlaybackInfo) {
        if (playbackInfo.playbackEvent == PlaybackEvent.START ||
            playbackInfo.playbackEvent == PlaybackEvent.SWITCH
        ) {
            firebaseAnalytics.logPlayVoizy(voizy.id, voizy.name)
        }
    }

    private fun handleSearchAnalytics(searchKeyword: String) {
        if (!searchKeyword.isNullOrEmpty()) {
            firebaseAnalytics.logSearch(searchKeyword)
        }
    }
}