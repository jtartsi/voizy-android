package com.voizy.android.repositories

import android.content.Context
import com.voizy.android.model.Voizy
import timber.log.Timber
import java.io.File

class FileRepository(private val context: Context) {

    fun getTempFilePath(): String {
        return "${context.filesDir}/voizy_tmp"
    }

    /**
     * @newFileName does not include the path, only the file name
     */
    fun renameFile(newFileName: String): Boolean {
        try {
            val tmpPath = getTempFilePath()
            Timber.d("renameFile before $tmpPath")
            val newPath = tmpPath.replace("_tmp", "_")
                .plus(newFileName.toLowerCase())
            Timber.d("renameFile after $newPath")
            File(tmpPath).renameTo(File(newPath))
            return true
        } catch (e: Exception) {
            Timber.e(e, "Failed to rename the file")
            return false
        }
    }

    fun getAllOwnVoizys(): List<Voizy> {
        return context.filesDir.listFiles().map { Voizy(it.path) }
    }

    fun getReceivedVoizys(): List<Voizy> {
        val voizyFiles = getAllFilesInFolderTree(File("/sdcard/"), "voizy")
        val voizys = voizyFiles.map { Voizy(it.absolutePath) }
        return voizys
    }

    private fun getAllFilesInFolderTree(parentDir: File, criteria: String): List<File> {
        val inFiles = mutableListOf<File>()
        val files = parentDir.listFiles()

        for (file in files) {
            if (file.isDirectory) {
                inFiles.addAll(getAllFilesInFolderTree(file, criteria))
            } else {
                if (file.name.contains(criteria)) {
                    inFiles.add(file)
                }
            }
        }
        return inFiles
    }
}