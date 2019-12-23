package com.voizy.android.viewmodels

import android.net.Uri
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.audio.FFmpegManager
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.ui.model.ImportedData
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable

class RecordingViewModel(
    private val voizyRecorder: AudioRecorder,
    private val localFileManager: LocalFileManager,
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

    // TODO audio-editor move to audio editor fragment
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
                    .doOnNext { voizyRecorder.audioFileReceived() }
            }
            .withErrorHandling(TAG, "Failed to save received file")
    }

    // TODO audio-editor move to audio editor fragment
    /**
     * Clips to 15s, renames file and converts videos to audio
     */
    private fun editAudioForUpload(importedData: ImportedData): Observable<ImportedData> {
        val outputPath = localFileManager.getTempFilePath()
        localFileManager.deleteFile(outputPath)

        val editObservable = when {
            !isAudioTrackWithinLimit(importedData.durationInMillis) -> {
                importedData.durationInMillis = 15000
                ffmpegManager.clip(importedData.accessibleFilePath, outputPath)
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

    // TODO audio-editor move to audio edit fragment
    private fun isAudioTrackWithinLimit(audioDurationInMillis: Long): Boolean {
        return audioDurationInMillis < MAX_AUDIO_LENGTH_MS
    }
}
