package com.voizy.android.repositories

import android.content.Context
import com.voizy.android.model.Voizy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File

class FileRepository(private val context: Context) {

    private val renameSubject = PublishSubject.create<String>()
    private val saveNameEvents = renameSubject
        .observeOn(Schedulers.io())
        .map { renameFile(it) }
        .map { Voizy(it) }
        .share()

    fun renameVoizy(newFileName: String) {
        Timber.d("save-voizy-iss renameVoizy.onNext() $newFileName")
        renameSubject.onNext(newFileName)
    }

    fun getSaveVoizyEvents(): Observable<Voizy> {
        return saveNameEvents
    }

    fun getTempFilePath(): String {
        return "${context.filesDir}/voizy_tmp"
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
            val newPath = tmpPath.replace("_tmp", "_").plus(newFileName)

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