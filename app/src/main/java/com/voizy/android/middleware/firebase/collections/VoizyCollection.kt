package com.voizy.android.middleware.firebase.collections

import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.middleware.firebase.model.FirestoreVoizy
import com.voizy.android.ui.model.Voizy
import timber.log.Timber

class VoizyCollection(private val firestore: FirebaseFirestore) {

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

    fun getVoizys() {
        firestore.collection(VOIZYS_COLLECTION)
            .get()
            .addOnSuccessListener {
                Timber.d("readVoizys success $it")
                it.documents.forEach { documentSnapshot ->
                    val voizy = FirestoreVoizy.from(documentSnapshot)
                    Timber.d("readVoizys voizy $voizy")
                }
            }
            .addOnFailureListener {
                Timber.e(it, "readVoizys failed")
            }
    }
}