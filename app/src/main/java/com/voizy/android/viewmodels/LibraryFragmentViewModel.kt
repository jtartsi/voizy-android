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
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class LibraryFragmentViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyPlayer: AudioPlayer,
    private val firebaseAnalytics: VoizyFirebaseAnalytics,
    private val compositeDisposable: CompositeDisposable,
    private val shareManager: ShareManager
) : DisposingViewModel() {

    companion object {
        private val TAG = LibraryFragmentViewModel::class.java.simpleName
    }

    private val saveVoizyEventsBehaviorSubject = BehaviorSubject.create<Pair<Boolean, Voizy?>>()

    private val searchKeyword = PublishSubject.create<String>()

    private val voizyResults = searchKeyword
        .debounce(500, TimeUnit.MILLISECONDS)
        .doOnNext { firebaseAnalytics.logSearch(it) }
        .map { voizyRepository.voizys(it) }
        .share()

    val voizys: Observable<PagedList<Voizy>> = voizyResults
        .flatMap { it.pagedListObservable }
        .doOnNext {
            it.addWeakCallback(null, object : PagedList.Callback() {
                override fun onChanged(position: Int, count: Int) {
                    Timber.d("results-state onChanged() position $position, count $count")
                }

                override fun onInserted(position: Int, count: Int) {
                    Timber.d("results-state onInserted() position $position, count $count")
                }

                override fun onRemoved(position: Int, count: Int) {
                    Timber.d("results-state onRemoved() position $position, count $count")
                }
            })
        }
        .withErrorHandling(TAG, "failed to get voizys")

    val resultsState: Observable<Boolean> = voizys.map {
        Timber.d("results-state $it")
        Timber.d("results-state size ${it.size}")
        Timber.d("results-state snapshot ${it.snapshot()}")
        Timber.d("results-state snapshot.size ${it.snapshot().size}")
        Timber.d("results-state snapshot.loadedCount ${it.loadedCount}")
        Timber.d("results-state snapshot.positionOffSet ${it.positionOffset}")
        !it.isEmpty()
    }

    val networkState: Observable<NetworkState> = voizyResults
        .flatMap { it.networkSate }
        .withErrorHandling(TAG, "failed to get networkState")

    val initialLoading: Observable<NetworkState> = voizyResults
        .flatMap { it.initialLoading }
        .withErrorHandling(TAG, "failed to get initialLoading state")

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

    fun getPlayEvents(): Observable<PlaybackInfo> {
        return voizyPlayer.getPlaybackEvents()
    }

    fun togglePlay(voizy: Voizy): Observable<PlaybackInfo> {
        return voizyRepository.getDownloadUrl(voizy.filePath)
            .flatMap {
                voizyPlayer.togglePlay(it)
            }
            .doOnNext { handlePlayAnalytics(voizy) }
            .withErrorHandling(TAG, "Failed to toggle play Voizy ${voizy.name}")
    }

    fun downloadVoizy(context: Context, voizy: Voizy): Observable<Pair<Voizy, File>> {
        val destinationFile = File(LocalFileManager(context).getTempFilePath())
        return voizyRepository
            .downloadVoizy(voizy.filePath, destinationFile)
            .map { Pair(voizy, it) }
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to download Voizy")
    }

    fun startVoizyShare(context: Context, voizy: Voizy, file: File) {
        shareManager.startVoizyShare(context, voizy, file)
    }

    fun downloadUrlToClipboard(context: Context, voizy: Voizy): Observable<String> {
        return voizyRepository.getDownloadUrl(voizy.filePath)
            .subscribeOn(Schedulers.io())
            .doOnNext {
                val clipBoard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipBoard.primaryClip = ClipData.newPlainText("voizy url", it)
            }
            .withErrorHandling(TAG, "Failed to copy download url")
    }

    private fun handlePlayAnalytics(voizy: Voizy): Consumer<PlaybackInfo> {
        return Consumer {
            if (it.playbackEvent == PlaybackEvent.START ||
                it.playbackEvent == PlaybackEvent.SWITCH
            ) {
                firebaseAnalytics.logPlayVoizy(voizy.id, voizy.name)
            }
        }
    }
}