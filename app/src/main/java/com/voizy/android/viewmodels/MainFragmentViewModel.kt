package com.voizy.android.viewmodels

import androidx.lifecycle.ViewModel
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.model.Voizy
import com.voizy.android.repositories.FileRepository
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.Observable

class MainFragmentViewModel(
    private val fileRepository: FileRepository,
    private val voizyPlayer: VoizyPlayer
) : ViewModel() {

    private val voizySearchRequest = BehaviorSubject.create<Boolean>()
    private val voizysStream = voizySearchRequest
        .observeOn(Schedulers.io())
        .map { fileRepository.getAllVoizys() }

    public fun getVoizyStream(): Observable<Voizy> {
        return return voizysStream
    }

    public fun searchAllVoizys() {
        voizySearchRequest.onNext()
    }
}