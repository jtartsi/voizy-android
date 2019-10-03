package com.voizy.android.middleware.firebase

enum class FirebaseAnalyticsEvent(val value: String) {
    RECORDING_SAVE("recording_save"),
    RECORDING_CANCEL("recording_cancel"),
    PLAY_VOIZY("play_voizy")
}