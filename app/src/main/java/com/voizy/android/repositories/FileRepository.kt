package com.voizy.android.repositories

import android.content.Context
import timber.log.Timber
import java.io.File

class FileRepository(private val context: Context) {

    companion object {
        private const val TMP_FILE_NAME = "Voizy_tmp"
    }

    public fun getTemporaryPath(): String {
        return "${context.filesDir}/$TMP_FILE_NAME"
    }

    /**
     * @newFileName does not include the path, only the file name
     */
    public fun renameFile(newFileName: String): Boolean {
        try {
            val tmpPath = getTemporaryPath()
            val newPath = tmpPath.replace("_tmp", newFileName)
            File(tmpPath).renameTo(File(newPath))
            return true
        } catch (e: Exception) {
            Timber.e(e, "Failed to renamge the file")
            return false
        }
    }
}