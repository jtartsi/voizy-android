package com.voizy.android.middleware.firebase

import com.google.firebase.storage.StorageReference
import com.voizy.android.ui.model.Voizy
import com.voizy.android.utils.toObservable
import io.reactivex.Observable
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.util.Date

class VoizyFirebaseStorage(
    private val storageRef: StorageReference
) {

    companion object {
        private const val VOIZYS_PATH = "voizys"
    }

    fun uploadVoizy(voizy: Voizy): Observable<Pair<Boolean, Voizy>> {
        val firebaseFileName = voizy.name.plus(Date().time)
        val uploadRef = storageRef.child(VOIZYS_PATH).child(firebaseFileName)
        val stream = FileInputStream(File(voizy.localFilePath))
        return uploadRef.putStream(stream).toObservable()
            .map {
                Timber.d("save-voizy uploadVoizy $it")
                if (it.uploadSessionUri == null) {
                    throw IllegalStateException("Failed to upload voizy")
                }
                voizy.firebaseFilePath = uploadRef.path
                Pair(true, voizy)
            }
    }
}