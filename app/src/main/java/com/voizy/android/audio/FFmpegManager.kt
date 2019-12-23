package com.voizy.android.audio

import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import io.reactivex.Observable
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class FFmpegManager {

    init {
        Config.enableLogCallback {
            Timber.d("FFmpeg $it")
        }

        Config.enableStatisticsCallback {
            val update = String.format("frame: %d, time: %d", it.videoFrameNumber, it.time)
            Timber.d("FFmpeg $update")
        }
    }

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

    fun convertToAudio(sourceFile: String, outputFile: String): Observable<String> {
        val cmd = arrayOf(
            "-i", sourceFile,
            outputFile
        )
        return execute(cmd).map { outputFile }
    }

    private fun execute(arguments: Array<String>): Observable<Any> {
        return Observable.defer {
            Observable.fromCallable {
                FFmpeg.execute(arguments)
            }
        }
    }

    private fun getFFmpegTime(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss.SSS")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(Date(timeInMillis).time)
    }
}