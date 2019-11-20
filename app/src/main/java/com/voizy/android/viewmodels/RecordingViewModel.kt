package com.voizy.android.viewmodels

import android.content.Context
import android.net.Uri
import com.voizy.android.audio.VoizyRecorder
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
    private val voizyRecorder: VoizyRecorder,
    private val voizyFirebaseAnalytics: VoizyFirebaseAnalytics,
    private val localFileManager: LocalFileManager,
    private val shareManager: ShareManager
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
        return Observable
            .defer {
                Observable.fromCallable {
                    localFileManager.saveUriContentToFile(uri, localFileManager.getTempFilePath())
                }
            }
            .doOnNext { voizyRecorder.audioFileReceived() }
            .withErrorHandling(TAG, "Failed to save received file")
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
            .downloadVoizy(voizy.filePath, destinationFile)
            .map { Pair(voizy, it) }
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to download Voizy")
    }

    fun startVoizyShare(context: Context, voizy: Voizy, file: File) {
        shareManager.startVoizyShare(context, voizy, file)
    }
}
