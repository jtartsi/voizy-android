package com.voizy.android.middleware.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class VoizyFirebaseAnalytics(val firebaseAnalytics: FirebaseAnalytics) {

    companion object {
        private const val SAVE_VOIZY = "save_voizy"
        private const val SAVE_VOIZY_CANCEL = "save_voizy_cancel"
        private const val PLAY_VOIZY = "play_voizy"
        private const val SHARE_TO = "choose_share_location"
        private const val EDIT_SCREEN_OPEN = "edit_screen_open"
        private const val YOUTUBE_DL_SELECTED = "youtube_dl_selected"
        private const val FILE_IMPORT_SELECTED = "file_import_selected"
        private const val RECORD_MICROPHONE = "record_microphone"
    }

    fun logAppOpen() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, Bundle())
    }

    fun logSaveVoizy() {
        firebaseAnalytics.logEvent(SAVE_VOIZY, Bundle())
    }

    fun logSaveVoizyCancel() {
        firebaseAnalytics.logEvent(SAVE_VOIZY_CANCEL, Bundle())
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
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "voizy")
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, params)
    }

    fun logShareToApplication(id: String?, name: String?, method: String?) {
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "voizy")
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        params.putString(FirebaseAnalytics.Param.METHOD, method)
        firebaseAnalytics.logEvent(SHARE_TO, params)
    }

    fun logSearch(searchTerm: String) {
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, params)
    }

    fun logEditScreenOpen() {
        firebaseAnalytics.logEvent(EDIT_SCREEN_OPEN, Bundle())
    }

    fun logYoutubeDLSelected() {
        firebaseAnalytics.logEvent(YOUTUBE_DL_SELECTED, Bundle())
    }

    fun logFileImportSelected() {
        firebaseAnalytics.logEvent(FILE_IMPORT_SELECTED, Bundle())
    }

    fun logRecordMicrophone() {
        firebaseAnalytics.logEvent(RECORD_MICROPHONE, Bundle())
    }
}