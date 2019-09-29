package com.voizy.android.ui.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Voizy(
    val name: String = "",
    val tags: List<String> = emptyList(),
    val filePath: String = "",
    @ServerTimestamp
    val createdAt: Timestamp = Timestamp.now()
)