package com.voizy.android.utils

class SupportedFileTypes {
    companion object {
        private const val AUDIO_MPEG = "audio/mpeg"
        private const val AUDIO_MP3 = "audio/mp3"
        private const val AUDIO_AAC = "audio/aac"
        private const val AUDIO_X_WAV = "audio/x-wav"
        private const val AUDIO_AMR = "audio/amr"
        private const val AUDIO_MP4 = "audio/mp4"
        private const val AUDIO_M4A = "audio/m4a"
        private const val AUDIO_MPEG_4 = "audio/mpeg-4"
        private const val AUDIO_X_M4A = "audio/x-m4a"
        private const val VIDEO_ANY = "video/*"

        fun toArray(): Array<String> {
            return arrayOf(
                AUDIO_MPEG, AUDIO_MP3, AUDIO_AAC, AUDIO_X_WAV, AUDIO_AMR,
                AUDIO_MP4, AUDIO_M4A, AUDIO_MPEG_4, AUDIO_X_M4A, VIDEO_ANY
            )
        }
    }
}