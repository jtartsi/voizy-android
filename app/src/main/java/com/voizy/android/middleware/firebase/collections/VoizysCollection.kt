package com.voizy.android.middleware.firebase.collections

import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.ui.models.Voizy
import com.voizy.android.utils.toObservable
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable

class VoizysCollection(private val firestore: FirebaseFirestore) {

    companion object {
        private val TAG = VoizysCollection::class.java.simpleName
        private val VOIZYS_COLLECTION = "voizys"
    }

    fun saveVoizy(voizy: Voizy): Observable<Pair<Boolean, Voizy?>> {
        return firestore
            .voizysCollection()
            .add(voizy)
            .toObservable()
            .map { Pair<Boolean, Voizy?>(true, voizy) }
            .withErrorHandling(TAG, "Failed to save Voizy to Firestore")
    }

    private fun FirebaseFirestore.voizysCollection() = collection(VOIZYS_COLLECTION)
}