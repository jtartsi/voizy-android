package com.voizy.android.ui.model

data class ImportedData(val filePath: String) {

    val fileExtension: String
        get() = filePath!!.split(".")!!.last()

    var durationInMillis: Long = -1

    val durationInSecods: Long
        get() = durationInMillis / 1000
}