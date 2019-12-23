package com.voizy.android.viewmodels

import android.net.Uri
import com.voizy.android.audio.AudioPlayer
import com.voizy.android.audio.FFmpegManager
import com.voizy.android.audio.PlaybackInfo
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.ui.model.ImportedData
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable

class AudioClipViewModel(
    private val voizyPlayer: AudioPlayer,
    private val voizyRepository: VoizyRepository,
    private val ffmpegManager: FFmpegManager,
    private val localFileManager: LocalFileManager
) : DisposingViewModel() {

    val playbackEvents: Observable<PlaybackInfo>
        get() = voizyPlayer.playbackEventStream

    companion object {
        private val TAG = AudioClipViewModel::class.java.simpleName
        private const val MAX_AUDIO_LENGTH_MS = 15000
    }

    fun togglePlay(startPos: Int, endPos: Int): Observable<PlaybackInfo> {
        return voizyPlayer.togglePlay(voizyRepository.getTempFilePath(), startPos, endPos)
            .withErrorHandling(TAG, "Failed to togglePlay on preview voizy")
    }

    fun stopPlayback(): Observable<Int> {
        return voizyPlayer.stop()
            .withErrorHandling(TAG, "Failed to stopPlayback on preview voizy")
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
                            .getAudioDurationInMillis(it.accessibleFilePath)
                        it
                    }
                    .flatMap { editAudioForUpload(it) }
            }
            .withErrorHandling(TAG, "Failed to save received file")
    }

    /**
     * Clips to 15s, renames file and converts videos to audio
     */
    private fun editAudioForUpload(importedData: ImportedData): Observable<ImportedData> {
        val outputPath = localFileManager.getTempFilePath()
        localFileManager.deleteFile(outputPath)

        val editObservable = when {
            !isAudioTrackWithinLimit(importedData.durationInMillis) -> {
                // importedData.durationInMillis = 15000
                // ffmpegManager.clip(importedData.accessibleFilePath, outputPath)
                // TODO audio-editor remove and reorganize this
                localFileManager.renameToTempFile(importedData.accessibleFilePath)
            }
            isAudioTrackWithinLimit(importedData.durationInMillis)
                && importedData.contentType == ImportedData.TYPE_VIDEO -> {
                ffmpegManager.convertToAudio(importedData.accessibleFilePath, outputPath)
            }
            isAudioTrackWithinLimit(importedData.durationInMillis)
                && importedData.contentType == ImportedData.TYPE_AUDIO -> {
                localFileManager.renameToTempFile(importedData.accessibleFilePath)
            }
            else -> {
                throw IllegalStateException("Editing file import failed")
            }
        }

        return editObservable.map {
            importedData.accessibleFilePath = it
            importedData
        }
    }

    private fun isAudioTrackWithinLimit(audioDurationInMillis: Long): Boolean {
        return audioDurationInMillis < MAX_AUDIO_LENGTH_MS
    }
}