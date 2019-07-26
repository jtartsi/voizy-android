package com.voizy.android.utils

import android.content.Context
import timber.log.Timber
import java.io.File

class FileUtil {

    companion object {

        public fun getDefaultFileName(context: Context): String {
            return "${context.filesDir}/voizy_tmp"
        }

        /**
         * @currentPath includes the path and the name of the file
         * @newName does not include the path, only the file name
         */
        public fun renameFile(currentPath: String, newName: String): Boolean {
            try {
                val newPath = currentPath.replace("_tmp", newName)
                File(currentPath).renameTo(File(newPath))
                return true
            } catch (e: Exception) {
                Timber.e(e, "Failed to renamge the file")
                return false
            }
        }
    }
}