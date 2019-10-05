package com.voizy.android.middleware.repositories

import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.collections.VoizySearchRequestCollection
import com.voizy.android.middleware.firebase.collections.VoizysCollection
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.ui.models.Voizy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File

class VoizyRepository(
    private val voizys: VoizysCollection,
    private val compositeDisposable: CompositeDisposable,
    private val voizySearchRequestCollection: VoizySearchRequestCollection,
    private val localFileManager: LocalFileManager,
    private val voizyStorage: VoizyFirebaseStorage
) {

    companion object {

        private val TAG = VoizyRepository::class.java.simpleName

        private const val INITIAL_PAGE_SIZE: Int = 10
        private val PAGE_SIZE: Int = 2

        private val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(INITIAL_PAGE_SIZE)
            .setPageSize(PAGE_SIZE)
            .build()
    }

    private val saveVoizyQueue = PublishSubject.create<Voizy>()
    private val saveVoizyEvents = saveVoizyQueue
        .observeOn(Schedulers.io())
        .switchMap { localFileManager.saveVoizy(it) }
        .switchMap { voizyStorage.uploadVoizy(it) }
        .switchMap { voizys.saveVoizy(it.second) }
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

        val networkStateObservable = sourceFactory
            .dataSource.flatMap { it.networkState }

        return Listing(voizysPagedList, networkStateObservable)
    }

    fun getFileUrl(firestorePath: String): Observable<String> {
        return voizyStorage.getDownloadUri(firestorePath)
            .map { it.toString() }
    }

    fun downloadVoizy(firestorePath: String, destinationFile: File): Observable<File> {
        return voizyStorage.getFile(firestorePath, destinationFile)
            .map { destinationFile }
    }
}