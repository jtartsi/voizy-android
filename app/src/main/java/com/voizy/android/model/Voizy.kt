package com.voizy.android.model

class Voizy(private val path: String) {

    val filePath: String
        get() = path

    val name: String
        get() = filePath.replaceBefore("_", "")
}
