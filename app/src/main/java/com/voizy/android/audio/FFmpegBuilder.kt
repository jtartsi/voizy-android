package com.voizy.android.audio

import android.content.Context
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import timber.log.Timber

class FFmpegBuilder {
    companion object {
        public fun getInstance(context: Context): FFmpeg {
            Timber.d("ffmpeg-build buildFFmpeg()")
            val ffmpeg = FFmpeg.getInstance(context)
            Timber.d("ffmpeg-build buildFFmpeg()")
            ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                override fun onFinish() {
                    super.onFinish()
                    Timber.d("ffmpeg-build onFinish")
                }

                override fun onSuccess() {
                    super.onSuccess()
                    Timber.d("ffmpeg-build onSuccess")
                }

                override fun onFailure() {
                    super.onFailure()
                    Timber.d("ffmpeg-build onFailure")
                }

                override fun onStart() {
                    super.onStart()
                    Timber.d("ffmpeg-build onStart")
                }
            })
            return ffmpeg
        }
    }
}