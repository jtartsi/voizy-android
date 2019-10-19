package com.voizy.android.middleware.firebase.collections

import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.middleware.firebase.models.FirestoreVoizyShare
import timber.log.Timber

class ShareCollection(private val firestore: FirebaseFirestore) {

    companion object {
        private val TAG = ShareCollection::class.java.simpleName
        private const val SHARE_COLLECTION = "share"
    }

    fun sendShareEvent(voizyId: String) {
        firestore.shareCollection()
            .add(FirestoreVoizyShare(voizyId))
            .addOnSuccessListener {
                Timber.d("Sharing Voizy success")
            }
            .addOnFailureListener {
                Timber.e("Sharing Voizy failed $it")
            }
    }

    private fun FirebaseFirestore.shareCollection() = collection(SHARE_COLLECTION)
}