package com.voizy.android.middleware.repositories

import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.collections.VoizySearchRequestCollection
import com.voizy.android.middleware.firebase.collections.VoizysCollection
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.ui.models.Voizy
import com.voizy.android.utils.getSnapshotChange
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
            .getSnapshotChange()
            .map {
                // TODO search parse data
                Timber.d("search-voizys snapshot change $it")
                true
            }
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