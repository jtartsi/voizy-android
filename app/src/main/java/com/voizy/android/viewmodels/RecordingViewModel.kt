package com.voizy.android.viewmodels

import android.content.Context
import android.net.Uri
import com.voizy.android.audio.AudioPlayer
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.audio.FFmpegManager
import com.voizy.android.audio.PlaybackInfo
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.ui.model.ImportedData
import com.voizy.android.utils.ShareManager
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.File

class RecordingViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyRecorder: AudioRecorder,
    private val voizyFirebaseAnalytics: VoizyFirebaseAnalytics,
    private val localFileManager: LocalFileManager,
    private val shareManager: ShareManager,
    private val voizyPlayer: AudioPlayer,
    private val ffmpegManager: FFmpegManager
) : DisposingViewModel() {

    companion object {
        private val TAG = RecordingViewModel::class.java.simpleName
        private const val MAX_AUDIO_LENGTH_MS = 15000
    }

    fun getRecordingEvents(): Observable<AudioRecorder.RecordingEvent> {
        return voizyRecorder.getRecordingEvents()
            .withErrorHandling(TAG, "recordingEvents error")
    }

    fun saveVoizy(voizy: Voizy) {
        voizyFirebaseAnalytics.logRecordingSave()
        voizyRepository.saveVoizy(voizy)
    }

    fun saveReceivedFile(uri: Uri): Observable<ImportedData> {
        return Observable
            .defer {
                Observable.just(ImportedData(uri))
                    .map {
                        it.accessibleFilePath = localFileManager.saveUriContentToFile(
                            it.uri,
                            localFileManager.getImportFilePath()
                        )
                        it
                    }
                    .map {
                        it.durationInMillis = localFileManager
                            .getAudioFileLengthInMillis(it.accessibleFilePath)
                        it
                    }
                    .flatMap { editAudioForUpload(it) }
                    .doOnNext { voizyRecorder.audioFileReceived() }
            }
            .withErrorHandling(TAG, "Failed to save received file")
    }

    fun finalizeImportedAudio(sourcePath: String): Observable<String> {
        val outputPath = localFileManager.getTempFilePath()
        localFileManager.deleteFile(outputPath)
        return localFileManager.renameToTempFile(sourcePath)
    }

    /**
     * Clips to 15s and renames file
     */
    private fun editAudioForUpload(importedData: ImportedData): Observable<ImportedData> {
        val outputPath = localFileManager.getTempFilePath()
        localFileManager.deleteFile(outputPath)

        val editObservable = when {
            !isAudioTrackWithinLimit(importedData.durationInMillis) -> {
                ffmpegManager.clip(importedData.accessibleFilePath, outputPath)
            }
            isAudioTrackWithinLimit(importedData.durationInMillis) -> {
                localFileManager.renameToTempFile(importedData.accessibleFilePath)
            }
            // TODO video-to-audio add video logic here.
            else -> {
                throw IllegalStateException("Editing file import failed")
            }
        }

        return editObservable.map {
            importedData.accessibleFilePath = it
            importedData
        }
    }

    // /**
    //  * Edits audio to be ready for upload
    //  *  - Clips to 15s
    //  *  - Converts video to audio
    //  *  - Renames file
    //  */
    // private fun editAudioForUpload(import: ImportedFile): Observable<String> {
    //     val outputPath = localFileManager.getTempFilePath()
    //     localFileManager.deleteFile(outputPath)
    //     return when {
    //         !isLengthWithinLimit(import.durationInMillis) -> {
    //             ffmpegManager.clip(import.filePath, outputPath)
    //         }
    //         isLengthWithinLimit(import.durationInMillis) && import.contentType == TYPE_VIDEO -> {
    //             ffmpegManager.convertToAudio(import.filePath, outputPath)
    //         }
    //         isLengthWithinLimit(import.durationInMillis) && import.contentType == TYPE_AUDIO -> {
    //             localFileManager.renameToTempFile(import.filePath)
    //         }
    //         else -> {
    //             throw IllegalStateException("Editing file import failed")
    //         }
    //     }
    // }

    // fun getAudioFileLengthInMillis(path: String): Long {
    //     return localFileManager.getAudioFileLengthInMillis(path)
    // }
    //
    // fun getAudioFileLengthInSeconds(path: String): Observable<Int> {
    //     return Observable
    //         .defer {
    //             Observable.fromCallable {
    //                 localFileManager.getAudioFileLengthInMillis(path)
    //             }
    //         }.map {
    //             (it / 1000).toInt()
    //         }
    //         .withErrorHandling(TAG, "failed to get audio length")
    // }

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        return voizyRepository.getSaveVoizyEvents()
    }

    fun downloadVoizy(context: Context, voizy: Voizy): Observable<Pair<Voizy, File>> {
        val destinationFile = File(LocalFileManager(context).getTempFilePath())
        return voizyRepository
            .downloadVoizy(voizy.remoteUrl, destinationFile)
            .map { Pair(voizy, it) }
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to download Voizy")
    }

    fun startVoizyShare(context: Context, voizy: Voizy, file: File) {
        shareManager.startVoizyShare(context, voizy, file)
    }

    fun togglePlay(): Observable<PlaybackInfo> {
        return voizyPlayer.togglePlay(voizyRepository.getTempFilePath())
            .withErrorHandling(TAG, "Failed to togglePlay on preview voizy")
    }

    fun stopPlayback(): Observable<Int> {
        return voizyPlayer.stop()
            .withErrorHandling(TAG, "Failed to stopPlayback on preview voizy")
    }

    fun getPlaybackEvents(): Observable<PlaybackInfo> {
        return voizyPlayer.getPlaybackEvents()
    }

    private fun isAudioTrackWithinLimit(audioDurationInMillis: Long): Boolean {
        return audioDurationInMillis < MAX_AUDIO_LENGTH_MS
    }
}
