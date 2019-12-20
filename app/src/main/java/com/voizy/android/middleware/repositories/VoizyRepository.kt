package com.voizy.android.middleware.repositories

import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.collections.VoizySearchRequestCollection
import com.voizy.android.middleware.firebase.collections.VoizysCollection
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.File

class VoizyRepository(
    private val voizysCollection: VoizysCollection,
    private val compositeDisposable: CompositeDisposable,
    private val voizySearchRequestCollection: VoizySearchRequestCollection,
    private val localFileManager: LocalFileManager,
    private val voizyStorage: VoizyFirebaseStorage
) {

    companion object {

        private val TAG = VoizyRepository::class.java.simpleName
        private const val PAGE_SIZE: Int = 25
        private const val INITIAL_LOADING = PAGE_SIZE * 2

        private val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(INITIAL_LOADING)
            .setPageSize(PAGE_SIZE)
            .build()
    }

    private val lastSavedVoizy = BehaviorSubject.create<Voizy>()
    private val saveVoizyQueue = PublishSubject.create<Voizy>()

    init {
        saveVoizyQueue
            .observeOn(Schedulers.io())
            .switchMap { localFileManager.saveVoizy(it) }
            .map { updateDuration(it) }
            .doOnNext { lastSavedVoizy.onNext(it) }
            .switchMap { voizyStorage.uploadVoizy(it) }
            .switchMap { voizysCollection.saveVoizyToCloud(it.second) }
            .withErrorHandling(TAG, "saveVoizyEvents error")
            .subscribe()
    }

    /**
     * Returns last voizy that has been queued up for saving. Voizy might be already saved, or
     * uploading might still be in process
     */
    fun lastVoizyToBeSaved(): Observable<Voizy> {
        return lastSavedVoizy
    }

    fun getTempFilePath(): String {
        return localFileManager.getTempFilePath()
    }

    fun saveVoizy(voizy: Voizy) {
        saveVoizyQueue.onNext(voizy)
    }

    fun voizys(searchKeyword: String): Listing<Voizy> {
        val sourceFactory = VoizyDataSourceFactory(
            searchKeyword, compositeDisposable, voizySearchRequestCollection
        )

        val voizysPagedList = RxPagedListBuilder(
            sourceFactory, pagedListConfig
        )
            .setFetchScheduler(Schedulers.io())
            .setNotifyScheduler(AndroidSchedulers.mainThread())
            .buildObservable()

        val networkState = sourceFactory
            .dataSource.flatMap { it.networkState }

        val initialLoading = sourceFactory
            .dataSource.flatMap { it.initialLoading }

        return Listing(voizysPagedList, networkState, initialLoading)
    }

    fun getDownloadUrl(firestorePath: String): Observable<String> {
        return voizyStorage.getDownloadUri(firestorePath)
            .map { it.toString() }
    }

    fun downloadVoizy(firestorePath: String, destinationFile: File): Observable<File> {
        return voizyStorage.getFile(firestorePath, destinationFile)
            .map { destinationFile }
    }

    private fun updateDuration(voizy: Voizy): Voizy {
        voizy.duration = localFileManager.getAudioDurationInMillis(voizy.localPath)
        return voizy
    }
}