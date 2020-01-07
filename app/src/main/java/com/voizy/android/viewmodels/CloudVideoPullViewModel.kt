package com.voizy.android.viewmodels

import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.utils.toPair
import com.voizy.android.utils.withErrorHandling
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File

class CloudVideoPullViewModel(
    private val youtubeDL: YoutubeDL,
    private val fileManager: LocalFileManager
) : DisposingViewModel() {

    private val cancelEvents = BehaviorSubject.create<Boolean>()
    private val downloadQueue = PublishSubject.create<String>()
    val downloadEvents = downloadQueue
        .observeOn(Schedulers.io())
        .flatMap { downloadVideo(it) }

    companion object {
        private val TAG = CloudVideoPullViewModel::class.java.simpleName
    }

    fun download(url: String) {
        downloadQueue.onNext(url)
    }

    fun cancelDownload() {
        cancelEvents.onNext(true)
    }

    private fun downloadVideo(url: String): Observable<String> {
        cancelEvents.onNext(false)
        return Observable
            .create<String> { emitter ->
                fileManager.deleteFile(fileManager.getImportFilePath())
                val downloadFile = File(fileManager.getImportFilePath())

                val request = YoutubeDLRequest(url)
                request.setOption("-o", downloadFile.absolutePath)
                request.setOption("-f", "bestaudio")

                val videoInfo = youtubeDL.getInfo(url)
                Timber.d("cloud-pull downloadVideo() videoInfo: $videoInfo}")
                for (videoFormat in videoInfo.formats) {
                    Timber.d("cloud-pull VIDEO FORMAT: $videoFormat")
                }

                youtubeDL.execute(request) { progress, etaInSeconds ->
                    Timber.d("cloud-pull downloadVideo() progress $progress $etaInSeconds")
                    if (progress == 100.toFloat()) {
                        emitter.onNext(downloadFile.absolutePath)
                        emitter.onComplete()
                    }
                }
            }
            .zipWith(cancelEvents, toPair<String, Boolean>())
            .filter { !it.second }
            .map { it.first }
            .withErrorHandling(TAG, "Failed to download video")
    }
}