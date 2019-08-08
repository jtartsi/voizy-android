package com.voizy.android.viewmodels

import androidx.lifecycle.ViewModel
import com.uber.autodispose.autoDisposable
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.model.FirestoreVoizy
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.ui.model.Voizy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import timber.log.Timber

class MainFragmentViewModel(
    private val voizyRepository: VoizyRepository,
    private val voizyPlayer: VoizyPlayer,
    private val voizyStorage: VoizyFirebaseStorage
) : ViewModel() {

    private val voizySearchRequest = ReplaySubject.create<Boolean>()
    private val voizysStream = voizySearchRequest
        .observeOn(Schedulers.io())
        .map { voizyRepository.getAllOwnVoizys() }

    fun getSaveVoizyEvents(): Observable<Pair<Boolean, Voizy?>> {
        return voizyRepository.getSaveVoizyEvents()
    }

    fun getVoizyStream(): Observable<List<Voizy>> {
        return voizysStream
    }

    fun getLocalVoizys() {
        voizySearchRequest.onNext(true)
    }

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

    fun playRemoteVoizy(firebasePath: String) {
        voizyStorage.getDownloadUri(firebasePath)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("play-remote-voizy playRemoteVoizy $it, path ${it.path}")
                voizyPlayer.play(it.path)
            }
    }

    fun playVoizy(filePath: String): Observable<Int> {
        return Observable.just(filePath)
            .map { voizyPlayer.play(it) }
    }

    fun fetchRemoteVoizys(): Observable<List<FirestoreVoizy>> {
        return voizyRepository.getVoizys()
    }
}