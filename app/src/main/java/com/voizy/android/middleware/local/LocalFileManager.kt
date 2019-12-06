package com.voizy.android.middleware.local

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

class LocalFileManager(private val context: Context) {

    companion object {
        private val TAG = LocalFileManager::class.java.simpleName

        const val TMP_IMPORT_FILE_NAME = "source.mp3"
        const val TMP_VOIZY_FILE_NAME = "voizy_tmp.mp3"
        const val VOIZY_FILE_PREFIX = "voizy_"
        const val MP3_FILE_EXT = ".mp3"
    }

    fun saveVoizy(voizy: Voizy): Observable<Voizy> {
        return Observable.just(voizy)
            .map { it.copy(localPath = renameFile(it.name)) }
            .withErrorHandling(TAG, "Failed to save voizy locally")
    }

    fun getImportFilePath(): String {
        return "${context.filesDir}/".plus(TMP_IMPORT_FILE_NAME)
    }

    fun getTempFilePath(): String {
        return "${context.filesDir}/".plus(TMP_VOIZY_FILE_NAME)
    }

    fun renameToTempFile(sourceFile: String): Observable<String> {
        return Observable.just(sourceFile)
            .map {
                File(it).renameTo(File(getTempFilePath()))
                getTempFilePath()
            }
    }

    fun saveUriContentToFile(uri: Uri, newPath: String): String {
        val destinationFile = File(newPath)
        deleteFile(destinationFile.path)
        destinationFile.createNewFile()

        lateinit var sourceChannel: FileChannel
        lateinit var destinationChannel: FileChannel
        return try {
            sourceChannel = FileInputStream(
                context.contentResolver
                    .openFileDescriptor(uri, "r")!!
                    .fileDescriptor
            ).channel
            destinationChannel = FileOutputStream(destinationFile).channel
            destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size())
            destinationFile.path
        } catch (e: Exception) {
            Timber.e("saveUriToContentFile catch $e")
            ""
        } finally {
            sourceChannel.close()
            destinationChannel.close()
        }
    }

    fun getAudioFileLengthInMillis(path: String): Long {
        val mediaDataRetriever = MediaMetadataRetriever()
        mediaDataRetriever.setDataSource(path)
        return mediaDataRetriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            .toLong()
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