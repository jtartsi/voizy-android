package com.voizy.android.model

import com.voizy.android.repositories.FileRepository

class Voizy(private val path: String) {

    val filePath: String
        get() = path

    val name: String
        get() = filePath
            .replaceBefore(FileRepository.VOIZY_FILE_PREFIX, "")
            .removePrefix(FileRepository.VOIZY_FILE_PREFIX)
            .removeSuffix(FileRepository.MP3_FILE_EXT)
}
