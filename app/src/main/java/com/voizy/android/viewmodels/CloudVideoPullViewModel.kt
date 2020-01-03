package com.voizy.android.viewmodels

import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.utils.withErrorHandling
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import io.reactivex.Observable
import timber.log.Timber
import java.io.File

class CloudVideoPullViewModel(
    private val youtubeDL: YoutubeDL,
    private val fileManager: LocalFileManager
) : DisposingViewModel() {

    companion object {
        private val TAG = CloudVideoPullViewModel::class.java.simpleName
    }

    fun downloadVideo(url: String): Observable<Float> {
        Timber.d("cloud-pull downloadVideo()")
        return Observable
            .create<Float> { emitter ->
                emitter.onNext(0f)
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
                    Timber.d("cloud-pull downloadVideo() prgress $progress $etaInSeconds")
                    emitter.onNext(progress)
                    if (progress == 100.toFloat()) {
                        emitter.onComplete()
                    }
                }
            }.withErrorHandling(TAG, "Failed to download video")
    }
}