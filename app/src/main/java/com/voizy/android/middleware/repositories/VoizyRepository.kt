package com.voizy.android.middleware.repositories

import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.collections.VoizySearchRequestCollection
import com.voizy.android.middleware.firebase.collections.VoizysCollection
import com.voizy.android.middleware.firebase.collections.result
import com.voizy.android.middleware.firebase.models.FirestoreVoizySearchResult
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.ui.models.Voizy
import com.voizy.android.utils.collectionChange
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File

class VoizyRepository(
    private val voizys: VoizysCollection,
    private val voizysSearch: VoizySearchRequestCollection,
    private val localFileManager: LocalFileManager,
    private val voizyStorage: VoizyFirebaseStorage
) {

    companion object {
        private val TAG = VoizyRepository::class.java.simpleName
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

    fun deleteLocalVoizy(localFilePath: String) {
        localFileManager.deleteFile(localFilePath)
    }

    fun searchVoizys(searchKeyword: String): Observable<Boolean> {
        return voizysSearch.find(searchKeyword)
            .map { it.result() }
            .doOnNext {
                Timber.d("search-voizys path ${it.path}")
            }
            .collectionChange()
            .filter {
                Timber.d("search-voizys filter size ${it.documentChanges.size}")
                it.documentChanges.size != 0
            }
            .map { it.toObjects(FirestoreVoizySearchResult::class.java) }
            .map { it.first() }
            .doOnNext {
                Timber.d("search-voizys hits ${it.hits}")
                Timber.d("search-voizys hits.voizys ${it.hits.hits}")
                Timber.d("search-voizys total value ${it.hits.total.value}")
            }
            .map { it.getVoizys() }
            .doOnNext {
                Timber.d("search-voizys voizys size ${it.size}")
            }
            .doOnNext {
                for (voizy in it) {
                    Timber.d("search-voizys voizy: ${voizy.name}}")
                }
            }
            .map { true }
    }

    // fun getVoizys(searchKeyword: String = ""): Observable<List<Voizy>> {
    //     return voizys.getVoizys()
    //         .flatMap { Observable.fromIterable(it) }
    //         .map { it.toVoizy() }
    //         .toList()
    //         .toObservable()
    //         .withErrorHandling(TAG, "failed to fetch Voizys")
    // }

    fun getFileUrl(firestorePath: String): Observable<String> {
        return voizyStorage.getDownloadUri(firestorePath)
            .map { it.toString() }
    }

    fun downloadVoizy(firestorePath: String, destinationFile: File): Observable<File> {
        return voizyStorage.getFile(firestorePath, destinationFile)
            .map { destinationFile }
    }
}