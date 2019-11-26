package com.voizy.android.audio

import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import io.reactivex.Observable
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class MediaEditor(private val ffmpeg: FFmpeg) {

    fun clip(
        sourceFile: String,
        outputFile: String,
        startPosMillis: Long = 0,
        endPosMillis: Long = 15000
    ): Observable<String> {
        val startPos = getFFmpegTime(startPosMillis)
        val endPos = getFFmpegTime(endPosMillis)

        val cmd = arrayOf(
            "-i", sourceFile,
            "-ss", startPos,
            "-to", endPos,
            outputFile
        )
        return execute(cmd).map { outputFile }
    }

    private fun execute(commandList: Array<String>): Observable<String> {
        return Observable.defer {
            Observable.create<String> { emitter ->
                ffmpeg.execute(commandList, object : FFmpegExecuteResponseHandler {
                    override fun onFinish() {
                        Timber.v("FFmpeg.onSuccess()")
                        emitter.onComplete()
                    }

                    override fun onSuccess(message: String?) {
                        Timber.v("FFmpeg.onSuccess()")
                        emitter.onNext(message.orEmpty())
                    }

                    override fun onFailure(message: String?) {
                        Timber.e("FFmpeg.onSuccess() $message")
                        emitter.onError(Throwable(message))
                    }

                    override fun onProgress(message: String?) {
                        Timber.v("FFmpeg.onProgress() $message")
                    }

                    override fun onStart() {
                        Timber.v("FFmpeg.onStart()")
                    }
                })
            }
        }
    }

    private fun getFFmpegTime(timeInMillis: Long): String {
        SimpleDateFormat.getInstance()
        val dateFormat = SimpleDateFormat("HH:mm:ss.SSS")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(Date(timeInMillis).time)
    }
}