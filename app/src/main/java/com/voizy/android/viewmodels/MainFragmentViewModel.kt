package com.voizy.android.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.ui.model.Voizy
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.io.File

class MainFragmentViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyPlayer: VoizyPlayer
) : ViewModel() {

    companion object {
        private val TAG = MainFragmentViewModel::class.java.simpleName
    }

    private val voizyFetchRequest = BehaviorSubject.create<Boolean>()
    private val voizysStream = voizyFetchRequest
        .observeOn(Schedulers.io())
        .flatMap { voizyRepository.getVoizys() }

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        return voizyRepository.getSaveVoizyEvents()
            .doOnNext { voizyFetchRequest.onNext(true) }
    }

    fun getVoizyStream(): Observable<List<Voizy>> {
        return voizysStream
    }

    fun fetchVoizys() {
        voizyFetchRequest.onNext(true)
    }

    // TODO v0.3.0 remove this
    fun deleteVoizy(voizy: Voizy) {
        val completable = Completable.fromAction {
            voizyRepository.deleteLocalVoizy(voizy.localFilePath!!)
        }
            .onErrorComplete()
            .subscribeOn(Schedulers.io())

        completable.apply {
            autoDisposable(this)
            subscribe()
        }
    }

    fun playVoizy(voizy: Voizy): Observable<Int> {
        return voizyRepository.getFileUrl(voizy.firebaseFilePath!!)
            .map { voizyPlayer.playRemote(it) }
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to play Voizy")
    }

    fun downloadVoizy(context: Context, voizy: Voizy): Observable<File> {
        val destinationFile = File(LocalFileManager(context).getTempFilePath())
        return voizyRepository.downloadVoizy(voizy.firebaseFilePath!!, destinationFile)
            .subscribeOn(Schedulers.io())
            .withErrorHandling(TAG, "Failed to download Voizy")
    }
}