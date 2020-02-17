package com.voizy.android.viewmodels

import androidx.lifecycle.ViewModel
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics

class CreateOptionsViewModel(
    private val firebaseAnalytics: VoizyFirebaseAnalytics
) : ViewModel() {

    fun logFileSelected() {
        firebaseAnalytics.logFileImportSelected()
    }

    fun logRecordSelected() {
        firebaseAnalytics.logRecordMicrophone()
    }

    fun logDownloadSelected() {
        firebaseAnalytics.logYoutubeDLSelected()
    }
}