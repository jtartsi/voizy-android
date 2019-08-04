package com.voizy.android.ui.model

import com.voizy.android.middleware.firebase.model.FirestoreVoizy
import com.voizy.android.middleware.local.LocalFileManager

data class Voizy(
    private val localFilePath: String,
    private val tagsList: List<String> = listOf()
) {

    constructor() : this("", listOf())

    val tags: Map<String, Boolean>
        get() = tagsList.map { Pair(it, true) }.toMap()

    val localPath: String
        get() = localFilePath

    val name: String
        get() = localPath
            .replaceBefore(LocalFileManager.VOIZY_FILE_PREFIX, "")
            .removePrefix(LocalFileManager.VOIZY_FILE_PREFIX)
            .removeSuffix(LocalFileManager.MP3_FILE_EXT)

    fun toFirestoreData(): FirestoreVoizy {
        return FirestoreVoizy(name, tags)
    }
}
