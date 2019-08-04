package com.voizy.android.middleware.repositories

import com.voizy.android.middleware.model.Voizy
import io.reactivex.Observable

class VoizyRepository(
    private val voizyFirestore: VoizyFirestore,
    private val localFileManager: LocalFileManager
) {
    fun getSaveVoizyEvents(): Observable<Voizy> {
        return localFileManager.getSaveVoizyEvents()
    }

    fun getTempFilePath(): String {
        return localFileManager.getTempFilePath()
    }

    fun saveVoizy(voizy: Voizy) {
        localFileManager.saveVoizy(voizy.localFilePath)
    }

    fun getAllOwnVoizys(): List<Voizy> {
        return localFileManager.getAllOwnVoizys()
    }

    fun deleteLocalVoizy(localFilePath: String) {
        localFileManager.deleteFile(localFilePath)
    }
}