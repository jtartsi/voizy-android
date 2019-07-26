package com.voizy.android.viewmodels

import androidx.lifecycle.ViewModel
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.model.Voizy
import com.voizy.android.repositories.FileRepository
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class MainFragmentViewModel(
    private val fileRepository: FileRepository,
    private val voizyPlayer: VoizyPlayer
) : ViewModel() {

    private enum class VoizyLocation { PRIVATE, PUBLIC }

    private val voizySearchRequest = BehaviorSubject.create<VoizyLocation>()
    private val voizysStream = voizySearchRequest
        .observeOn(Schedulers.io())
        .map {
            when (it) {
                VoizyLocation.PUBLIC -> fileRepository.getReceivedVoizys()
                VoizyLocation.PRIVATE -> fileRepository.getAllOwnVoizys()
            }
        }

    public fun getVoizyStream(): Observable<List<Voizy>> {
        return voizysStream
    }

    public fun getReceivedVoizys() {
        voizySearchRequest.onNext(VoizyLocation.PUBLIC)
    }

    public fun getOwnVoizys() {
        voizySearchRequest.onNext(VoizyLocation.PRIVATE)
    }
}