package com.voizy.android.middleware.model

import com.voizy.android.middleware.repositories.LocalFileManager

class Voizy(
    private val localPath: String,
    private val tags: List<String> = listOf()
) {

    val tagsMap: Map<String, Boolean>
        get() = tags.map { Pair(it, true) }.toMap()

    val localFilePath: String
        get() = localPath

    val name: String
        get() = localPath
            .replaceBefore(LocalFileManager.VOIZY_FILE_PREFIX, "")
            .removePrefix(LocalFileManager.VOIZY_FILE_PREFIX)
            .removeSuffix(LocalFileManager.MP3_FILE_EXT)
}
