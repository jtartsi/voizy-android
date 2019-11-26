package com.voizy.android.audio

import android.content.Context
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import timber.log.Timber

class FFmpegBuilder {
    companion object {
        fun getInstance(context: Context): FFmpeg {
            val ffmpeg = FFmpeg.getInstance(context)
            ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                override fun onFinish() {
                    super.onFinish()
                    Timber.d("FFmpegBuilder.onFinish")
                }

                override fun onSuccess() {
                    super.onSuccess()
                    Timber.d("FFmpegBuilder.onSuccess")
                }

                override fun onFailure() {
                    super.onFailure()
                    Timber.d("FFmpegBuilder.onFailure")
                }

                override fun onStart() {
                    super.onStart()
                    Timber.d("FFmpegBuilder.onStart")
                }
            })
            return ffmpeg
        }
    }
}