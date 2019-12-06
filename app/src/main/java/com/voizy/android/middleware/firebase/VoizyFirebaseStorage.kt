package com.voizy.android.middleware.firebase

import android.net.Uri
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageReference
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.utils.toObservable
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import java.io.File
import java.io.FileInputStream
import java.net.URLEncoder
import java.util.Date

class VoizyFirebaseStorage(
    private val storageRef: StorageReference
) {

    companion object {
        private val TAG = VoizyFirebaseStorage::class.java.simpleName
        private const val VOIZYS_PATH = "voizys"
    }

    fun uploadVoizy(voizy: Voizy): Observable<Pair<Boolean, Voizy>> {

        val firebaseFileName = voizy.name.plus(Date().time).plus(".mp3")
        val encodedFirebaseFileName = URLEncoder.encode(firebaseFileName, "UTF-8")

        val uploadRef = storageRef.child(VOIZYS_PATH).child(encodedFirebaseFileName)
        val stream = FileInputStream(File(voizy.localPath))

        return uploadRef.putStream(stream).toObservable()
            .map {
                if (it.uploadSessionUri == null) {
                    throw IllegalStateException("Failed to upload voizy")
                }
                val uploadedVoizy = voizy.copy(remoteUrl = uploadRef.path)
                Pair(true, uploadedVoizy)
            }
            .withErrorHandling(TAG, "Failed to upload audio file")
    }

    fun getFile(
        fireBasePath: String,
        destinationFile: File
    ): Observable<FileDownloadTask.TaskSnapshot> {
        return storageRef.child(fireBasePath)
            .getFile(destinationFile)
            .toObservable()
    }

    fun getDownloadUri(path: String): Observable<Uri> {
        return storageRef.child(path)
            .downloadUrl
            .toObservable()
    }
}