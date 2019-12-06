package com.voizy.android.middleware.repositories

import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.collections.ShareCollection
import com.voizy.android.middleware.firebase.collections.VoizySearchRequestCollection
import com.voizy.android.middleware.firebase.collections.VoizysCollection
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.local.LocalFileManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File

class VoizyRepository(
    private val voizysCollection: VoizysCollection,
    private val shareCollection: ShareCollection,
    private val compositeDisposable: CompositeDisposable,
    private val voizySearchRequestCollection: VoizySearchRequestCollection,
    private val localFileManager: LocalFileManager,
    private val voizyStorage: VoizyFirebaseStorage
) {

    companion object {

        private val TAG = VoizyRepository::class.java.simpleName
        private val PAGE_SIZE: Int = 25
        private val INITIAL_LOADING = PAGE_SIZE * 2

        private val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(INITIAL_LOADING)
            .setPageSize(PAGE_SIZE)
            .build()
    }

    private val saveVoizyQueue = PublishSubject.create<Voizy>()
    private val saveVoizyEvents = saveVoizyQueue
        .observeOn(Schedulers.io())
        .switchMap { localFileManager.saveVoizy(it) }
        .switchMap { voizyStorage.uploadVoizy(it) }
        .switchMap { voizysCollection.saveVoizy(it.second) }
        // .switchMap { localFileManager.deleteFile(it.) } TODO(temp-file fix this)
        .share()

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        return saveVoizyEvents
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
}