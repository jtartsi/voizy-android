package com.voizy.android.middleware.firebase.models

import com.google.firebase.firestore.PropertyName

data class FirestoreVoizySearchResult(val hits: Hits)

data class Hits(
    @PropertyName("hits")
    val voizys: List<FirestoreElasticVoizy>,
    val total: Total
)

data class Total(val relation: String, val value: Int)

data class FirestoreElasticVoizy(
    @PropertyName("_id") val id: String,
    @PropertyName("_source") val source: Source
) {
    inner class Source(
        val name: String,
        @PropertyName("createdAt") val createdAtInSeconds: Int,
        val tags: List<String>
    )
}

