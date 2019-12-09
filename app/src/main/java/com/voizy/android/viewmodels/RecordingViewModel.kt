package com.voizy.android.viewmodels

import android.content.Context
import android.net.Uri
import com.voizy.android.audio.AudioPlayer
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
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
    private val voizyPlayer: AudioPlayer
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

    fun saveReceivedFile(uri: Uri): Observable<String> {
        return Observable
            .defer {
                Observable
                    .fromCallable {
                        localFileManager.saveUriContentToFile(
                            uri,
                            localFileManager.getImportFilePath()
                        )
                    }
                    .flatMap { finalizeImportedAudio(it) }
                    .doOnNext { voizyRecorder.audioFileReceived() }
            }

            .withErrorHandling(TAG, "Failed to save received file")
    }

    /**
     * Clips to 15s and renames file
     */
    fun finalizeImportedAudio(sourcePath: String): Observable<String> {
        val outputPath = localFileManager.getTempFilePath()
        localFileManager.deleteFile(outputPath)
        return localFileManager.renameToTempFile(sourcePath)

        // val audioLength = localFileManager.getAudioFileLengthInMillis(sourcePath)

        // TODO FFmpeg implement clipping and remove rename logics
        // return if (isAudioTrackWithinLimit(audioLength)) {
        //     localFileManager.renameToTempFile(sourcePath)
        // } else {
        //     ffmpegManager.clip(sourcePath, outputPath)
        //
        // }
    }

    fun getAudioFileLengthInSeconds(path: String): Observable<Int> {
        return Observable.defer {
            Observable.fromCallable {
                localFileManager.getAudioFileLengthInMillis(path)
            }
        }.map {
            (it / 1000).toInt()
        }.withErrorHandling(TAG, "failed to get audio length")
    }

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

    fun startPreviewVoizyPlayback() {
        voizyPlayer.togglePlay(voizyRepository.getTempFilePath())
            .withErrorHandling(TAG, "Failed to play preview voizy")
            .subscribeOn(Schedulers.io())
            .subscribe()
            .autoDispose()
    }

    fun releasePlayer() {
        voizyPlayer.release()
    }

    private fun isAudioTrackWithinLimit(audioLength: Long): Boolean {
        return audioLength < MAX_AUDIO_LENGTH_MS
    }
}
