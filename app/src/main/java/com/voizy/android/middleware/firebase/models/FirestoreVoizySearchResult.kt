package com.voizy.android.middleware.firebase.models

import com.voizy.android.ui.models.Voizy

data class FirestoreVoizySearchResult(
    val resultsInfo: ResultsInfo = ResultsInfo(),
    val items: List<Voizy> = emptyList()
)

data class ResultsInfo(
    val itemsCount: Long = 0,
    val totalCount: Long = 0,
    val pageSize: Long = 0,
    val page: Long = 0,
    val hasMore: Boolean = false
)