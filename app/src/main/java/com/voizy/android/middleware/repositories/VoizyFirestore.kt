package com.voizy.android.middleware.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.middleware.model.Voizy
import timber.log.Timber

class VoizyFirestore(private val firestore: FirebaseFirestore) {

    companion object {
        private val VOIZYS_COLLECTION = "voizys"
    }

    fun saveVoizy(voizy: Voizy) {

        Timber.d("saveVoizy()")
        firestore.collection(VOIZYS_COLLECTION)
            .add(voizy.toFirestoreData())
            .addOnSuccessListener {
                Timber.d("saveVoizy success $it")
            }
            .addOnFailureListener {
                Timber.e(it, "saveVoizy failed")
            }
    }
}