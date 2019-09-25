package com.voizy.android.middleware.repositories

import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.collections.VoizyCollection
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.ui.model.Voizy
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File

class VoizyRepository(
    private val voizyFirestore: VoizyCollection,
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
        .switchMap { voizyFirestore.saveVoizy(it.second) }
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

    fun getVoizys(): Observable<List<Voizy>> {
        return voizyFirestore.getVoizys()
            .flatMap { Observable.fromIterable(it) }
            .map { it.toVoizy() }
            .toList()
            .toObservable()
            .withErrorHandling(TAG, "failed to fetch Voizys")
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