package com.voizy.android.middleware.firebase.models

import com.google.firebase.firestore.PropertyName
import com.voizy.android.ui.models.Voizy

data class FirestoreVoizySearchResult(val hits: Hits = Hits()) {

    fun getVoizys(): List<Voizy> {
        return this.hits.hits.map { it.toVoizy() }
    }
}

data class Hits(
    var hits: List<FirestoreElasticVoizy> = emptyList(),
    val total: Total = Total()
)

data class Total(
    val relation: String = "",
    val value: Int = 0
)

data class FirestoreElasticVoizy(
    @get:PropertyName("_id")
    @set:PropertyName("_id")
    var id: String = "",
    @get:PropertyName("_source")
    @set:PropertyName("_source")
    var source: Source = Source()
) {
    fun toVoizy() = Voizy(this.id, this.source.name, this.source.tags, this.source.filePath)
}

data class Source(
    val name: String = "",
    val tags: List<String> = emptyList(),
    val filePath: String = ""
)