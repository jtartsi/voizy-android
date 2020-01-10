package com.voizy.android.viewmodels

import android.content.Context
import com.voizy.android.R
import com.voizy.android.error.DownloadDurationOverLimit
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

class YoutubeDownloadViewModel(
    private val context: Context,
    private val youtubeDL: YoutubeDL,
    private val fileManager: LocalFileManager
) : DisposingViewModel() {

    private val cancelEvents = BehaviorSubject.create<Boolean>()
    private val downloadQueue = PublishSubject.create<String>()
    val downloadEvents = downloadQueue
        .observeOn(Schedulers.io())
        .flatMap { downloadVideo(it) }!!

    private val errorQueue = PublishSubject.create<String>()
    val downloadErrors = errorQueue as Observable<String>

    companion object {
        private val TAG = YoutubeDownloadViewModel::class.java.simpleName
        private const val DOWNLOAD_DURATION_LIMIT = 900
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
                try {
                    fileManager.deleteFile(fileManager.getImportFilePath())
                    val downloadFile = File(fileManager.getImportFilePath())

                    val request = YoutubeDLRequest(url)
                    request.setOption("-o", downloadFile.absolutePath)
                    request.setOption("-f", "bestaudio")

                    val videoInfo = youtubeDL.getInfo(url)
                    if (videoInfo.duration > DOWNLOAD_DURATION_LIMIT) {
                        throw DownloadDurationOverLimit("Download duration over the limit of 15 minutes")
                    }

                    youtubeDL.execute(request) { progress, etaInSeconds ->
                        if (progress == 100.toFloat()) {
                            emitter.onNext(downloadFile.absolutePath)
                            emitter.onComplete()
                        }
                    }
                } catch (exception: InterruptedException) {
                    Timber.e(exception, "YoutubeDL process interrupted")
                }
            }
            .onErrorResumeNext(errorHandler())
            .zipWith(cancelEvents, toPair<String, Boolean>())
            .filter { !it.second }
            .map { it.first }
            .withErrorHandling(TAG, "Failed to download video")
    }

    private fun <T> errorHandler(): (throwable: Throwable) -> Observable<T> {
        return {
            Timber.d("download error handler $it")
            when (it) {
                is DownloadDurationOverLimit -> {
                    errorQueue.onNext(context.getString(R.string.error_downloading_duration_over_limit))
                }
                else -> {
                    errorQueue.onNext(context.getString(R.string.error_downloading_link))
                }
            }
            Observable.empty()
        }
    }
}