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
        private val VOIZYS_DEV_COLLECTION =
            "voizys-dev" // TODO search go back to production version
    }

    fun saveVoizy(voizy: Voizy): Observable<Pair<Boolean, Voizy?>> {
        return firestore
            .voizysCollection()
            .add(voizy)
            .toObservable()
            .map { Pair<Boolean, Voizy?>(true, voizy) }
            .withErrorHandling(TAG, "Failed to save Voizy to Firestore")
    }

    // TODO search remove once done
    fun getVoizys(): Observable<List<Voizy>> {
        return firestore
            .voizysCollection()
            .get()
            .toObservable()
            .map { it.toObjects(Voizy::class.java) }
    }

    private fun FirebaseFirestore.voizysCollection() = collection(VOIZYS_DEV_COLLECTION)
}