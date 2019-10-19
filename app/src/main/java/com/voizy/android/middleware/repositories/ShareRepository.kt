package com.voizy.android.middleware.repositories

import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.collections.ShareCollection
import com.voizy.android.middleware.firebase.models.Voizy

class ShareRepository(
    private val shareCollection: ShareCollection,
    private val firebaseAnalytics: VoizyFirebaseAnalytics
) {

    var shareVoizy: Voizy? = null

    fun startVoizyShare(voizy: Voizy) {
        shareVoizy = voizy
    }

    fun finishShare(packageName: String?) {
        firebaseAnalytics.logShareToApplication(shareVoizy?.id, shareVoizy?.name, packageName)
        shareCollection.sendShareEvent(shareVoizy!!.id)
    }
}