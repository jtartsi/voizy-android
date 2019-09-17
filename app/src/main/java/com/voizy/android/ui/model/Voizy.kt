package com.voizy.android.ui.model

import com.voizy.android.middleware.firebase.model.FirestoreVoizy

data class Voizy(
    private val nameField: String,
    private var tagsList: List<String> = listOf()
) {
    constructor() : this("", listOf())

    var firebaseFilePath: String? = null
    var localFilePath: String? = null

    var tags: List<String>
        get() = tagsList
        set(newTags) {
            tags = newTags
        }

    val name: String
        get() = nameField

    fun toFirestoreData(): FirestoreVoizy {
        return FirestoreVoizy(name, tags, firebaseFilePath!!)
    }
}
