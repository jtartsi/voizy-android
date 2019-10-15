package com.voizy.android.middleware.firebase.collections

import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.middleware.firebase.models.FirestoreVoizyShare
import com.voizy.android.utils.toObservable
import io.reactivex.Observable
import timber.log.Timber

class ShareCollection(private val firestore: FirebaseFirestore) {

    companion object {
        private val TAG = ShareCollection::class.java.simpleName
        private val SHARE_COLLECTION = "share"
    }

    fun share(id: String): Observable<Boolean> {
        return firestore.shareCollection()
            .add(FirestoreVoizyShare(id))
            .toObservable()
            .map { true }
            .onErrorReturn {
                Timber.e(it, "Sharing Voizy failed")
                false
            }
    }

    private fun FirebaseFirestore.shareCollection() = collection(SHARE_COLLECTION)
}