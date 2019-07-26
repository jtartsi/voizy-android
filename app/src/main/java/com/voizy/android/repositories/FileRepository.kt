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
            val newPath = tmpPath.replace("_tmp", newFileName)
            File(tmpPath).renameTo(File(newPath))
            return true
        } catch (e: Exception) {
            Timber.e(e, "Failed to renamge the file")
            return false
        }
    }

    // TODO
    /*
     1. check if file save (rename) works
     2. fix so that the file save is not allowed to finish until
     3. fix playback

     /data/user/0/com.voizy.android/files/Voizy_tmp
     */

    fun getAllOwnVoizys(): List<Voizy> {
        Timber.d("file-iss getAllOwnVoizys")

        // This returns one file, maybe we need to check if the file save really works
        val fileList = context.fileList()
        Timber.d("file-iss getAllOwnVoizys size-test ${fileList.size}")
        val privateFolder = File("${context.filesDir}/")

        val voizyFiles = getAllFilesInFolderTree(privateFolder, "Voizy")
        val voizys = voizyFiles.map {
            Voizy(it.path)
        }

        Timber.d("file-iss getAllOwnVoizys size ${voizys.size}")
        return voizys
    }

    fun getReceivedVoizys(): List<Voizy> {
        Timber.d("file-iss getReceivedVoizys")
        val voizyFiles = getAllFilesInFolderTree(File("/sdcard/"), "Voizy")
        val voizys = voizyFiles.map {
            Voizy(it.absolutePath)
        }
        Timber.d("file-iss getReceivedVoizys size ${voizys.size}")
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
        // return inFiles.filter { it.extension === ".3gpp" }
    }
}