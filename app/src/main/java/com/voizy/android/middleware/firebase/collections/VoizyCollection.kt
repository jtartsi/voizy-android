package com.voizy.android.middleware.firebase.collections

import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.middleware.firebase.model.FirestoreVoizy
import com.voizy.android.ui.model.Voizy
import com.voizy.android.utils.toObservable
import io.reactivex.Observable

class VoizyCollection(private val firestore: FirebaseFirestore) {

    companion object {
        private val VOIZYS_COLLECTION = "voizys"
    }

    fun saveVoizy(voizy: Voizy): Observable<Pair<Boolean, Voizy?>> {
        return firestore
            .collection(VOIZYS_COLLECTION)
            .add(voizy.toFirestoreData())
            .toObservable()
            .map { Pair<Boolean, Voizy?>(true, voizy) }
            .onErrorReturn { Pair(false, null) }
    }

    fun getVoizys(): Observable<List<FirestoreVoizy>> {
        return firestore
            .collection(VOIZYS_COLLECTION)
            .get()
            .toObservable()
            .map { querySnapshot ->
                querySnapshot.documents.map { FirestoreVoizy.from(it) }
            }
    }
}