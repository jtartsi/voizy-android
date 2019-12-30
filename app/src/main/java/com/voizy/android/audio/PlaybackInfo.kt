package com.voizy.android.audio

data class PlaybackInfo(
    val playbackEvent: PlaybackEvent,
    val audioDurationInMs: Int = 0,
    val progressInMs: Int = 0
)