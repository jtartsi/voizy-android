package com.voizy.android.viewmodels

import android.net.Uri
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable

class RecordingViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyRecorder: VoizyRecorder,
    private val voizyFirebaseAnalytics: VoizyFirebaseAnalytics,
    private val localFileManager: LocalFileManager
) : DisposingViewModel() {

    companion object {
        private val TAG = RecordingViewModel::class.java.simpleName
    }

    fun getRecordingEvents(): Observable<VoizyRecorder.RecordingEvent> {
        return voizyRecorder.getRecordingEvents()
            .withErrorHandling(TAG, "recordingEvents error")
    }

    fun saveVoizy(voizy: Voizy) {
        voizyFirebaseAnalytics.logRecordingSave()
        voizyRepository.saveVoizy(voizy)
    }

    fun saveReceivedFileToTempLocation(uri: Uri): Observable<String> {
        return Observable.defer {
            Observable.fromCallable {
                localFileManager.saveUriContentToFile(uri, localFileManager.getTempFilePath())
            }
        }.doOnNext { voizyRecorder.audioFileReceived() }
    }

    fun getAudioFileLengthInSeconds(path: String): Observable<Int> {
        return Observable.defer {
            Observable.fromCallable {
                localFileManager.getAudioFileLengthInMillis(path)
            }
        }.map {
            (it / 1000).toInt()
        }
    }

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        return voizyRepository.getSaveVoizyEvents()
    }

    fun downloadVoizy() {
    }
}
