package com.voizy.android.middleware.repositories

import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.collections.VoizyCollection
import com.voizy.android.middleware.firebase.model.FirestoreVoizy
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.ui.model.Voizy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class VoizyRepository(
    private val voizyFirestore: VoizyCollection,
    private val localFileManager: LocalFileManager,
    private val voizyStorage: VoizyFirebaseStorage
) {

    private val saveVoizyQueue = PublishSubject.create<Voizy>()
    private val saveVoizyEvents = saveVoizyQueue
        .observeOn(Schedulers.io())
        .switchMap { localFileManager.saveVoizy(it) }
        .doOnNext { Timber.d("save-voizy local save done $it") }
        .switchMap { voizyStorage.uploadVoizy(it) }
        .doOnNext { Timber.d("save-voizy upload done $it") }
        .switchMap { voizyFirestore.saveVoizy(it.second) }
        .doOnNext { Timber.d("save-voizy firestore save $it") }
        .onErrorReturnItem(Pair(false, null))
        .share()

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        return saveVoizyEvents
    }

    fun getTempFilePath(): String {
        return localFileManager.getTempFilePath()
    }

    fun saveVoizy(voizy: Voizy) {
        Timber.d("save-voizy")
        saveVoizyQueue.onNext(voizy)
    }

    fun getAllOwnVoizys(): List<Voizy> {
        return localFileManager.getAllOwnVoizys()
    }

    fun deleteLocalVoizy(localFilePath: String) {
        localFileManager.deleteFile(localFilePath)
    }

    fun getVoizys(): Observable<List<FirestoreVoizy>> {
        return voizyFirestore.getVoizys()
    }
}