package com.voizy.android.middleware.firebase.collections

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.middleware.firebase.models.FirestoreVoizySearchRequest
import com.voizy.android.utils.toObservable
import io.reactivex.Observable

class VoizySearchRequestCollection(private val firestore: FirebaseFirestore) {

    companion object {
        private val TAG = VoizySearchRequestCollection::class.java.simpleName
        const val VOIZYS_SEARCH_COLLECTION = "voizySearchRequests"
        const val RESULT = "result"
    }

    fun find(searchKeyword: String = ""): Observable<DocumentReference> {
        return firestore
            .voizySearchRequestCollection()
            .add(FirestoreVoizySearchRequest(searchKeyword = searchKeyword))
            .toObservable()
    }

    fun getVoizyResults(): Observable<String> {
        return Observable.just("")
    }
}

fun FirebaseFirestore.voizySearchRequestCollection() =
    collection(VoizySearchRequestCollection.VOIZYS_SEARCH_COLLECTION)

fun DocumentReference.result() = collection(VoizySearchRequestCollection.RESULT)

fun DocumentReference.resultDoc(uid: String) = result().document(uid)