package com.voizy.android.middleware.firebase.models

import com.google.firebase.firestore.PropertyName
import com.voizy.android.ui.models.Voizy

data class FirestoreVoizySearchResult(val hits: Hits) {

    fun getVoizys(): List<Voizy> {
        return this.hits.voizys.map {
            it.source.toVoizy()
        }
    }
}

data class Hits(
    @PropertyName("hits")
    val voizys: List<FirestoreElasticVoizy>,
    val total: Total
)

data class Total(val relation: String, val value: Int)

data class FirestoreElasticVoizy(
    @PropertyName("_id") val id: String,
    @PropertyName("_source") val source: Source
)

data class Source(
    val name: String,
    val tags: List<String>,
    val filePath: String
) {
    fun toVoizy() = Voizy(
        name = name,
        tags = tags,
        filePath = filePath
    )
}