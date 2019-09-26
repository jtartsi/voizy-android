package com.voizy.android.middleware.firebase.collections

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.middleware.firebase.models.VoizySearchRequest
import com.voizy.android.utils.toObservable
import io.reactivex.Observable

class VoizySearchRequestCollection(private val firestore: FirebaseFirestore) {

    companion object {
        private val TAG = VoizySearchRequestCollection::class.java.simpleName
        private const val VOIZYS_SEARCH_COLLECTION = "voizySearchRequests"
    }

    fun find(searchKeyword: String = ""): Observable<DocumentReference> {
        return firestore
            .voizySearchRequestCollection()
            .add(VoizySearchRequest(searchKeyword = searchKeyword))
            .toObservable()
    }

    fun getVoizyResults(): Observable<String> {
        return Observable.just("")
    }

    private fun FirebaseFirestore.voizySearchRequestCollection() =
        collection(VOIZYS_SEARCH_COLLECTION)
}