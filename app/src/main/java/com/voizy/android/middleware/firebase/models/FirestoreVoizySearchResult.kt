package com.voizy.android.middleware.firebase.models

data class FirestoreVoizySearchResult(
    val resultsInfo: ResultsInfo = ResultsInfo(),
    val items: List<Voizy> = emptyList()
)

data class ResultsInfo(
    val itemsCount: Long = 0,
    val totalCount: Long = 0,
    val size: Long = 0,
    val from: Long = 0,
    val hasMore: Boolean = false
)