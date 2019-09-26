package com.voizy.android.middleware.firebase.collections

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.middleware.firebase.model.VoizySearchRequest
import com.voizy.android.utils.toObservable
import io.reactivex.Observable

class VoizysSearchCollection(private val firestore: FirebaseFirestore) {

    companion object {
        private val TAG = VoizysSearchCollection::class.java.simpleName
        private val VOIZYS_SEARCH_COLLECTION = "voizySearchRequests"
    }

    fun find(searchKeyword: String = ""): Observable<DocumentReference> {
        return firestore
            .collection(VOIZYS_SEARCH_COLLECTION)
            .add(VoizySearchRequest(searchKeyword = searchKeyword))
            .toObservable()
    }

    fun getVoizyResults(): Observable<String> {
        return Observable.just("")
    }
}