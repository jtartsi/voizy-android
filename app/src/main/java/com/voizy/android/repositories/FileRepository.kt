package com.voizy.android.repositories

import android.content.Context
import com.voizy.android.model.Voizy
import timber.log.Timber
import java.io.File

class FileRepository(private val context: Context) {

    companion object {
        private const val TMP_FILE_NAME = "Voizy_tmp"
    }

    public fun getTempFilePath(): String {
        return "${context.filesDir}/$TMP_FILE_NAME"
    }

    /**
     * @newFileName does not include the path, only the file name
     */
    public fun renameFile(newFileName: String): Boolean {
        try {
            val tmpPath = getTempFilePath()
            val newPath = tmpPath.replace("_tmp", newFileName)
            File(tmpPath).renameTo(File(newPath))
            return true
        } catch (e: Exception) {
            Timber.e(e, "Failed to renamge the file")
            return false
        }
    }

    public fun getAllVoizys(): List<Voizy> {
        val allVoizyFiles = getAllFilesInFolder(File("/"), "Voizy")
        val allVoizys = allVoizyFiles.map {
            Voizy(it.absolutePath)
        }
        return allVoizys
    }

    private fun getAllFilesInFolder(parentDir: File, criteria: String): List<File> {
        val inFiles = mutableListOf<File>()
        val files = parentDir.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                inFiles.addAll(getAllFilesInFolder(file, criteria))
            } else {
                if (file.name.contains(criteria)) {
                    inFiles.add(file)
                }
            }
        }
        return inFiles
    }

    // private fun getListFiles(parentDir: File): List<File> {
    //     val inFiles = ArrayList<File>()
    //     val files = parentDir.listFiles()
    //     for (file in files) {
    //         if (file.isDirectory) {
    //             inFiles.addAll(getListFiles(file))
    //         } else {
    //             if (file.name.endsWith(".csv")) {
    //                 inFiles.add(file)
    //             }
    //         }
    //     }
    //     return inFiles
    // }
}