package com.voizy.android.middleware.repositories

import android.content.Context
import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.collections.VoizyCollection
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.ui.model.Voizy
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File

class VoizyRepository(
    private val voizyFirestore: VoizyCollection,
    private val localFileManager: LocalFileManager,
    private val voizyStorage: VoizyFirebaseStorage
) {

    companion object {
        private val TAG = VoizyRepository::class.java.simpleName
    }

    // TODO implement error repository

    // private val saveVoizyQueue = PublishSubject.create<Voizy>()
    // private val saveVoizyEvents = saveVoizyQueue
    //     .observeOn(Schedulers.io())
    //     .doOnNext {
    //         Timber.d("save-voizy saveVoizyEventStream, before save local")
    //     }
    //     .switchMap { localFileManager.saveVoizy(it) }
    //     .switchMap { voizyStorage.uploadVoizy(it) }
    //     .switchMap { voizyFirestore.saveVoizy(it.second) }
    //     .share()

    private val saveVoizyEvents = BehaviorSubject.create<Pair<Boolean, Voizy?>>()

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        Timber.d("save-voizy getSaveVoizyEvents()")
        return saveVoizyEvents
    }

    fun getTempFilePath(): String {
        return localFileManager.getTempFilePath()
    }

    fun saveVoizy(voizy: Voizy) {
        Timber.d("save-voizy saveVoizy() ${voizy.name}")

        Observable.just(voizy)
            .doOnNext { Timber.d("save-voizy before local save ") }
            .switchMap { localFileManager.saveVoizy(it) }
            .doOnNext { Timber.d("save-voizy before upload") }
            .switchMap { voizyStorage.uploadVoizy(it) }
            .doOnNext { Timber.d("save-voizy before save db") }
            .switchMap { voizyFirestore.saveVoizy(it.second) }
            .doOnNext { Timber.d("save-voizy after save db") }
            .subscribeOn(Schedulers.io())
            .subscribe(saveVoizyEvents)
    }

    fun getAllOwnVoizys(): List<Voizy> {
        return localFileManager.getAllOwnVoizys()
            .filter { it.name != "tmp" }
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

    fun downloadVoizy(context: Context, firebasePath: String) {
        val destinationFile = File(LocalFileManager(context).getTempFilePath())
        voizyStorage.getFile(firebasePath, destinationFile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun downloadVoizy(firestorePath: String, destinationFile: File): Observable<File> {
        return voizyStorage.getFile(firestorePath, destinationFile)
            .map { destinationFile }
    }
}