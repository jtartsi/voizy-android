package com.voizy.android.middleware.firebase.collections

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.middleware.firebase.models.FirestoreVoizySearchRequest
import com.voizy.android.middleware.firebase.models.FirestoreVoizySearchResult
import com.voizy.android.utils.collectionChange
import com.voizy.android.utils.toObservable
import io.reactivex.Observable

class VoizySearchRequestCollection(private val firestore: FirebaseFirestore) {

    companion object {
        private val TAG = VoizySearchRequestCollection::class.java.simpleName
        const val VOIZYS_SEARCH_COLLECTION = "voizySearchRequests"
        const val RESULT = "result"
    }

    fun voizys(
        searchKeyword: String,
        from: Int,
        pageSize: Int
    ): Observable<FirestoreVoizySearchResult> {
        return firestore
            .voizySearchRequestCollection()
            .add(
                FirestoreVoizySearchRequest(
                    searchKeyword = searchKeyword,
                    from = from,
                    size = pageSize
                )
            )
            .toObservable()
            .map { it.result() }
            .collectionChange()
            .map {
                it.toObjects(FirestoreVoizySearchResult::class.java)
            }
            .filter { it.size != 0 }
            .map { it.first() }
    }
}

fun FirebaseFirestore.voizySearchRequestCollection() =
    collection(VoizySearchRequestCollection.VOIZYS_SEARCH_COLLECTION)

fun DocumentReference.result() = collection(VoizySearchRequestCollection.RESULT)