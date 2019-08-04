package com.voizy.android.middleware.model

import com.voizy.android.middleware.repositories.LocalFileManager

class Voizy(private val path: String) {

    val filePath: String
        get() = path

    val name: String
        get() = filePath
            .replaceBefore(LocalFileManager.VOIZY_FILE_PREFIX, "")
            .removePrefix(LocalFileManager.VOIZY_FILE_PREFIX)
            .removeSuffix(LocalFileManager.MP3_FILE_EXT)
}
