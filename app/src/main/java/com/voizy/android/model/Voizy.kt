package com.voizy.android.model

import com.voizy.android.repositories.FileRepository

class Voizy(private val path: String) {

    val filePath: String
        get() = path

    val name: String
        get() = filePath
            .replaceBefore("voizy_", "")
            .removePrefix("voizy_")
            .removeSuffix(FileRepository.MP3_FILE_EXT)
}
