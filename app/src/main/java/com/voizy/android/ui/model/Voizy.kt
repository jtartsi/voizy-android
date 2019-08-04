package com.voizy.android.ui.model

import com.voizy.android.middleware.firebase.model.FirestoreVoizy
import com.voizy.android.middleware.local.LocalFileManager

data class Voizy(
    private val localFilePath: String,
    private var tagsList: List<String> = listOf()
) {
    constructor() : this("", listOf())

    val localPath: String
        get() = localFilePath

    var tags: List<String>
        get() = tagsList
        set(newTags) {
            tags = newTags
        }

    val name: String
        get() = localPath
            .replaceBefore(LocalFileManager.VOIZY_FILE_PREFIX, "")
            .removePrefix(LocalFileManager.VOIZY_FILE_PREFIX)
            .removeSuffix(LocalFileManager.MP3_FILE_EXT)

    fun toFirestoreData(): FirestoreVoizy {
        return FirestoreVoizy(name, tags.map { it to true }.toMap())
    }
}
