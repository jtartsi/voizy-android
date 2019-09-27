package com.voizy.android.middleware.firebase.models

import com.google.firebase.firestore.PropertyName
import com.voizy.android.ui.models.Voizy

data class FirestoreVoizySearchResult(val hits: Hits = Hits()) {

    fun getVoizys(): List<Voizy> {
        return this.hits.voizys.map {
            it.source.toVoizy()
        }
    }
}

data class Hits(
    @PropertyName("hits")
    val voizys: List<FirestoreElasticVoizy> = emptyList(),
    val total: Total = Total()
)

data class Total(
    val relation: String = "",
    val value: Int = 0
)

data class FirestoreElasticVoizy(
    @PropertyName("_id") val id: String = "",
    @PropertyName("_source") val source: Source = Source()
)

data class Source(
    val name: String = "",
    val tags: List<String> = emptyList(),
    val filePath: String = ""
) {
    fun toVoizy() = Voizy(
        name = name,
        tags = tags,
        filePath = filePath
    )
}