package com.voizy.android.middleware.firebase.model

//TODO search only one type of Voizy into use
data class VoizySearchResult(
    val count: Int,
    val totalCount: Int,
    val voizys: List<FirestoreVoizy>
)