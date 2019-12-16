package com.voizy.android.viewmodels

import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable

class VoizyDetailsViewModel(
    private val voizyRepository: VoizyRepository,
    private val localFileManager: LocalFileManager
) : DisposingViewModel() {

    companion object {
        private val TAG = VoizyDetailsViewModel::class.java.simpleName
    }

    fun getLastVoizyToBeSaved(): Observable<Voizy> {
        return voizyRepository.lastVoizyToBeSaved()
    }

    fun getAudioFileLengthInSeconds(path: String): Observable<Int> {
        return Observable
            .defer {
                Observable.fromCallable {
                    localFileManager.getAudioFileLengthInMillis(path)
                }
            }.map {
                (it / 1000).toInt()
            }
            .withErrorHandling(TAG, "failed to get audio length")
    }
}