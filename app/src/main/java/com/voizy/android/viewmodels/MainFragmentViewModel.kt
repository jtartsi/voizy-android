package com.voizy.android.viewmodels

import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.paging.PagedList
import com.google.firebase.analytics.FirebaseAnalytics
import com.voizy.android.R
import com.voizy.android.ShareBroadcastReceiver
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.ShareRepository
import com.voizy.android.middleware.repositories.VoizyRepository
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
    private val shareRepository: ShareRepository,
    private val voizyPlayer: VoizyPlayer,
    private val firebaseAnalytics: VoizyFirebaseAnalytics,
    private val compositeDisposable: CompositeDisposable
) : DisposingViewModel() {

    companion object {
        private val TAG = MainFragmentViewModel::class.java.simpleName

        private const val AUTHORITY = "com.voizy.android.fileprovider"

        private fun getFileUri(context: Context, file: File): Uri? {
            return try {
                FileProvider.getUriForFile(context, AUTHORITY, file)
            } catch (e: IllegalArgumentException) {
                Timber.e(
                    e, "File Selector. The selected file can't be shared: ${file.absolutePath}"
                )
                null
            }
        }
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
        .withErrorHandling(TAG, "failed to get voizys")
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

    fun playVoizy(voizy: Voizy): Observable<Int> {
        firebaseAnalytics.logPlayVoizy(voizy.id, voizy.name)
        return voizyRepository.getDownloadUrl(voizy.filePath)
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

    fun startVoizyShare(context: Context, voizy: Voizy, file: File) {
        val fileUri: Uri? = getFileUri(context, file)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "audio/*"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            Intent(context, ShareBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        VoizyFirebaseAnalytics(FirebaseAnalytics.getInstance(context))
            .logShareVoizyEvent(voizy.id, voizy.name)

        shareRepository.startVoizyShare(voizy)

        context.startActivity(
            Intent.createChooser(
                sendIntent,
                context.getString(R.string.share_voizy),
                pendingIntent.intentSender
            )
        )
    }

    fun downloadUrlToClipboard(context: Context, voizy: Voizy): Observable<String> {
        return voizyRepository.getDownloadUrl(voizy.filePath)
            .doOnNext {
                val clipBoard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipBoard.primaryClip = ClipData.newPlainText("voizy url", it)
            }
    }
}