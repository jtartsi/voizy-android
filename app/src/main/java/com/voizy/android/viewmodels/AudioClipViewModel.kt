package com.voizy.android.viewmodels

import com.voizy.android.audio.AudioPlayer
import com.voizy.android.audio.FFmpegManager
import com.voizy.android.audio.PlaybackInfo
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.ui.model.ImportedData
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable

class AudioClipViewModel(
    private val voizyPlayer: AudioPlayer,
    private val ffmpegManager: FFmpegManager,
    private val localFileManager: LocalFileManager,
    private val firebaseAnalytics: VoizyFirebaseAnalytics
) : DisposingViewModel() {

    val playbackEvents: Observable<PlaybackInfo>
        get() = voizyPlayer.playbackEventStream

    companion object {
        private val TAG = AudioClipViewModel::class.java.simpleName
    }

    fun togglePlay(startPos: Int, endPos: Int): Observable<PlaybackInfo> {
        return voizyPlayer.togglePlay(localFileManager.getImportFilePath(), startPos, endPos)
            .withErrorHandling(TAG, "Failed to togglePlay on preview voizy")
    }

    fun stopPlayback(): Observable<Int> {
        return voizyPlayer.stop()
            .withErrorHandling(TAG, "Failed to stopPlayback on preview voizy")
    }

    fun getImportedData(filePath: String): Observable<ImportedData> {
        return Observable
            .defer {
                Observable.just(ImportedData(filePath))
                    .map {
                        it.durationInMillis = localFileManager
                            .getAudioDurationInMillis(it.filePath)
                        it
                    }
            }
            .withErrorHandling(TAG, "Failed to get ImportedData")
    }

    fun clipAudio(startPosInMs: Long, endPosInMs: Long): Observable<String> {
        return Observable.just(localFileManager.getTempFilePath())
            .doOnNext { localFileManager.deleteFile(it) }
            .switchMap {
                ffmpegManager.clip(
                    localFileManager.getImportFilePath(),
                    localFileManager.getTempFilePath(),
                    startPosInMs, endPosInMs
                )
            }.withErrorHandling(TAG, "Failed to clip received file")
    }

    fun logAudioEditorOpened() {
        firebaseAnalytics.logEditScreenOpen()
    }
}