package com.voizy.android.viewmodels

import com.voizy.android.audio.AudioPlayer
import com.voizy.android.audio.PlaybackInfo
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable

class SaveVoizyViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyFirebaseAnalytics: VoizyFirebaseAnalytics,
    private val voizyPlayer: AudioPlayer,
    private val localFileManager: LocalFileManager
) : DisposingViewModel() {

    companion object {
        private val TAG = SaveVoizyViewModel::class.java.simpleName
    }

    fun saveVoizy(voizy: Voizy) {
        voizyFirebaseAnalytics.logSaveVoizy()
        voizyRepository.saveVoizy(voizy)
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
        return voizyPlayer.playbackEventStream
            .withErrorHandling(TAG, "Error in playbackEventStream")
    }

    fun getTempVoizyDurationInMillis(): Observable<Long> {
        return Observable.defer {
            Observable.fromCallable {
                localFileManager.getAudioDurationInMillis(localFileManager.getTempFilePath())
            }
        }.withErrorHandling(TAG, "Failed to get duration on preview voizy")
    }
}