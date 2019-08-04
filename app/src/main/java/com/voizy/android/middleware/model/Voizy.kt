package com.voizy.android.middleware.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.voizy.android.middleware.repositories.LocalFileManager

@IgnoreExtraProperties
class Voizy(
    private val localFilePath: String,
    private val tagsList: List<String> = listOf()
) {

    companion object {
        private const val FIRESTORE_NAME = "name"
        private const val FIRESTORE_TAGS = "tags"
    }

    val tags: Map<String, Boolean>
        get() = tagsList.map { Pair(it, true) }.toMap()

    val localPath: String
        get() = localFilePath

    val name: String
        get() = localPath
            .replaceBefore(LocalFileManager.VOIZY_FILE_PREFIX, "")
            .removePrefix(LocalFileManager.VOIZY_FILE_PREFIX)
            .removeSuffix(LocalFileManager.MP3_FILE_EXT)

    fun toFirestoreData(): HashMap<String, Any> {
        return hashMapOf(
            FIRESTORE_NAME to name,
            FIRESTORE_TAGS to tags
        )
    }
}
