package com.voizy.android.middleware.local

import android.content.Context
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import timber.log.Timber
import java.io.File

class LocalFileManager(private val context: Context) {

    companion object {
        private val TAG = LocalFileManager::class.java.simpleName

        const val TMP_VOIZY_FILE_NAME = "voizy_tmp.mp3"
        const val VOIZY_FILE_PREFIX = "voizy_"
        const val MP3_FILE_EXT = ".mp3"
    }

    fun saveVoizy(voizy: Voizy): Observable<Voizy> {
        return Observable.just(voizy)
            .map { it.copy(filePath = renameFile(it.name)) }
            .withErrorHandling(TAG, "Failed to save voizy locally")
    }

    fun getTempFilePath(): String {
        return "${context.filesDir}/".plus(TMP_VOIZY_FILE_NAME)
    }

    /**
     * @newFileName does not include the path, only the file name
     */
    private fun renameFile(newFileName: String): String {
        try {
            if (newFileName.isEmpty()) {
                throw IllegalArgumentException("Empty file name")
            }
            val tmpPath = getTempFilePath()
            val newPath = tmpPath.replace(
                TMP_VOIZY_FILE_NAME,
                VOIZY_FILE_PREFIX
            )
                .plus(newFileName)
                .plus(MP3_FILE_EXT)

            return if (File(tmpPath).renameTo(File(newPath))) {
                newPath
            } else {
                ""
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to rename the file return empty")
            return ""
        }
    }

    fun deleteFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }
}