package com.voizy.android.middleware.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class VoizyFirebaseAnalytics(val firebaseAnalytics: FirebaseAnalytics) {

    companion object {
        private const val RECORDING_SAVE = "recording_save"
        private const val RECORDING_CANCEL = "recording_cancel"
        private const val PLAY_VOIZY = "play_voizy"
        private const val SHARE_TO = "choose_share_location"
    }

    fun logAppOpen() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, Bundle())
    }

    fun logRecordingSave() {
        firebaseAnalytics.logEvent(RECORDING_SAVE, Bundle())
    }

    fun logRecordingCancel() {
        firebaseAnalytics.logEvent(RECORDING_CANCEL, Bundle())
    }

    fun logPlayVoizy(id: String, name: String) {
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        firebaseAnalytics.logEvent(PLAY_VOIZY, params)
    }

    fun logShareVoizyEvent(id: String, name: String) {
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, id)
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, params)
    }

    fun logChooseShareApplication(method: String) {
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.METHOD, method)
        firebaseAnalytics.logEvent(SHARE_TO, params)
    }

    fun logSearch(searchTerm: String) {
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, params)
    }
}