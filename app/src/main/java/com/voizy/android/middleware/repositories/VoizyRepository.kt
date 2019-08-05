package com.voizy.android.middleware.repositories

import com.voizy.android.middleware.firebase.collections.VoizyCollection
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.ui.model.Voizy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class VoizyRepository(
    private val voizyFirestore: VoizyCollection,
    private val localFileManager: LocalFileManager
) {

    private val saveVoizyQueue = PublishSubject.create<Voizy>()
    private val saveVoizyEvents = saveVoizyQueue
        .share()
        .observeOn(Schedulers.io())
        .switchMap { localFileManager.saveVoizy(it) }
        .switchMap { voizyFirestore.saveVoizy(it) }
        .onErrorReturnItem(Pair(false, null))

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        return saveVoizyEvents
    }

    fun getTempFilePath(): String {
        return localFileManager.getTempFilePath()
    }

    fun saveVoizy(voizy: Voizy) {
        saveVoizyQueue.onNext(voizy)
    }

    fun getAllOwnVoizys(): List<Voizy> {
        return localFileManager.getAllOwnVoizys()
    }

    fun deleteLocalVoizy(localFilePath: String) {
        localFileManager.deleteFile(localFilePath)
    }

    fun getVoizys() {
        voizyFirestore.getVoizys()
    }
}