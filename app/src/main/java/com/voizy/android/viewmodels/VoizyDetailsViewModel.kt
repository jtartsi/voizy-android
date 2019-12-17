package com.voizy.android.viewmodels

import android.content.Context
import com.voizy.android.audio.AudioPlayer
import com.voizy.android.audio.PlaybackInfo
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.utils.ShareManager
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable
import java.io.File

class VoizyDetailsViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyPlayer: AudioPlayer,
    private val shareManager: ShareManager,
    private val localFileManager: LocalFileManager
) : DisposingViewModel() {

    val playbackEvents: Observable<PlaybackInfo>
        get() = voizyPlayer.getPlaybackEvents()

    companion object {
        private val TAG = VoizyDetailsViewModel::class.java.simpleName
    }

    fun getLastVoizyToBeSaved(): Observable<Voizy> {
        return voizyRepository.lastVoizyToBeSaved()
    }

    fun togglePlay(): Observable<PlaybackInfo> {
        return voizyRepository.lastVoizyToBeSaved()
            .flatMap { voizyPlayer.togglePlay(it.localPath) }
            .withErrorHandling(TAG, "Failed to togglePlay on preview voizy")
    }

    fun stopPlayback(): Observable<Int> {
        return voizyPlayer
            .stop()
            .withErrorHandling(TAG, "Failed to stopPlayback on preview voizy")
    }

    fun share(context: Context) {
        voizyRepository.lastVoizyToBeSaved()
            .doOnNext { voizy ->
                shareManager.startVoizyShare(context, voizy, File(voizy.localPath))
            }
            .withErrorHandling(TAG, "Failed to share voizy")
            .subscribe()
            .autoDispose()
    }

    fun deleteLocalFile(): Observable<Unit> {
        return voizyRepository.lastVoizyToBeSaved()
            .flatMap {
                Observable.fromCallable { localFileManager.deleteFile(it.localPath) }
            }
            .withErrorHandling(TAG, "Failed to delete local file")
    }
}