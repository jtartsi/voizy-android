package com.voizy.android.middleware.repositories

import com.voizy.android.middleware.firebase.collections.VoizyCollection
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.ui.model.Voizy
import io.reactivex.Observable
import timber.log.Timber

class VoizyRepository(
    private val voizyFirestore: VoizyCollection,
    private val localFileManager: LocalFileManager
) {
    fun getSaveVoizyEvents(): Observable<Voizy> {
        return localFileManager.getSaveVoizyEvents()
    }

    fun getTempFilePath(): String {
        return localFileManager.getTempFilePath()
    }

    fun saveVoizy(voizy: Voizy) {
        Timber.d("saveVoizy()")
        localFileManager.saveVoizy(voizy.localPath)
        voizyFirestore.saveVoizy(voizy)
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